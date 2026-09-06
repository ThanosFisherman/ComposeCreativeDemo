package io.github.thanosfisherman.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.ComposeViewport
import composecreativedemo.shared.generated.resources.Res
import io.github.thanosfisherman.demo.audioUtils.Music
import io.github.thanosfisherman.demo.audioUtils.Sound
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.events.Event

@OptIn(ExperimentalComposeUiApi::class)
fun main() {

    val uri = Res.getUri("files/sounds/Tone2.wav")
    val song = Res.getUri("files/music/Epic_Ballz_backing.ogg")
    val sound: Sound = SoundPlayer()
    val music: Music = MusicPlayer()
    sound.init()
    music.init()
    val id = sound.loadSound(uri)
    music.load(song)

    ComposeViewport(document.body!!) {
        var started by remember { mutableStateOf(false) }
        if (!started) {
            StartOverlay(onStart = {
                music.play(loop = true, volume = 0.8f)
                started = true
            })
        } else {
            BouncingBallsInVGame(ballCount = 14, onBounce = { freq ->
                sound.play(id, 0.35f, freq)
            })
        }
    }

    window.addEventListener("beforeunload", { _: Event ->
        sound.dispose()
        music.dispose()
    })
}


@Composable
fun StartOverlay(onStart: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Button(
            onClick = onStart,
            shape = RoundedCornerShape(50),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 6.dp,
                pressedElevation = 2.dp
            ),
            contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Click to Start",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.5.sp
            )
        }
    }
}