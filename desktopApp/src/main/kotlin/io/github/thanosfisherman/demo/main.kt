package io.github.thanosfisherman.demo

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import demo.shared.generated.resources.Res
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
    ) {
        BouncingBallsInVGame(ballCount = 16, onBounce = { freq ->
            sound.play(id, 0.38f, freq)
        })
    }
}