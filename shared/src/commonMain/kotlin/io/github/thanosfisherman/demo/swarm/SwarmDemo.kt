package io.github.thanosfisherman.demo.swarm

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.keepScreenOn
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import io.github.thanosfisherman.demo.GameLoopCanvas
import io.github.thanosfisherman.demo.ballsoffury.BouncingBallsConfig
import io.github.thanosfisherman.demo.drawStars
import io.github.thanosfisherman.demo.generateStars
import io.github.thanosfisherman.demo.rememberGameLoopState
import io.github.thanosfisherman.demo.swarm.SwarmConfig.DEFAULT_BALL_COUNT
import io.github.thanosfisherman.demo.swarm.SwarmConfig.STAR_COUNT
import io.github.thanosfisherman.demo.swarm.SwarmConfig.STAR_SEED
import io.github.thanosfisherman.demo.swarm.SwarmConfig.VIRTUAL_SIZE
import kotlin.math.min


@Composable
fun SwarmDemo(
    modifier: Modifier = Modifier,
    ballCount: Int = DEFAULT_BALL_COUNT,
    onBounce: (freq: Float) -> Unit = {},
) {
    var scene by remember { mutableStateOf<SwarmScene?>(null) }
    var swarmSim by remember { mutableStateOf<SwarmSim?>(null) }
    var canvasSize by remember { mutableStateOf(Size.Zero) }
    val gameLoopState = rememberGameLoopState()

    // Stars are generated from the LIVE canvas size, not the fixed virtual one
    val stars = remember(canvasSize) { generateStars(canvasSize, STAR_COUNT, STAR_SEED) }

    LaunchedEffect(ballCount) {
        scene = buildSwarmScene(VIRTUAL_SIZE, ballCount)
        swarmSim = SwarmSim(VIRTUAL_SIZE, scene)
    }

    Box(modifier = Modifier.fillMaxSize().keepScreenOn()) {
        GameLoopCanvas(
            modifier = modifier
                .fillMaxSize()
                .onSizeChanged { canvasSize = Size(it.width.toFloat(), it.height.toFloat()) },
            gameLoopState = gameLoopState,
            onUpdate = { dt -> swarmSim?.update(dt, onBounce) },
            onDraw = {
                drawBackground()
                drawStars(stars)

                val s = scene ?: return@GameLoopCanvas
                if (size.width <= 0f || size.height <= 0f) return@GameLoopCanvas

                val fitScale = min(size.width / VIRTUAL_SIZE.width, size.height / VIRTUAL_SIZE.height)
                val offsetX = (size.width - VIRTUAL_SIZE.width * fitScale) / 2f
                val offsetY = (size.height - VIRTUAL_SIZE.height * fitScale) / 2f

                translate(left = offsetX, top = offsetY) {
                    scale(fitScale, fitScale, pivot = Offset.Zero) {
                        drawBoundary(s.center, s.boundaryRadius)
                        s.balls.forEach { drawSwarmBall(it) }
                    }
                }
            },
        )

        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .safeDrawingPadding()
                .padding(8.dp)
        ) {
            BasicText(text = "Swarm - Thanos Psaridis", style = BouncingBallsConfig.DEBUG_TEXT_STYLE)
            BasicText(text = "FPS: ${gameLoopState.fps}", style = BouncingBallsConfig.DEBUG_TEXT_STYLE)
            BasicText(text = "SCALE: ${swarmSim?.scaleLabel ?: ""}", style = BouncingBallsConfig.DEBUG_TEXT_STYLE)
        }
    }
}