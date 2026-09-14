package io.github.thanosfisherman.demo

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.media.AudioAttributes
import android.media.SoundPool
import io.github.thanosfisherman.demo.audioUtils.Sound
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

class SoundPlayer(private val context: Context) : Sound {

    companion object {
        private const val MAX_STREAMS = 32
        private const val PRIORITY = 1
    }

    private lateinit var soundPool: SoundPool
    private val loadedIds = HashMap<String, Int>()

    private val playbackExecutor = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "SoundPlayer-playback").apply { isDaemon = true }
    }

    private val disposed = AtomicBoolean(false)

    override fun init() {
        if (::soundPool.isInitialized) return

        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(MAX_STREAMS)
            .setAudioAttributes(attributes)
            .build()
    }

    /**
     * Loads a short sound effect.
     *
     * @param path a Compose Multiplatform resource URI from Res.getUri(...),
     *             e.g. "file:///android_asset/composeResources/.../files/sounds/Tone3.wav"
     */
    override fun loadSound(path: String): Int {
        check(!disposed.get()) { "SoundPlayer has been disposed" }

        loadedIds[path]?.let { return it }

        val afd: AssetFileDescriptor =
            context.assets.openFd(toAssetPath(path))

        return afd.use { afd ->
            val soundId = soundPool.load(afd, PRIORITY)
            loadedIds[path] = soundId
            soundId
        }
    }

    override fun play(bufferId: Int, volume: Float, pitch: Float) {
        if (disposed.get()) return

        val vol = volume.coerceAtLeast(0f)
        val rate = pitch.coerceIn(0.5f, 2f)

        playbackExecutor.execute {
            if (disposed.get()) return@execute

            soundPool.play(
                bufferId,
                vol,
                vol,
                PRIORITY,
                0,
                rate
            )
        }
    }

    override fun dispose() {
        if (!disposed.compareAndSet(false, true)) return

        playbackExecutor.shutdown()

        if (::soundPool.isInitialized) {
            soundPool.release()
        }

        loadedIds.clear()
    }
}

/**
 * Converts a Compose Multiplatform "file:///android_asset/..." resource URI
 * into the relative path expected by AssetManager.
 */
internal fun toAssetPath(resUri: String): String =
    resUri.removePrefix("file:///android_asset/")