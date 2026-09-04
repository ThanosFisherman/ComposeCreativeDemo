package io.github.thanosfisherman.demo

import io.github.thanosfisherman.demo.audioUtils.Sound

@OptIn(ExperimentalWasmJsInterop::class)
class SoundPlayer : Sound {

    private val howls = ArrayList<Howl>()
    private val pathToId = HashMap<String, Int>()

    override fun init() {
        // no-op — see MusicPlayer.init()
    }


    override fun loadSound(path: String): Int {
        pathToId[path]?.let { return it }
        val howl = Howl(howlOptions(src = path, loop = false, volume = 1.0))
        val id = howls.size
        howls.add(howl)
        pathToId[path] = id
        return id
    }

    /**
     * Plays a short sound effect.
     * @param volume linear gain, 1.0 = normal
     * @param pitch playback rate multiplier. Howler clamps this to 0.5–4.0 internally.
     */
    override fun play(bufferId: Int, volume: Float, pitch: Float) {
        val howl = howls.getOrNull(bufferId) ?: return
        val instanceId = howl.play()
        howl.volume(vol = volume.toDouble().coerceAtLeast(0.0), id = instanceId)
        howl.rate(rate = pitch.toDouble().coerceIn(0.5, 4.0), id = instanceId)
    }

    override fun dispose() {
        howls.forEach { it.unload() }
        howls.clear()
        pathToId.clear()
    }
}