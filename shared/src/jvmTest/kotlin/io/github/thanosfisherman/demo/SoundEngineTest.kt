package io.github.thanosfisherman.demo

import java.io.BufferedInputStream
import javax.sound.sampled.AudioSystem
import kotlin.test.Test
import kotlin.test.assertTrue

class SoundEngineTest {
    @Test
    fun testResourceStream() {
        val stream = SoundEngine::class.java.classLoader.getResourceAsStream("composeResources/demo.shared.generated.resources/files/sounds/beep.wav")
        assertTrue(stream != null, "Stream should not be null")
        val audioIn = AudioSystem.getAudioInputStream(BufferedInputStream(stream))
        val data = audioIn.readAllBytes()
        assertTrue(data.isNotEmpty(), "Audio data should not be empty")
    }
}
