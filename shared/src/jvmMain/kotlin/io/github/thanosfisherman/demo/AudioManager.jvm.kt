package io.github.thanosfisherman.demo

import io.github.thanosfisherman.demo.audioUtils.Music
import io.github.thanosfisherman.demo.audioUtils.Sound


class AudioManagerJvm : AudioManager {

    private val bouncingBallsSoundPlayer = SoundPlayer()
    private val pendulumSoundPlayer = SoundPlayer()
    private val bigBallsPendulumSoundPlayer = SoundPlayer()
    private val swarmSoundPlayer = SoundPlayer()

    private val ballsMusicPlayer = MusicPlayer()
    private val pendulumsMusicPlayer = MusicPlayer()

    private var bouncingBallsSoundId = -1
    private var pendulumSoundId = -1
    private var bigBallsPendulumSoundId = -1
    private var swarmSoundId = -1

    private var initialized = false
    override val allMusic: List<Music>
        get() = listOf(ballsMusicPlayer, pendulumsMusicPlayer)
    override val allSounds: List<Sound>
        get() = listOf(bouncingBallsSoundPlayer, pendulumSoundPlayer, bigBallsPendulumSoundPlayer, swarmSoundPlayer)

    override fun init() {
        if (initialized) return

        bouncingBallsSoundPlayer.init()
        pendulumSoundPlayer.init()
        bigBallsPendulumSoundPlayer.init()
        swarmSoundPlayer.init()

        ballsMusicPlayer.init()
        pendulumsMusicPlayer.init()

        val ballsToneUri = bouncingBallsToneUri
        val pendulumsUri = pendulumsToneUri
        val bigPendulumToneUri = bassToneUri
        val swarmUri = swarmToneUri

        bouncingBallsSoundId = bouncingBallsSoundPlayer.loadSound(ballsToneUri)
        pendulumSoundId = pendulumSoundPlayer.loadSound(pendulumsUri)
        bigBallsPendulumSoundId = bigBallsPendulumSoundPlayer.loadSound(bigPendulumToneUri)
        swarmSoundId = swarmSoundPlayer.loadSound(swarmUri)

        ballsMusicPlayer.load(bouncingBallsMusicUri)
        pendulumsMusicPlayer.load(pendulumsMusicUri)

        initialized = true
    }

    override fun getBouncingBallsSound(): Pair<Int, Sound> =
        bouncingBallsSoundId to bouncingBallsSoundPlayer

    override fun getSwarmSound(): Pair<Int, Sound> =
        swarmSoundId to swarmSoundPlayer

    override fun getBouncingBallsMusic(): Music =
        ballsMusicPlayer

    override fun getPendulumsSound(): Pair<Int, Sound> =
        pendulumSoundId to pendulumSoundPlayer

    override fun getBigBallsPendulumsSound(): Pair<Int, Sound> =
        bigBallsPendulumSoundId to bigBallsPendulumSoundPlayer

    override fun getPendulumsMusic(): Music =
        pendulumsMusicPlayer

    override fun dispose() {
        allSounds.forEach { it.dispose() }
        allMusic.forEach { it.dispose() }
    }
}

private val audioManagerJvm by lazy {
    AudioManagerJvm()
}

actual fun getAudioManager(): AudioManager = audioManagerJvm