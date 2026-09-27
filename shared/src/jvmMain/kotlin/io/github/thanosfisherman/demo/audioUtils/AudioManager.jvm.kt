package io.github.thanosfisherman.demo.audioUtils

object JvmAudioPlayerFactory : AudioPlayerFactory {
    override fun createSoundPlayer(): Sound = SoundPlayer()
    override fun createMusicPlayer(): Music = MusicPlayer()
}

private val audioManagerJvm by lazy {
    AudioManager(JvmAudioPlayerFactory)
}

actual fun getAudioManager(): AudioManager = audioManagerJvm