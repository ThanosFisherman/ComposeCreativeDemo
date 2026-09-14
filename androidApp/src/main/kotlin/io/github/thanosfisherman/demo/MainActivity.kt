package io.github.thanosfisherman.demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.keepScreenOn
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.thanosfisherman.demo.audioUtils.Music
import io.github.thanosfisherman.demo.ballsoffury.BouncingBallsInVGame
import io.github.thanosfisherman.demo.pendulums.PendulumsDemo

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        initializeAudioManager(this)
        val audioManager = getAudioManager()

        setContent {

            var started by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                audioManager.init()
            }

            fun playOnly(active: Music, loop: Boolean = true, volume: Float = 0.8f) {
                audioManager.allMusic.forEach { track -> if (track !== active) track.stop() }
                active.play(loop = loop, volume = volume)
            }

            DisposableEffect(Unit) {
                onDispose {
                    audioManager.allSounds.forEach { it.dispose() }
                    audioManager.allMusic.forEach { it.dispose() }
                }
            }

            MaterialTheme {
                if (!started) {
                    StartOverlay(onStart = {
                        started = true
                    })
                } else {
                    DemoNavigator(
                        demos = listOf(
                            Demo("The balls of Fury") {
                                LaunchedEffect(Unit) { playOnly(audioManager.getBouncingBallsMusic()) }
                                BouncingBallsInVGame(ballCount = 14, onBounce = { freq ->
                                    val id = audioManager.getBouncingBallsSound().first
                                    val sound = audioManager.getBouncingBallsSound().second
                                    sound.play(id, 0.35f, freq)
                                })
                            },
                            Demo("Pendulums") {
                                LaunchedEffect(Unit) { playOnly(audioManager.getPendulumsMusic()) }
                                PendulumsDemo { freq ->
                                    val id = audioManager.getPendulumsSound().first
                                    val sound = audioManager.getPendulumsSound().second
                                    sound.play(id, 0.35f, freq)
                                }
                            },
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun StartOverlay(onStart: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .keepScreenOn(),
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
fun AppAndroidPreview() {
    BouncingBallsInVGame()
}