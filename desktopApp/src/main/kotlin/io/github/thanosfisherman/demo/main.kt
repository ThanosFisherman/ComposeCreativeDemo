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
import androidx.compose.ui.window.rememberWindowState
import io.github.thanosfisherman.demo.audioUtils.MusicTrack
import io.github.thanosfisherman.demo.audioUtils.SoundTrack
import io.github.thanosfisherman.demo.audioUtils.getAudioManager
import io.github.thanosfisherman.demo.ballsoffury.BouncingBallsInVGame
import io.github.thanosfisherman.demo.pendulums.PendulumsDemo
import io.github.thanosfisherman.demo.swarm.SwarmDemo

fun main() = application {


    val audioManager = remember { getAudioManager() }
    val windowState = rememberWindowState(width = 1280.dp, height = 800.dp)

    LaunchedEffect(Unit) {
        audioManager.init()
    }

    Window(
        state = windowState,
        onCloseRequest = { audioManager.dispose(); exitApplication() },
        title = "Balls of fury",
        onKeyEvent = { event ->
            if (event.key == Key.Escape && event.type == KeyEventType.KeyUp) {
                audioManager.dispose()
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
                        Demo("Swarm") {
                            LaunchedEffect(Unit) {
                                audioManager.stopAllMusic()
                            }
                            SwarmDemo { freq ->
                                audioManager.playSound(SoundTrack.Swarm, 0.62f, freq)
                            }
                        },
                        Demo("Pendulums") {
                            LaunchedEffect(Unit) {
                                audioManager.playMusic(MusicTrack.Pendulums, volume = 0.5f)
                            }
                            PendulumsDemo(onCrossedCenterSmall = { freq ->
                                audioManager.playSound(SoundTrack.Pendulums, 0.58f, freq)
                            }, onCrossedCenterBig = { freq ->
                                audioManager.playSound(SoundTrack.Bass, 0.88f, freq)
                            })
                        },
                        Demo("The balls of Fury") {
                            LaunchedEffect(Unit) {
                                audioManager.playMusic(MusicTrack.BouncingBalls)
                            }
                            BouncingBallsInVGame(onBounce = { freq ->
                                audioManager.playSound(SoundTrack.BouncingBalls, 0.42f, freq)
                            })
                        }
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