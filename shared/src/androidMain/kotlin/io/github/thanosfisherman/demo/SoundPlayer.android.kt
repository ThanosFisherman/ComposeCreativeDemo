package io.github.thanosfisherman.demo

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.media.AudioAttributes
import android.media.SoundPool
import io.github.thanosfisherman.demo.audioUtils.Sound
import java.util.concurrent.Executors

class SoundPlayer(private val context: Context) : Sound {
    private lateinit var soundPool: SoundPool
    private val loadedIds = HashMap<String, Int>()

    // `play()` gets called synchronously from inside the game loop's withFrameNanos callback,
    // which runs on the main/Choreographer thread — same thread responsible for stepping
    // physics and committing each frame's draw. SoundPool.play() is a JNI call into native
    // audio code (stream allocation, mixer bookkeeping); even a couple of milliseconds of
    // that, happening once per bounce with many balls in flight, is latency stolen straight
    // out of the frame budget. Dispatching the actual play() call onto a dedicated background
    // thread means worst-case native audio latency can never block a frame, at the cost of a
    // (usually sub-millisecond, and totally inaudible) delay before the sound actually starts.
    private val playbackExecutor = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "SoundPlayer-playback").apply { isDaemon = true }
    }

    override fun init() {
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder()
            .setMaxStreams(16)
            .setAudioAttributes(attributes)
            .build()
    }

    /**
     * Loads a short sound effect.
     * @param path a Compose Multiplatform resource URI from Res.getUri(...),
     *             e.g. "file:///android_asset/composeResources/.../files/sounds/Tone3.wav"
     */
    override fun loadSound(path: String): Int {
        loadedIds[path]?.let { return it }
        val afd: AssetFileDescriptor = context.assets.openFd(toAssetPath(path))
        val soundId = soundPool.load(afd, 1)
        afd.close()
        loadedIds[path] = soundId
        return soundId
    }

    override fun play(soundId: Int, volume: Float, pitch: Float) {
        val vol = volume.coerceAtLeast(0f)
        val rate = pitch.coerceIn(0.5f, 2f)
        // Off the main thread — see the comment on playbackExecutor above. SoundPool itself is
        // thread-safe for play() calls (its own native mixing already happens off-thread; this
        // just moves the *call itself* off the thread that's also driving your frame clock).
        playbackExecutor.execute {
            soundPool.play(soundId, vol, vol, 1, 0, rate)
        }
    }

    override fun dispose() {
        soundPool.release()
        loadedIds.clear()
        playbackExecutor.shutdown()
    }
}

/**
 * Converts a Compose Multiplatform "file:///android_asset/..." resource URI into
 * the relative path AssetManager expects (no scheme, no leading "android_asset/").
 */
internal fun toAssetPath(resUri: String): String =
    resUri.removePrefix("file:///android_asset/")