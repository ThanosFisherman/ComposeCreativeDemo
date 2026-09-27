package io.github.thanosfisherman.demo.audioUtils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AudioManagerTest {

    private class FakeSound : Sound {
        var initialized = false
        var disposed = false
        val loadedPaths = mutableListOf<String>()
        val played = mutableListOf<Triple<Int, Float, Float>>()
        private var nextBufferId = 1

        override fun init() {
            initialized = true
        }

        override fun loadSound(path: String): Int {
            loadedPaths.add(path)
            return nextBufferId++
        }

        override fun play(bufferId: Int, volume: Float, pitch: Float) {
            played.add(Triple(bufferId, volume, pitch))
        }

        override fun dispose() {
            disposed = true
        }
    }

    private class FakeMusic : Music {
        var initialized = false
        var loadedPath: String? = null
        var isPlaying = false
        var isPaused = false
        var disposed = false
        var playVolume: Float = 1f
        var playLoop: Boolean = true

        override fun init() {
            initialized = true
        }

        override fun load(path: String) {
            loadedPath = path
        }

        override fun play(loop: Boolean, volume: Float) {
            isPlaying = true
            playLoop = loop
            playVolume = volume
        }

        override fun setVolume(volume: Float) {
            playVolume = volume
        }

        override fun pause() {
            isPaused = true
        }

        override fun resume() {
            isPaused = false
        }

        override fun stop() {
            isPlaying = false
        }

        override fun update() {}

        override fun dispose() {
            disposed = true
        }
    }

    private class FakeAudioPlayerFactory : AudioPlayerFactory {
        val createdSounds = mutableListOf<FakeSound>()
        val createdMusics = mutableListOf<FakeMusic>()

        override fun createSoundPlayer(): Sound {
            return FakeSound().also { createdSounds.add(it) }
        }

        override fun createMusicPlayer(): Music {
            return FakeMusic().also { createdMusics.add(it) }
        }
    }

    @Test
    fun testAudioManagerInitializationAndPlayback() {
        val factory = FakeAudioPlayerFactory()
        val manager = AudioManager(factory)

        manager.init()

        assertEquals(SoundTrack.entries.size, factory.createdSounds.size)
        assertEquals(MusicTrack.entries.size, factory.createdMusics.size)

        assertTrue(factory.createdSounds.all { it.initialized })
        assertTrue(factory.createdMusics.all { it.initialized && it.loadedPath != null })

        manager.playSound(SoundTrack.Swarm, volume = 0.62f, pitch = 1.5f)
        val swarmSound = factory.createdSounds[SoundTrack.entries.indexOf(SoundTrack.Swarm)]
        assertEquals(1, swarmSound.played.size)
        assertEquals(0.62f, swarmSound.played.first().second)
        assertEquals(1.5f, swarmSound.played.first().third)

        manager.playMusic(MusicTrack.Pendulums, volume = 0.5f)
        val pendulumsMusic = factory.createdMusics[MusicTrack.entries.indexOf(MusicTrack.Pendulums)]
        val ballsMusic = factory.createdMusics[MusicTrack.entries.indexOf(MusicTrack.BouncingBalls)]
        assertTrue(pendulumsMusic.isPlaying)
        assertEquals(0.5f, pendulumsMusic.playVolume)
        assertFalse(ballsMusic.isPlaying)

        manager.playMusic(MusicTrack.BouncingBalls, volume = 0.8f)
        assertTrue(ballsMusic.isPlaying)
        assertFalse(pendulumsMusic.isPlaying)

        manager.stopAllMusic()
        assertFalse(ballsMusic.isPlaying)
        assertFalse(pendulumsMusic.isPlaying)

        manager.dispose()
        assertTrue(factory.createdSounds.all { it.disposed })
        assertTrue(factory.createdMusics.all { it.disposed })
    }
}
