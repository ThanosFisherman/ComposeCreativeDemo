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

    val toneUri = Res.getUri("files/sounds/Tone2.wav")
    val ballsSongUri = Res.getUri("files/music/Epic_Ballz_backing.ogg")
    val padsSongUri = Res.getUri("files/music/PadsEMaj.ogg")

    val sound: Sound = SoundPlayer()
    sound.init()
    val toneId = sound.loadSound(toneUri)
    val ballsMusic: Music = MusicPlayer().apply { init(); load(ballsSongUri) }
    val padsMusic: Music = MusicPlayer().apply { init(); load(padsSongUri) }
    val allMusic = listOf(ballsMusic, padsMusic)

    fun playOnly(active: Music, loop: Boolean = true, volume: Float = 0.8f) {
        allMusic.forEach { track -> if (track !== active) track.stop() }
        active.play(loop = loop, volume = volume)
    }

    ComposeViewport(document.body!!) {
        var started by remember { mutableStateOf(false) }
        if (!started) {
            StartOverlay(onStart = {
                started = true
            })
        } else {
            DemoNavigator(
                demos = listOf(
                    Demo("The balls of fury! - Thanos Psaridis") {
                        LaunchedEffect(Unit) { playOnly(ballsMusic) }
                        BouncingBallsInVGame(ballCount = 14, onBounce = { freq ->
                            sound.play(toneId, 0.35f, freq) // toneId was already loaded above — no per-call loading
                        })
                    },
                    Demo("Circle") {
                        LaunchedEffect(Unit) { playOnly(padsMusic) }
                        CircleDemo()
                    },
                )
            )
        }
    }

    window.addEventListener("beforeunload", { _: Event ->
        sound.dispose()
        allMusic.forEach { it.dispose() }
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
                pressedElevation = 4.dp
            ),
            contentPadding = PaddingValues(horizontal = 48.dp, vertical = 24.dp)
        ) {
            Text(
                text = "Click to Start",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.5.sp
            )
        }
    }
}