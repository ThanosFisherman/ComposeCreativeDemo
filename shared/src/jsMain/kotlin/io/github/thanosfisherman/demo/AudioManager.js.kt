package io.github.thanosfisherman.demo

import io.github.thanosfisherman.demo.audioUtils.Music
import io.github.thanosfisherman.demo.audioUtils.Sound


class AudioManagerJs : AudioManager {
    override val allMusic: List<Music>
        get() = TODO("Not yet implemented")
    override val allSounds: List<Sound>
        get() = TODO("Not yet implemented")

    override fun init() {
        TODO("Not yet implemented")
    }

    override fun getBouncingBallsSound(): Pair<Int, Sound> {
        TODO("Not yet implemented")
    }

    override fun getBouncingBallsMusic(): Music {
        TODO("Not yet implemented")
    }

    override fun getPendulumsSound(): Pair<Int, Sound> {
        TODO("Not yet implemented")
    }

    override fun getBigBallsPendulumsSound(): Pair<Int, Sound> {
        TODO("Not yet implemented")
    }

    override fun getPendulumsMusic(): Music {
        TODO("Not yet implemented")
    }

}

private val audioManagerJs by lazy<AudioManager> {
    AudioManagerJs()
}

actual fun getAudioManager(): AudioManager = audioManagerJs