package io.github.thanosfisherman.demo

import io.github.thanosfisherman.demo.audioUtils.Music
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.lwjgl.openal.AL10.*
import org.lwjgl.stb.STBVorbis.*
import org.lwjgl.stb.STBVorbisInfo
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import java.io.InputStream
import java.net.URI
import java.net.URL
import java.nio.ByteBuffer
import java.nio.ShortBuffer
import kotlin.concurrent.Volatile
import kotlin.time.Duration.Companion.milliseconds

class MusicPlayer(
    bufferCount: Int = 4,
    private val bufferSizeSamples: Int = 8192
) : Music {
    private var source = 0
    private val streamBuffers = IntArray(bufferCount)

    private var decoder: Long = 0
    private var channels = 0
    private var sampleRate = 0
    private var alFormat = 0

    private var pcmBuffer: ShortBuffer? = null
    private var oggData: ByteBuffer? = null // must stay alive for the lifetime of the decoder

    private var loop = false
    @Volatile private var playing = false
    var scope: CoroutineScope? = null

    override fun init() {
        source = alGenSources()
        alGenBuffers(streamBuffers)
    }

    /**
     * Loads an OGG Vorbis resource for streaming playback.
     * @param path a classpath resource path, e.g. "/music/track.ogg"
     */
    override fun load(path: String) {
        stop()

        val bytes = readResourceBytes(path)
        val buffer = MemoryUtil.memAlloc(bytes.size)
        buffer.put(bytes).flip()
        oggData = buffer

        MemoryStack.stackPush().use { stack ->
            val error = stack.mallocInt(1)
            decoder = stb_vorbis_open_memory(buffer, error, null)
            require(decoder != 0L) { "Failed to open OGG resource '$path' (stb_vorbis error ${error[0]})" }

            val info = STBVorbisInfo.malloc(stack)
            stb_vorbis_get_info(decoder, info)
            channels = info.channels()
            sampleRate = info.sample_rate()
            alFormat = if (channels == 1) AL_FORMAT_MONO16 else AL_FORMAT_STEREO16
        }

        pcmBuffer = MemoryUtil.memAllocShort(bufferSizeSamples * channels)
    }

    private fun readResourceBytes(path: String): ByteArray {
        // Compose Multiplatform (and other loaders) may hand us a full URL,
        // e.g. "jar:file:/.../app.jar!/composeResources/.../track.ogg"
        // rather than a plain classpath-relative path.
        val looksLikeUrl = path.contains("://") || path.startsWith("jar:")

        val stream = if (looksLikeUrl) {
            URI.create(path).toURL().openStream()
        } else {
            javaClass.getResourceAsStream(path)
                ?: javaClass.classLoader.getResourceAsStream(path.removePrefix("/"))
                ?: error("Resource not found on classpath: $path")
        }

        return stream.use { it.readBytes() }
    }

    override fun play(loop: Boolean, volume: Float) {
        require(decoder != 0L) { "No track loaded — call load() first" }
        this.loop = loop
        alSourcef(source, AL_GAIN, volume.coerceAtLeast(0f))
        for (buf in streamBuffers) {
            if (!fillBuffer(buf)) break
        }
        alSourceQueueBuffers(source, streamBuffers)
        alSourcePlay(source)
        playing = true
        scope?.cancel()
        scope = CoroutineScope(Dispatchers.Default)
        scope?.launch {
            while (playing) {
                update()
                delay(10.milliseconds)
            }
        }
    }

    override fun update() {
        if (!playing) return
        val processed = alGetSourcei(source, AL_BUFFERS_PROCESSED)
        repeat(processed) {
            val buf = alSourceUnqueueBuffers(source)
            if (fillBuffer(buf)) {
                alSourceQueueBuffers(source, buf)
            } else {
                playing = false
            }
        }
        if (playing && alGetSourcei(source, AL_SOURCE_STATE) != AL_PLAYING) {
            alSourcePlay(source)
        }
    }

    override fun setVolume(volume: Float) = alSourcef(source, AL_GAIN, volume.coerceAtLeast(0f))
    override fun pause() = alSourcePause(source)
    override fun resume() = alSourcePlay(source)

    override fun stop() {
        playing = false
        if (source != 0) {
            alSourceStop(source)
            val queued = alGetSourcei(source, AL_BUFFERS_QUEUED)
            if (queued > 0) {
                val tmp = IntArray(queued)
                alSourceUnqueueBuffers(source, tmp)
            }
        }
        if (decoder != 0L) {
            stb_vorbis_close(decoder)
            decoder = 0
        }
        pcmBuffer?.let { MemoryUtil.memFree(it) }
        pcmBuffer = null
        oggData?.let { MemoryUtil.memFree(it) }
        oggData = null
        scope?.cancel()
    }

    private fun fillBuffer(bufferId: Int): Boolean {
        val pcm = pcmBuffer ?: return false
        pcm.clear()
        var samples = stb_vorbis_get_samples_short_interleaved(decoder, channels, pcm)
        if (samples == 0) {
            if (!loop) return false
            stb_vorbis_seek_start(decoder)
            samples = stb_vorbis_get_samples_short_interleaved(decoder, channels, pcm)
            if (samples == 0) return false
        }
        pcm.limit(samples * channels)
        alBufferData(bufferId, alFormat, pcm, sampleRate)
        return true
    }

    override fun dispose() {
        stop()
        if (source != 0) alDeleteSources(source)
        alDeleteBuffers(streamBuffers)
    }
}