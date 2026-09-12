package io.github.thanosfisherman.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import composecreativedemo.shared.generated.resources.Res
import io.github.thanosfisherman.demo.audioUtils.Music
import io.github.thanosfisherman.demo.audioUtils.Sound

fun main() = application {

    val sound: Sound = remember { SoundPlayer() }
    val ballsMusic: Music = remember { MusicPlayer() }
    val padsMusic: Music = remember { MusicPlayer() }
    val allMusic = remember { listOf(ballsMusic, padsMusic) }
    var soundId by remember { mutableIntStateOf(-1) }
    val toneUri = Res.getUri("files/sounds/Tone2.wav")
    val ballsSongUri = Res.getUri("files/music/Epic_Ballz_backing.ogg")
    val padsSongUri = Res.getUri("files/music/PadsEMaj.ogg")

    LaunchedEffect(Unit) {
        sound.init()
        ballsMusic.init()
        padsMusic.init()
        soundId = sound.loadSound(toneUri)
        ballsMusic.load(ballsSongUri)
        padsMusic.load(padsSongUri)
    }

    fun playOnly(active: Music, loop: Boolean = true, volume: Float = 0.8f) {
        allMusic.forEach { track -> if (track !== active) track.stop() }
        active.play(loop = loop, volume = volume)
    }

    Window(
        onCloseRequest = { exitApplication(); sound.dispose(); allMusic.forEach { it.dispose() } },
        title = "Balls of fury",
        onKeyEvent = { event ->
            if (event.key == Key.Escape && event.type == KeyEventType.KeyUp) {
                exitApplication()
                true
            } else false
        }
    ) {
        MaterialTheme {
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
                                sound.play(soundId, 0.35f, freq)
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
    }
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
                color = Color.White,
                text = "Click to Start",
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Preview
@Composable
fun BouncingBallsInVGamePreview() {
    BouncingBallsInVGame()
}