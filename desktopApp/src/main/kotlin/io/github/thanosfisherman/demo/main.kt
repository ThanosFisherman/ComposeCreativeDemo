package io.github.thanosfisherman.demo

import androidx.compose.runtime.Composable
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import composecreativedemo.shared.generated.resources.Res
import io.github.thanosfisherman.demo.audioUtils.Music
import io.github.thanosfisherman.demo.audioUtils.Sound

fun main() = application {
    val uri = Res.getUri("files/sounds/Tone3.wav")
    val song = Res.getUri("files/music/Epic_Ballz_backing.ogg")
    val sound: Sound = SoundPlayer()
    val music: Music = MusicPlayer()
    sound.init() // need to call this before music to initialize global context capabilities (will fix later)
    music.init()
    val id = sound.loadSound(uri)
    music.load(song)
    music.play(loop = true, volume = 0.8f)
    Window(
        onCloseRequest = { exitApplication(); sound.dispose(); music.dispose() },
        title = "demo",
        onKeyEvent = { event ->
            if (event.key == Key.Escape && event.type == KeyEventType.KeyUp) {
                exitApplication()
                true
            } else false
        }
    ) {
        BouncingBallsInVGame(ballCount = 14, onBounce = { freq ->
            sound.play(id, 0.30f, freq)
        })
    }
}

@Preview
@Composable
fun BouncingBallsInVGamePreview() {
    BouncingBallsInVGame()
}