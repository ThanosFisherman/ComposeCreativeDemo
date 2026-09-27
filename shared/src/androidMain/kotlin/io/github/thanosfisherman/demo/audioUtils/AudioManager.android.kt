package io.github.thanosfisherman.demo.audioUtils

import android.content.Context

class AndroidAudioPlayerFactory(private val context: Context) : AudioPlayerFactory {
    override fun createSoundPlayer(): Sound = SoundPlayer(context.applicationContext)
    override fun createMusicPlayer(): Music = MusicPlayer(context.applicationContext)
}

private lateinit var audioManagerAndroid: AudioManager

fun initializeAudioManager(context: Context) {
    if (::audioManagerAndroid.isInitialized) {
        audioManagerAndroid.dispose()
    }
    audioManagerAndroid = AudioManager(
        AndroidAudioPlayerFactory(context.applicationContext)
    )
}

actual fun getAudioManager(): AudioManager = audioManagerAndroid