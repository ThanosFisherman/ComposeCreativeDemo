package io.github.thanosfisherman.demo.audioUtils

class AudioManager(
    private val factory: AudioPlayerFactory
) : Disposable {

    private var initialized = false

    private val soundEffects: Map<SoundTrack, SoundEffect> by lazy {
        SoundTrack.entries.associateWith { track ->
            SoundEffect(track.uri, factory.createSoundPlayer())
        }
    }

    private val musicPlayers: Map<MusicTrack, Music> by lazy {
        MusicTrack.entries.associateWith { factory.createMusicPlayer() }
    }

    val allSounds: List<Sound>
        get() = soundEffects.values.map { it.player }

    val allMusic: List<Music>
        get() = musicPlayers.values.toList()

    fun init() {
        if (initialized) return
        soundEffects.values.forEach { it.init() }
        musicPlayers.forEach { (track, music) ->
            music.init()
            music.load(track.uri)
        }
        initialized = true
    }

    fun playSound(track: SoundTrack, volume: Float = 1f, pitch: Float = 1f) {
        soundEffects[track]?.play(volume, pitch)
    }

    fun getSound(track: SoundTrack): SoundEffect? = soundEffects[track]

    fun playMusic(
        track: MusicTrack,
        loop: Boolean = true,
        volume: Float = 0.8f,
        stopOthers: Boolean = true
    ) {
        if (stopOthers) {
            stopAllMusic(except = track)
        }
        musicPlayers[track]?.play(loop = loop, volume = volume)
    }

    fun getMusic(track: MusicTrack): Music? = musicPlayers[track]

    fun stopAllMusic(except: MusicTrack? = null) {
        musicPlayers.forEach { (track, music) ->
            if (track != except) {
                music.stop()
            }
        }
    }

    fun pauseAllMusic() {
        musicPlayers.values.forEach { it.pause() }
    }

    fun resumeAllMusic() {
        musicPlayers.values.forEach { it.resume() }
    }

    override fun dispose() {
        soundEffects.values.forEach { it.dispose() }
        musicPlayers.values.forEach { it.dispose() }
        initialized = false
    }
}

expect fun getAudioManager(): AudioManager