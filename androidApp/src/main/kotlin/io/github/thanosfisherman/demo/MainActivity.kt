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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import composecreativedemo.shared.generated.resources.Res
import io.github.thanosfisherman.demo.audioUtils.Music
import io.github.thanosfisherman.demo.audioUtils.Sound

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val toneUri = Res.getUri("files/sounds/Tone2.wav")
        val ballsSongUri = Res.getUri("files/music/Epic_Ballz_backing.ogg")
        val padsSongUri = Res.getUri("files/music/PadsEMaj.ogg")

        setContent {
            val context = LocalContext.current

            val sound: Sound = remember { SoundPlayer(context) }
            val ballsMusic: Music = remember { MusicPlayer(context) }
            val padsMusic: Music = remember { MusicPlayer(context) }
            val allMusic = remember { listOf(ballsMusic, padsMusic) }

            var soundId by remember { mutableIntStateOf(-1) }
            var started by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                sound.init()
                soundId = sound.loadSound(toneUri)
                ballsMusic.init()
                ballsMusic.load(ballsSongUri)
                padsMusic.init()
                padsMusic.load(padsSongUri)
            }

            fun playOnly(active: Music, loop: Boolean = true, volume: Float = 0.8f) {
                allMusic.forEach { track -> if (track !== active) track.stop() }
                active.play(loop = loop, volume = volume)
            }

            DisposableEffect(Unit) {
                onDispose {
                    sound.dispose()
                    allMusic.forEach { it.dispose() }
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
                            Demo("The balls of fury! - Thanos Psaridis") {
                                LaunchedEffect(Unit) { playOnly(ballsMusic) }
                                BouncingBallsInVGame(ballCount = 14, onBounce = { freq ->
                                    sound.play(soundId, 0.40f, freq)
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