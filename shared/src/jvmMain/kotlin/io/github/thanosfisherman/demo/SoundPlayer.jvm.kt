package io.github.thanosfisherman.demo

import io.github.thanosfisherman.demo.audioUtils.Sound
import org.lwjgl.openal.AL
import org.lwjgl.openal.AL10.*
import org.lwjgl.openal.ALC
import org.lwjgl.openal.ALC10
import org.lwjgl.system.MemoryUtil
import java.io.BufferedInputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream
import java.net.URI
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem

class SoundPlayer(sourcePoolSize: Int = 16) : Sound {

    private var device: Long = 0
    private var context: Long = 0
    private val sourcePool = IntArray(sourcePoolSize)
    private var nextSource = 0

    // cache: file path -> AL buffer id
    private val bufferCache = HashMap<String, Int>()

    override fun init() {
        val defaultDeviceName = ALC10.alcOpenDevice(null as CharSequence?)
        device = defaultDeviceName
        require(device != 0L) { "Failed to open default OpenAL device" }

        val attribs = intArrayOf(0)
        context = ALC10.alcCreateContext(device, attribs)
        ALC10.alcMakeContextCurrent(context)

        val alcCapabilities = ALC.createCapabilities(device)
        AL.createCapabilities(alcCapabilities)

        // Pre-generate a pool of sources we reuse for SFX
        for (i in sourcePool.indices) {
            sourcePool[i] = alGenSources()
        }
    }

    /** Loads a mono/stereo PCM WAV file into an AL buffer. Cached by path. */
    override fun loadSound(path: String): Int {
        bufferCache[path]?.let { return it }

        val audioIn = run {
            val looksLikeUrl = path.contains("://") || path.startsWith("jar:") || path.startsWith("file:")
            val stream: InputStream = if (looksLikeUrl) {
                URI.create(path).toURL().openStream()
            } else {
                val file = File(path)
                if (file.exists() && file.isFile) {
                    file.inputStream()
                } else {
                    val cleanPath = path.removePrefix("/")
                    val candidatePaths = listOf(
                        path,
                        cleanPath,
                        "composeResources/composecreativedemo.shared.generated.resources/$cleanPath",
                        "composeResources/composecreativedemo.shared.generated.resources/files/$cleanPath",
                        "composeResources/composecreativedemo.shared.generated.resources/files/sounds/${cleanPath.substringAfterLast('/')}",
                        "composeResources/composecreativedemo.shared.generated.resources/sounds/${cleanPath.substringAfterLast('/')}",
                        "composeResources/demo.shared.generated.resources/$cleanPath",
                        "composeResources/demo.shared.generated.resources/files/$cleanPath",
                        "composeResources/demo.shared.generated.resources/files/sounds/${cleanPath.substringAfterLast('/')}",
                        "composeResources/demo.shared.generated.resources/sounds/${cleanPath.substringAfterLast('/')}",
                        "files/$cleanPath",
                        "sounds/${cleanPath.substringAfterLast('/')}",
                        cleanPath.substringAfterLast('/')
                    ).distinct()

                    candidatePaths.firstNotNullOfOrNull { candidate ->
                        SoundPlayer::class.java.getResourceAsStream(if (candidate.startsWith("/")) candidate else "/$candidate")
                            ?: SoundPlayer::class.java.classLoader.getResourceAsStream(candidate.removePrefix("/"))
                            ?: Thread.currentThread().contextClassLoader.getResourceAsStream(candidate.removePrefix("/"))
                    } ?: throw FileNotFoundException("Could not find audio file or resource: $path")
                }
            }
            val bufferedStream = if (stream.markSupported()) stream else BufferedInputStream(stream)
            AudioSystem.getAudioInputStream(bufferedStream)
        }
        val format: AudioFormat = audioIn.format
        val data = audioIn.readAllBytes()

        val alFormat = when (format.channels) {
            1 if format.sampleSizeInBits == 8 -> AL_FORMAT_MONO8
            1 if format.sampleSizeInBits == 16 -> AL_FORMAT_MONO16
            2 if format.sampleSizeInBits == 8 -> AL_FORMAT_STEREO8
            2 if format.sampleSizeInBits == 16 -> AL_FORMAT_STEREO16
            else -> error("Unsupported audio format: $format")
        }

        val buffer = MemoryUtil.memAlloc(data.size)
        buffer.put(data).flip()

        val alBuffer = alGenBuffers()
        alBufferData(alBuffer, alFormat, buffer, format.sampleRate.toInt())

        MemoryUtil.memFree(buffer)
        audioIn.close()

        bufferCache[path] = alBuffer
        return alBuffer
    }

    /**
     * Plays a short sound effect.
     * @param volume linear gain, 1.0 = normal, 0.0 = silent, >1.0 = boosted
     * @param pitch playback speed/pitch multiplier, 1.0 = normal
     */
    override fun play(bufferId: Int, volume: Float, pitch: Float) {
        val source = sourcePool[nextSource]
        nextSource = (nextSource + 1) % sourcePool.size

        // Stop whatever this pooled source was doing before reuse
        alSourceStop(source)

        alSourcei(source, AL_BUFFER, bufferId)
        alSourcef(source, AL_GAIN, volume.coerceAtLeast(0f))
        alSourcef(source, AL_PITCH, pitch.coerceIn(0.01f, 4f)) // AL_PITCH must stay > 0
        alSourcePlay(source)
    }

    override fun dispose() {
        sourcePool.forEach { alDeleteSources(it) }
        bufferCache.values.forEach { alDeleteBuffers(it) }
        bufferCache.clear()

        ALC10.alcMakeContextCurrent(0)
        ALC10.alcDestroyContext(context)
        ALC10.alcCloseDevice(device)
    }
}