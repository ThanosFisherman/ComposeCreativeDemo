package io.github.thanosfisherman.demo

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import demo.shared.generated.resources.Res
import io.github.thanosfisherman.demo.audioUtils.Sound

fun main() = application {
    val uri = Res.getUri("files/sounds/Tone2.wav")
    val sound: Sound = SoundEngine()
    sound.init()
    val id = sound.loadSound(uri)
    Window(
        onCloseRequest = { exitApplication(); sound.dispose() },
        title = "demo",
    ) {
        BouncingBallsInVGame(ballCount = 13, onBounce = { freq ->
            sound.play(id, 0.58f, freq)
        })
    }
}