package io.github.thanosfisherman.demo

import io.github.thanosfisherman.demo.audioUtils.Music

@OptIn(ExperimentalWasmJsInterop::class)
class MusicPlayer : Music {

    private var howl: Howl? = null
    private var playId: Double = -1.0

    override fun init() {
        // Howler creates its shared AudioContext lazily on first play() — nothing to set up.
    }

    override fun load(path: String) {
        stop()
        howl = Howl(howlOptions(src = path, loop = false, volume = 1.0))
    }

    override fun play(loop: Boolean, volume: Float) {
        val h = howl ?: error("No track loaded — call load() first")
        h.loop(loop = loop)
        h.volume(vol = volume.toDouble().coerceAtLeast(0.0))
        playId = h.play()
    }

    override fun update() {
        // Streaming/decoding happens inside the browser's own audio pipeline —
        // nothing to pump per-frame here, unlike the JVM stb_vorbis path.
    }

    override fun setVolume(volume: Float) {
        howl?.volume(vol = volume.toDouble().coerceAtLeast(0.0))
    }

    override fun pause() {
        val h = howl ?: return
        if (playId >= 0.0) h.pause(id = playId) else h.pause()
    }

    override fun resume() {
        val h = howl ?: return
        playId = if (playId >= 0.0) h.play(id = playId) else h.play()
    }

    override fun stop() {
        howl?.stop()
        playId = -1.0
    }

    override fun dispose() {
        howl?.unload()
        howl = null
    }
}