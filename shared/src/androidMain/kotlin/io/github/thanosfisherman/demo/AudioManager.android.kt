package io.github.thanosfisherman.demo

import android.content.Context
import io.github.thanosfisherman.demo.audioUtils.Music
import io.github.thanosfisherman.demo.audioUtils.Sound

class AudioManagerAndroid(
    context: Context
) : AudioManager {

    private val appContext = context.applicationContext

    private val bouncingBallsSound = SoundPlayer(appContext)
    private val pendulumSound = SoundPlayer(appContext)
    private val bigBallsPendulumSound = SoundPlayer(appContext)

    private val ballsMusic = MusicPlayer(appContext)
    private val pendulumsMusic = MusicPlayer(appContext)

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

        val toneUri = toneUri
        val bassToneUri = bassToneUri

        bouncingBallsSoundId = bouncingBallsSound.loadSound(toneUri)

        pendulumSoundId = pendulumSound.loadSound(toneUri)

        bigBallsPendulumSoundId = bigBallsPendulumSound.loadSound(bassToneUri)

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

private lateinit var audioManagerAndroid: AudioManagerAndroid

fun initializeAudioManager(context: Context) {
    if (::audioManagerAndroid.isInitialized) {
        audioManagerAndroid.dispose()
    }
    audioManagerAndroid = AudioManagerAndroid(
        context.applicationContext
    )
}

actual fun getAudioManager(): AudioManager = audioManagerAndroid