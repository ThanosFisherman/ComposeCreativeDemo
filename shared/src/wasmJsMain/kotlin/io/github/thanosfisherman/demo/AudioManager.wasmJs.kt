package io.github.thanosfisherman.demo

import io.github.thanosfisherman.demo.audioUtils.Music
import io.github.thanosfisherman.demo.audioUtils.Sound

class AudioManagerWasm : AudioManager {

    private val bouncingBallsSound = SoundPlayer()
    private val pendulumSound = SoundPlayer()
    private val bigBallsPendulumSound = SoundPlayer()

    private val ballsMusic = MusicPlayer()
    private val pendulumsMusic = MusicPlayer()

    private var bouncingBallsSoundId = -1
    private var pendulumSoundId = -1
    private var bigBallsPendulumSoundId = -1

    private var initialized = false
    override val allMusic: List<Music>
        get() = listOf(ballsMusic, pendulumsMusic)
    override val allSounds: List<Sound>
        get() = listOf(bouncingBallsSound, pendulumSound, bigBallsPendulumSound)

    override fun init() {
        if (initialized) return

        bouncingBallsSound.init()
        pendulumSound.init()
        bigBallsPendulumSound.init()

        ballsMusic.init()
        pendulumsMusic.init()

        val ballsToneUri = bouncingBallsToneUri
        val pendulumsUri = pendulumsToneUri
        val bigPendulumToneUri = bassToneUri

        bouncingBallsSoundId = bouncingBallsSound.loadSound(ballsToneUri)
        pendulumSoundId = pendulumSound.loadSound(pendulumsUri)
        bigBallsPendulumSoundId = bigBallsPendulumSound.loadSound(bigPendulumToneUri)

        ballsMusic.load(bouncingBallsMusicUri)

        pendulumsMusic.load(pendulumsMusicUri)

        initialized = true
    }

    override fun getBouncingBallsSound(): Pair<Int, Sound> =
        bouncingBallsSoundId to bouncingBallsSound

    override fun getBouncingBallsMusic(): Music =
        ballsMusic

    override fun getPendulumsSound(): Pair<Int, Sound> =
        pendulumSoundId to pendulumSound

    override fun getBigBallsPendulumsSound(): Pair<Int, Sound> =
        bigBallsPendulumSoundId to bigBallsPendulumSound

    override fun getPendulumsMusic(): Music =
        pendulumsMusic

    override fun dispose() {
        allSounds.forEach { it.dispose() }
        allMusic.forEach { it.dispose() }
    }
}

private val audioManagerWasm by lazy {
    AudioManagerWasm()
}

actual fun getAudioManager(): AudioManager = audioManagerWasm