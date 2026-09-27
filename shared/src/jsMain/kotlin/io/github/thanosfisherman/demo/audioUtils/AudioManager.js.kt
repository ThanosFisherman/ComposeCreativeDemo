package io.github.thanosfisherman.demo.audioUtils

object JsAudioPlayerFactory : AudioPlayerFactory {
    override fun createSoundPlayer(): Sound = object : Sound {
        override fun init() {}
        override fun loadSound(path: String): Int = 0
        override fun play(bufferId: Int, volume: Float, pitch: Float) {}
        override fun dispose() {}
    }

    override fun createMusicPlayer(): Music = object : Music {
        override fun init() {}
        override fun load(path: String) {}
        override fun play(loop: Boolean, volume: Float) {}
        override fun setVolume(volume: Float) {}
        override fun pause() {}
        override fun resume() {}
        override fun stop() {}
        override fun update() {}
        override fun dispose() {}
    }
}

private val audioManagerJs by lazy {
    AudioManager(JsAudioPlayerFactory)
}

actual fun getAudioManager(): AudioManager = audioManagerJs