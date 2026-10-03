package io.github.thanosfisherman.demo

import android.os.Bundle
import android.util.Log
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
import io.github.thanosfisherman.demo.audioUtils.*
import io.github.thanosfisherman.demo.ballsoffury.BouncingBallsInVGame
import io.github.thanosfisherman.demo.pendulums.PendulumsDemo
import io.github.thanosfisherman.demo.swarm.SwarmDemo

class MainActivity : ComponentActivity() {

    private lateinit var audioManager: AudioManager

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        initializeAudioManager(this)
        audioManager = getAudioManager()

        setContent {

            var started by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                audioManager.init()
            }

            DisposableEffect(Unit) {
                onDispose {
                    audioManager.dispose()
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
                                    audioManager.playSound(SoundTrack.Pendulums, 0.60f, freq)
                                }, onCrossedCenterBig = { freq ->
                                    audioManager.playSound(SoundTrack.Bass, 0.89f, freq)
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

    override fun onDestroy() {
        super.onDestroy()
        Log.d("MainActivity", "onDestroy")
        audioManager.dispose()
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