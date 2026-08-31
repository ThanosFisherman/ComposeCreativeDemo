package io.github.thanosfisherman.demo

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import demo.shared.generated.resources.Res
import io.github.thanosfisherman.demo.audioUtils.Sound

fun main() = application {
    val uri = Res.getUri("files/sounds/beep.wav")
    val sound: Sound = SoundEngine()
    sound.init()
    val id = sound.loadSound(uri)
    Window(
        onCloseRequest = { exitApplication(); sound.dispose() },
        title = "demo",
    ) {
        BouncingBallsInVGame(onBounce = { freq ->
            sound.play(id, 1f, freq)
        })
    }
}