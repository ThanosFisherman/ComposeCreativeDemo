package io.github.thanosfisherman.demo

import java.io.BufferedInputStream
import java.net.URI
import javax.sound.sampled.AudioSystem
import kotlin.test.Test
import kotlin.test.assertTrue

class SoundPlayerTest {
    @Test
    fun testResourceStream() {
        val resourcePath = "composeResources/composecreativedemo.shared.generated.resources/files/sounds/bass_tone.wav"
        val stream = SoundPlayer::class.java.classLoader.getResourceAsStream(resourcePath)
        assertTrue(stream != null, "Stream should not be null")
        val audioIn = AudioSystem.getAudioInputStream(BufferedInputStream(stream))
        val format = audioIn.format
        val data = audioIn.readAllBytes()
        println("[DEBUG_LOG] bass_tone format=$format, bytes=${data.size}, durationSec=${data.size.toDouble() / (format.sampleRate * format.frameSize)}")
        assertTrue(data.isNotEmpty(), "Audio data should not be empty")
    }

    @Test
    fun testResourceUrlStream() {
        val resourcePath = "composeResources/composecreativedemo.shared.generated.resources/files/sounds/tone3.wav"
        val url = SoundPlayer::class.java.classLoader.getResource(resourcePath)
        assertTrue(url != null, "URL should not be null")
        val uriString = url.toURI().toString()
        val stream = URI.create(uriString).toURL().openStream()
        val audioIn = AudioSystem.getAudioInputStream(BufferedInputStream(stream))
        val data = audioIn.readAllBytes()
        assertTrue(data.isNotEmpty(), "Audio data should not be empty")
    }

    @Test
    fun testMultipleSoundPlayersPlayback() {
        val p1 = SoundPlayer()
        val p2 = SoundPlayer()
        val p3 = SoundPlayer()
        p1.init()
        p2.init()
        p3.init()
        val uriBass = "composeResources/composecreativedemo.shared.generated.resources/files/sounds/bass_tone.wav"
        val uriTone = "composeResources/composecreativedemo.shared.generated.resources/files/sounds/tone2.wav"
        val id1 = p1.loadSound(uriTone)
        val id3 = p3.loadSound(uriBass)
        println("[DEBUG_LOG] id1=$id1, id3=$id3")
        for (i in 0 until 18) {
            p1.play(id1, 1f, 1f)
        }
        for (i in 0 until 6) {
            p3.play(id3, 1f, 1f)
        }
        Thread.sleep(500)
        p1.dispose()
        p2.dispose()
        p3.dispose()
    }
}
