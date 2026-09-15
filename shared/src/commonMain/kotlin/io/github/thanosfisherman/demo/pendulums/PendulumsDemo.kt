package io.github.thanosfisherman.demo.pendulums

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.keepScreenOn
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import io.github.thanosfisherman.demo.GameLoopCanvas
import io.github.thanosfisherman.demo.ballsoffury.BouncingBallsConfig
import io.github.thanosfisherman.demo.drawStars
import io.github.thanosfisherman.demo.pendulums.PendulumsConfig.DEFAULT_BIG_PENDULUM_COUNT
import io.github.thanosfisherman.demo.pendulums.PendulumsConfig.DEFAULT_PENDULUM_COUNT
import io.github.thanosfisherman.demo.pendulums.PendulumsConfig.VIRTUAL_SIZE_LANDSCAPE
import io.github.thanosfisherman.demo.pendulums.PendulumsConfig.VIRTUAL_SIZE_PORTRAIT
import io.github.thanosfisherman.demo.rememberGameLoopState
import kotlin.math.min


// ---------- Composable ----------

@Composable
fun PendulumsDemo(
    modifier: Modifier = Modifier,
    pendulumCount: Int = DEFAULT_PENDULUM_COUNT,
    bigPendulumCount: Int = DEFAULT_BIG_PENDULUM_COUNT,
    onCrossedCenterSmall: (Float) -> Unit,
    onCrossedCenterBig: (Float) -> Unit
) {
    var isPortrait by remember { mutableStateOf(false) }
    var scene by remember { mutableStateOf<PendulumScene?>(null) }
    var pendulumSim by remember { mutableStateOf<PendulumsSim?>(null) }
    val virtualSize = if (isPortrait) VIRTUAL_SIZE_PORTRAIT else VIRTUAL_SIZE_LANDSCAPE
    val gameLoopState = rememberGameLoopState()

    LaunchedEffect(pendulumCount, isPortrait) {
        scene = buildPendulumsScene(virtualSize, pendulumCount, bigPendulumCount)
        pendulumSim = PendulumsSim(scene)
    }
    Box(modifier = Modifier.fillMaxSize().keepScreenOn()) {
        GameLoopCanvas(
            modifier = modifier
                .fillMaxSize()
                .onSizeChanged { isPortrait = it.height > it.width },
            gameLoopState = gameLoopState,
            onUpdate = { dt ->
                pendulumSim?.update(dt, onCrossedCenterSmall, onCrossedCenterBig)
            },
            onDraw = {
                drawBackground()

                val s = scene ?: return@GameLoopCanvas
                if (size.width <= 0f || size.height <= 0f) return@GameLoopCanvas

                val fitScale = min(size.width / virtualSize.width, size.height / virtualSize.height)
                val offsetX = (size.width - virtualSize.width * fitScale) / 2f
                val offsetY = (size.height - virtualSize.height * fitScale) / 2f

                translate(left = offsetX, top = offsetY) {
                    scale(fitScale, fitScale, pivot = Offset.Zero) {
                        drawStars(s.stars)
                        drawGuideLines(s.pivot, virtualSize)
                        s.balls.forEach { drawThread(s.pivot, it) }
                        s.balls.forEach { drawPendulumBall(it) }
                        s.bigBalls.forEach { drawThread(s.pivot, it) }
                        s.bigBalls.forEach { drawPendulumBall(it) }
                        drawPivotCircle(s.pivot) // last, so it sits on top of every thread converging on it
                    }
                }
            },
        )
        // Debug overlay — plain Compose text on top of the Canvas, not drawn via DrawScope.
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .safeDrawingPadding()
                .padding(8.dp)
        ) {
            BasicText(text = "Pendulums - Thanos Psaridis", style = BouncingBallsConfig.DEBUG_TEXT_STYLE)
            BasicText(text = "FPS: ${gameLoopState.fps}", style = BouncingBallsConfig.DEBUG_TEXT_STYLE)
            BasicText(text = "SCALE: ${pendulumSim?.scaleLabel ?: ""}", style = BouncingBallsConfig.DEBUG_TEXT_STYLE)
        }
    }
}