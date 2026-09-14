package io.github.thanosfisherman.demo

import composecreativedemo.shared.generated.resources.Res
import io.github.thanosfisherman.demo.audioUtils.Disposable
import io.github.thanosfisherman.demo.audioUtils.Music
import io.github.thanosfisherman.demo.audioUtils.Sound

interface AudioManager : Disposable{

    val bouncingBallsToneUri: String
        get() = Res.getUri("files/sounds/Tone2.wav")
    val pendulumsToneUri: String
        get() = Res.getUri("files/sounds/Tone2.wav")
    val bassToneUri: String
        get() = Res.getUri("files/sounds/Bass_Tone.wav")
    val bouncingBallsMusicUri: String
        get() = Res.getUri("files/music/Epic_Ballz_backing.ogg")
    val pendulumsMusicUri: String
        get() = Res.getUri("files/music/E_Pads.ogg")

    val allMusic: List<Music>
    val allSounds: List<Sound>
    fun init()
    fun getBouncingBallsSound(): Pair<Int, Sound>
    fun getBouncingBallsMusic(): Music
    fun getPendulumsSound(): Pair<Int, Sound>
    fun getBigBallsPendulumsSound(): Pair<Int, Sound>
    fun getPendulumsMusic(): Music
}

expect fun getAudioManager(): AudioManager