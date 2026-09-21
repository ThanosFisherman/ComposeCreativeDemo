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
import io.github.thanosfisherman.demo.audioUtils.Music
import io.github.thanosfisherman.demo.ballsoffury.BouncingBallsInVGame
import io.github.thanosfisherman.demo.pendulums.PendulumsDemo
import io.github.thanosfisherman.demo.swarm.SwarmDemo
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.events.Event

@OptIn(ExperimentalComposeUiApi::class)
fun main() {

    val audioManager = getAudioManager().also { it.init() }

    fun playOnly(active: Music, loop: Boolean = true, volume: Float = 0.8f) {
        audioManager.allMusic.forEach { track -> if (track !== active) track.stop() }
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
                    Demo("Swarm") {
                        LaunchedEffect(Unit) {
                            audioManager.allMusic.forEach { it.stop() }
                        }
                        SwarmDemo { freq ->
                            val id = audioManager.getSwarmSound().first
                            val sound = audioManager.getSwarmSound().second
                            sound.play(id, 0.34f, freq)
                        }
                    },
                    Demo("Pendulums") {
                        LaunchedEffect(Unit) { playOnly(audioManager.getPendulumsMusic(), volume = 0.6f) }
                        PendulumsDemo(onCrossedCenterSmall = { freq ->
                            val id = audioManager.getPendulumsSound().first
                            val sound = audioManager.getPendulumsSound().second
                            sound.play(id, 0.35f, freq)
                        }, onCrossedCenterBig = { freq ->
                            val id = audioManager.getBigBallsPendulumsSound().first
                            val sound = audioManager.getBigBallsPendulumsSound().second
                            sound.play(id, 0.78f, freq)
                        })
                    },
                    Demo("The balls of Fury") {
                        LaunchedEffect(Unit) { playOnly(audioManager.getBouncingBallsMusic()) }
                        BouncingBallsInVGame(ballCount = 14, onBounce = { freq ->
                            val id = audioManager.getBouncingBallsSound().first
                            val sound = audioManager.getBouncingBallsSound().second
                            sound.play(id, 0.35f, freq)
                        })
                    }
                )
            )
        }
    }

    window.addEventListener("beforeunload", { _: Event ->
        audioManager.dispose()
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