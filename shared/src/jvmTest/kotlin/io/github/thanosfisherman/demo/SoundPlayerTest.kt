package io.github.thanosfisherman.demo

import java.io.BufferedInputStream
import java.net.URI
import javax.sound.sampled.AudioSystem
import kotlin.test.Test
import kotlin.test.assertTrue

class SoundPlayerTest {
    @Test
    fun testResourceStream() {
        val resourcePath = "composeResources/composecreativedemo.shared.generated.resources/files/sounds/beep.wav"
        val stream = SoundPlayer::class.java.classLoader.getResourceAsStream(resourcePath)
        assertTrue(stream != null, "Stream should not be null")
        val audioIn = AudioSystem.getAudioInputStream(BufferedInputStream(stream))
        val data = audioIn.readAllBytes()
        assertTrue(data.isNotEmpty(), "Audio data should not be empty")
    }

    @Test
    fun testResourceUrlStream() {
        val resourcePath = "composeResources/composecreativedemo.shared.generated.resources/files/sounds/Tone3.wav"
        val url = SoundPlayer::class.java.classLoader.getResource(resourcePath)
        assertTrue(url != null, "URL should not be null")
        val uriString = url.toURI().toString()
        val stream = URI.create(uriString).toURL().openStream()
        val audioIn = AudioSystem.getAudioInputStream(BufferedInputStream(stream))
        val data = audioIn.readAllBytes()
        assertTrue(data.isNotEmpty(), "Audio data should not be empty")
    }
}
