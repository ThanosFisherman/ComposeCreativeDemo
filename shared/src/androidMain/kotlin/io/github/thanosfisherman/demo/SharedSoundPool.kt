package io.github.thanosfisherman.demo

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.media.AudioAttributes
import android.media.SoundPool
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * One shared SoundPool for the whole app, instead of one per SoundPlayer/category.
 *
 * SoundPool.setMaxStreams() only requests a SOFTWARE-side cap — it does not guarantee that many
 * streams are actually mixed to audible output. Android routes SoundPool playback through the
 * platform's audio mixer (AudioFlinger), whose real polyphony ceiling is often much lower than
 * requested, and — critically — that ceiling is shared across the whole PROCESS, not per
 * SoundPool instance. Multiple independent SoundPool objects (one per sound category) each
 * believe they have their own full budget of free slots, with no visibility into what the
 * others are using, so they can all simultaneously think they have room while the device's true
 * shared ceiling is already exhausted — sounds get silently dropped in a way that looks
 * arbitrary. Routing every sound effect in the app through one shared pool means priority-based
 * stream stealing is at least deciding against the REAL available budget, not a fragmented,
 * inaccurate view of it. This can't exceed whatever hardware ceiling the device actually has —
 * nothing in software can — but it stops that ceiling from being wasted across uncoordinated pools.
 */
internal object SharedSoundPool {
    private const val MAX_STREAMS = 32
    private const val DEFAULT_PRIORITY = 1

    private var soundPool: SoundPool? = null
    private var refCount = 0

    private val loadedIds = HashMap<String, Int>()

    /** One thread for every actual play() call, across every SoundPlayer sharing this pool —
     *  same reasoning as OpenALAudioEngine's shared executor: keeps calls off the caller's
     *  thread (which may be the frame/main thread) without needing a separate thread per category. */
    val executor: ExecutorService = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "SharedSoundPool-playback").apply { isDaemon = true }
    }

    @Synchronized
    fun retain() {
        if (refCount == 0) {
            val attributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            soundPool = SoundPool.Builder()
                .setMaxStreams(MAX_STREAMS)
                .setAudioAttributes(attributes)
                .build()
        }
        refCount++
    }

    @Synchronized
    fun loadSound(context: Context, path: String): Int {
        loadedIds[path]?.let { return it }

        val pool = soundPool ?: error("SharedSoundPool not initialized — call retain() first")
        val afd: AssetFileDescriptor = context.assets.openFd(toAssetPath(path))
        return afd.use { descriptor ->
            val id = pool.load(descriptor, DEFAULT_PRIORITY)
            loadedIds[path] = id
            id
        }
    }

    fun play(bufferId: Int, volume: Float, pitch: Float) {
        val pool = soundPool ?: return
        val vol = volume.coerceAtLeast(0f)
        val rate = pitch.coerceIn(0.5f, 2f)

        executor.execute {
            pool.play(bufferId, vol, vol, DEFAULT_PRIORITY, 0, rate)
        }
    }

    @Synchronized
    fun release() {
        if (refCount > 0) {
            refCount--
            if (refCount == 0) {
                val pool = soundPool
                executor.execute {
                    pool?.release()
                }
                soundPool = null
                loadedIds.clear()
            }
        }
    }
}

/**
 * Converts a Compose Multiplatform "file:///android_asset/..." resource URI
 * into the relative path expected by AssetManager.
 */
internal fun toAssetPath(resUri: String): String =
    resUri.removePrefix("file:///android_asset/")