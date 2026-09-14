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
import java.util.concurrent.Executors
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem

class SoundPlayer(
    sourcePoolSize: Int = 24
) : Sound {

    private var device: Long = 0
    private var context: Long = 0

    private val sourcePool = IntArray(sourcePoolSize.coerceAtLeast(1))
    private var nextSource = 0

    private val bufferCache = HashMap<String, Int>()

    private val playbackExecutor = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "SoundPlayer-playback").apply {
            isDaemon = true
        }
    }

    override fun init() {
        val defaultDeviceName = ALC10.alcOpenDevice(null as CharSequence?)
        device = defaultDeviceName

        require(device != 0L) {
            "Failed to open default OpenAL device"
        }

        val attribs = intArrayOf(0)

        context = ALC10.alcCreateContext(device, attribs)
        require(context != 0L) {
            "Failed to create OpenAL context"
        }

        ALC10.alcMakeContextCurrent(context)

        val alcCapabilities = ALC.createCapabilities(device)
        AL.createCapabilities(alcCapabilities)

        for (i in sourcePool.indices) {
            sourcePool[i] = alGenSources()
        }
    }

    /**
     * Loads a mono/stereo PCM WAV file into an AL buffer.
     * Cached by path.
     */
    override fun loadSound(path: String): Int {
        bufferCache[path]?.let { return it }

        val audioIn = run {
            val looksLikeUrl =
                path.contains("://") ||
                        path.startsWith("jar:") ||
                        path.startsWith("file:")

            val stream: InputStream =
                if (looksLikeUrl) {
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
                            "composeResources/composecreativedemo.shared.generated.resources/files/sounds/${
                                cleanPath.substringAfterLast(
                                    '/'
                                )
                            }",
                            "composeResources/composecreativedemo.shared.generated.resources/sounds/${
                                cleanPath.substringAfterLast(
                                    '/'
                                )
                            }",
                            "composeResources/demo.shared.generated.resources/$cleanPath",
                            "composeResources/demo.shared.generated.resources/files/$cleanPath",
                            "composeResources/demo.shared.generated.resources/files/sounds/${
                                cleanPath.substringAfterLast(
                                    '/'
                                )
                            }",
                            "composeResources/demo.shared.generated.resources/sounds/${cleanPath.substringAfterLast('/')}",
                            "files/$cleanPath",
                            "sounds/${cleanPath.substringAfterLast('/')}",
                            cleanPath.substringAfterLast('/')
                        ).distinct()

                        candidatePaths.firstNotNullOfOrNull { candidate ->
                            SoundPlayer::class.java.getResourceAsStream(
                                if (candidate.startsWith("/")) candidate else "/$candidate"
                            )
                                ?: SoundPlayer::class.java.classLoader
                                    .getResourceAsStream(candidate.removePrefix("/"))
                                ?: Thread.currentThread().contextClassLoader
                                    .getResourceAsStream(candidate.removePrefix("/"))
                        }
                            ?: throw FileNotFoundException(
                                "Could not find audio file or resource: $path"
                            )
                    }
                }

            val bufferedStream =
                if (stream.markSupported()) stream
                else BufferedInputStream(stream)

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

        try {
            buffer.put(data).flip()

            val alBuffer = alGenBuffers()

            alBufferData(
                alBuffer,
                alFormat,
                buffer,
                format.sampleRate.toInt()
            )

            bufferCache[path] = alBuffer
            return alBuffer
        } finally {
            MemoryUtil.memFree(buffer)
            audioIn.close()
        }
    }

    /**
     * Plays a short sound effect.
     *
     * Reuses idle OpenAL sources whenever possible.
     * Only interrupts an active source when the entire pool is busy.
     *
     * @param volume linear gain, 1.0 = normal
     * @param pitch playback speed/pitch multiplier, 1.0 = normal
     */
    override fun play(
        bufferId: Int,
        volume: Float,
        pitch: Float
    ) {
        playbackExecutor.execute {
            val source = findAvailableSource()

            // Only stop if this source is still playing.
            // This is necessary when the pool is completely exhausted.
            if (alGetSourcei(source, AL_SOURCE_STATE) == AL_PLAYING) {
                alSourceStop(source)
            }

            alSourcei(source, AL_BUFFER, bufferId)
            alSourcef(source, AL_GAIN, volume.coerceAtLeast(0f))
            alSourcef(source, AL_PITCH, pitch.coerceIn(0.01f, 4f))

            alSourcePlay(source)
        }
    }

    /**
     * Finds a stopped/idle source first.
     * Falls back to round-robin reuse when every source is busy.
     */
    private fun findAvailableSource(): Int {
        // First pass: look for a source that has finished playing.
        for (source in sourcePool) {
            val state = alGetSourcei(source, AL_SOURCE_STATE)

            if (state != AL_PLAYING && state != AL_PAUSED) {
                return source
            }
        }

        // All sources are busy. Steal the next one in round-robin order.
        val source = sourcePool[nextSource]
        nextSource = (nextSource + 1) % sourcePool.size

        return source
    }

    override fun dispose() {
        playbackExecutor.execute {
            sourcePool.forEach { source ->
                alSourceStop(source)
                alDeleteSources(source)
            }

            bufferCache.values.forEach { buffer ->
                alDeleteBuffers(buffer)
            }

            bufferCache.clear()

            ALC10.alcMakeContextCurrent(0)
            ALC10.alcDestroyContext(context)
            ALC10.alcCloseDevice(device)
        }

        playbackExecutor.shutdown()
    }
}