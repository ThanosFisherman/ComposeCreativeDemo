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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

        setContent {
            val context = LocalContext.current

            val sound: Sound = remember { SoundPlayer(context) }
            val music: Music = remember { MusicPlayer(context) }

            var soundId by remember { mutableIntStateOf(-1) }
            var started by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                val uri = Res.getUri("files/sounds/Tone2.wav")
                val song = Res.getUri("files/music/Epic_Ballz_backing.ogg")

                sound.init()
                music.init()
                soundId = sound.loadSound(uri)
                music.load(song)
            }

            DisposableEffect(Unit) {
                onDispose {
                    sound.dispose()
                    music.dispose()
                }
            }

            MaterialTheme {
                if (!started) {
                    StartOverlay(onStart = {
                        music.play(loop = true, volume = 0.8f)
                        started = true
                    })
                } else {
                    BouncingBallsInVGame(ballCount = 14, onBounce = { freq ->
                        sound.play(soundId, 0.40f, freq)
                    })
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
                pressedElevation = 2.dp
            ),
            contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp)
        ) {
            Text(
                color = Color.White,
                text = "Click to Start",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.5.sp
            )
        }
    }
}

/**
 * old code to keep screen on
 */
/*@Composable
fun KeepScreenOn() {
    val context = LocalContext.current
    DisposableEffect(Unit) {
        val window = context.findActivity()?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
}

fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}*/

@Preview
@Composable
fun AppAndroidPreview() {
    BouncingBallsInVGame()
}