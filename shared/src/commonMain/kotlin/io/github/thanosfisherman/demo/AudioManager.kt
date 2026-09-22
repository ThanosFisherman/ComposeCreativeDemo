package io.github.thanosfisherman.demo

import composecreativedemo.shared.generated.resources.Res
import io.github.thanosfisherman.demo.audioUtils.Disposable
import io.github.thanosfisherman.demo.audioUtils.Music
import io.github.thanosfisherman.demo.audioUtils.Sound

interface AudioManager : Disposable {

    val bouncingBallsToneUri: String
        get() = Res.getUri("files/sounds/vib1.wav")
    val pendulumsToneUri: String
        get() = Res.getUri("files/sounds/vib2.wav")
    val bassToneUri: String
        get() = Res.getUri("files/sounds/bass.wav")
    val swarmToneUri: String
        get() = Res.getUri("files/sounds/bells.wav")
    val bouncingBallsMusicUri: String
        get() = Res.getUri("files/music/epic_ballz_backing.ogg")
    val pendulumsMusicUri: String
        get() = Res.getUri("files/music/epads.ogg")

    val allMusic: List<Music>
    val allSounds: List<Sound>
    fun init()
    fun getBouncingBallsSound(): Pair<Int, Sound>
    fun getSwarmSound(): Pair<Int, Sound>
    fun getBouncingBallsMusic(): Music
    fun getPendulumsSound(): Pair<Int, Sound>
    fun getBigBallsPendulumsSound(): Pair<Int, Sound>
    fun getPendulumsMusic(): Music
}

expect fun getAudioManager(): AudioManager