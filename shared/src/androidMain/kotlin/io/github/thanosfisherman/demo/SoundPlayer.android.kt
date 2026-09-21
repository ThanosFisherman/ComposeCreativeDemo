package io.github.thanosfisherman.demo

import android.content.Context
import io.github.thanosfisherman.demo.audioUtils.Sound
import java.util.concurrent.atomic.AtomicBoolean

class SoundPlayer(private val context: Context) : Sound {

    private val disposed = AtomicBoolean(false)

    override fun init() {
        SharedSoundPool.retain()
    }

    /**
     * Loads a short sound effect.
     *
     * @param path a Compose Multiplatform resource URI from Res.getUri(...),
     *             e.g. "file:///android_asset/composeResources/.../files/sounds/tone3.wav"
     */
    override fun loadSound(path: String): Int {
        check(!disposed.get()) { "SoundPlayer has been disposed" }
        return SharedSoundPool.loadSound(context, path)
    }

    override fun play(bufferId: Int, volume: Float, pitch: Float) {
        if (disposed.get()) return
        SharedSoundPool.play(bufferId, volume, pitch)
    }

    override fun dispose() {
        if (!disposed.compareAndSet(false, true)) return
        SharedSoundPool.release()
    }
}