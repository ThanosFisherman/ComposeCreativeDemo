package io.github.thanosfisherman.demo.audioUtils

object WasmAudioPlayerFactory : AudioPlayerFactory {
    override fun createSoundPlayer(): Sound = SoundPlayer()
    override fun createMusicPlayer(): Music = MusicPlayer()
}

private val audioManagerWasm by lazy {
    AudioManager(WasmAudioPlayerFactory)
}

actual fun getAudioManager(): AudioManager = audioManagerWasm