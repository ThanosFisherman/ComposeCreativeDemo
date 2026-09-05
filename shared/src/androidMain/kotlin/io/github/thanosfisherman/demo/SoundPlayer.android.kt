package io.github.thanosfisherman.demo

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.media.AudioAttributes
import android.media.SoundPool
import io.github.thanosfisherman.demo.audioUtils.Sound

class SoundPlayer(private val context: Context) : Sound {

    private lateinit var soundPool: SoundPool
    private val loadedIds = HashMap<String, Int>()

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
        soundPool.play(soundId, vol, vol, 1, 0, rate)
    }

    override fun dispose() {
        soundPool.release()
        loadedIds.clear()
    }
}

/**
 * Converts a Compose Multiplatform "file:///android_asset/..." resource URI into
 * the relative path AssetManager expects (no scheme, no leading "android_asset/").
 */
internal fun toAssetPath(resUri: String): String =
    resUri.removePrefix("file:///android_asset/")