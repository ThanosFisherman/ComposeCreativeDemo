package io.github.thanosfisherman.demo.audioUtils

interface AudioPlayerFactory {
    fun createSoundPlayer(): Sound
    fun createMusicPlayer(): Music
}
