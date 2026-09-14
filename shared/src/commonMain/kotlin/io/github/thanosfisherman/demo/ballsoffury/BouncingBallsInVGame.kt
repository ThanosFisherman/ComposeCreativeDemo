package io.github.thanosfisherman.demo.ballsoffury

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.keepScreenOn
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import io.github.thanosfisherman.demo.GameLoopCanvas
import io.github.thanosfisherman.demo.audioUtils.MusicIntervals
import io.github.thanosfisherman.demo.mapRange
import io.github.thanosfisherman.demo.rememberGameLoopState
import io.github.thanosfisherman.demo.sinDeg
import kotlin.math.max
import kotlin.math.min

// ---------- Virtual viewport ----------
private val VIRTUAL_SIZE_LANDSCAPE = Size(1024f, 480f)
private val VIRTUAL_SIZE_PORTRAIT = Size(480f, 1024f)

private fun updateBall(bouncingBall: BouncingBall, dt: Float, onBounce: (Float) -> Unit) {
    if (bouncingBall.forward) {
        bouncingBall.angle += dt * BouncingBallsConfig.ANGULAR_SPEED_DEG_PER_SEC * bouncingBall.speedModifier
        if (bouncingBall.angle > 180f) {
            bouncingBall.angle = 180f - (bouncingBall.angle - 180f) // mirror back into range
            bouncingBall.forward = false
            bouncingBall.pulse = 1f
            onBounce(bouncingBall.pitch)
        }
    } else {
        bouncingBall.angle -= dt * BouncingBallsConfig.ANGULAR_SPEED_DEG_PER_SEC * bouncingBall.speedModifier
        if (bouncingBall.angle < 0f) {
            bouncingBall.angle = -bouncingBall.angle // mirror back into range
            bouncingBall.forward = true
            bouncingBall.pulse = 1f
            onBounce(bouncingBall.pitch)
        }
    }

    val x = mapRange(0f, 180f, bouncingBall.startingX, bouncingBall.finalX, bouncingBall.angle)
    val y = bouncingBall.startingY - sinDeg(bouncingBall.angle) * bouncingBall.amplitude // minus: arc bulges upward
    bouncingBall.position = Offset(x, y)

    bouncingBall.pulse = max(0f, bouncingBall.pulse - dt * BouncingBallsConfig.PULSE_DECAY_PER_SEC)
}

private fun DrawScope.drawBackground() {
    drawRect(color = Color.Black)
}

private fun DrawScope.drawWall(wall: Wall) {
    drawLine(color = BouncingBallsConfig.WALL_COLOR, start = wall.p1, end = wall.p2, strokeWidth = 2f)
}

/** Connects consecutive balls' centers — since each ball moves independently, this segment's
 *  length naturally contracts/expands frame to frame as the balls drift apart or together. */
private fun DrawScope.drawChainLines(bouncingBalls: List<BouncingBall>) {
    for (i in 0 until bouncingBalls.size - 1) {
        drawLine(
            color = BouncingBallsConfig.CHAIN_LINE_COLOR,
            start = bouncingBalls[i].position,
            end = bouncingBalls[i + 1].position,
            strokeWidth = 2f,
        )
    }
}

private fun DrawScope.drawBall(bouncingBall: BouncingBall) {
    val radius = BouncingBallsConfig.PARTICLE_RADIUS * (1f + bouncingBall.pulse * 0.3f)

    val glowRadius = radius * (1.5f + bouncingBall.pulse * 0.8f)
    val innerRadius = radius - BouncingBallsConfig.BALL_STROKE_WIDTH / 2f
    val outerRadius = radius + BouncingBallsConfig.BALL_STROKE_WIDTH / 2f

    drawCircle(
        brush = Brush.radialGradient(
            colorStops = arrayOf(
                0f to Color.Transparent,
                (innerRadius / glowRadius) to Color.Transparent,
                (outerRadius / glowRadius) to bouncingBall.color.copy(alpha = 0.55f + bouncingBall.pulse * 0.3f),
                1f to Color.Transparent,
            ),
            center = bouncingBall.position,
            radius = glowRadius,
        ),
        radius = glowRadius,
        center = bouncingBall.position,
    )

    drawCircle(color = bouncingBall.color, radius = radius, center = bouncingBall.position, style = BouncingBallsConfig.BALL_STROKE)
}

// ---------- Composable ----------

@Composable
fun BouncingBallsInVGame(
    modifier: Modifier = Modifier,
    ballCount: Int = 10,
    onBounce: (freq: Float) -> Unit = {},
) {
    var isPortrait by remember { mutableStateOf(false) }
    var bouncingBallsScene by remember { mutableStateOf<BouncingBallsScene?>(null) }
    var scaleLabel by remember { mutableStateOf("TRITONE SCALE") }
    var ballSpeed by remember { mutableStateOf("") }
    val gameLoopState = rememberGameLoopState()
    var timer by remember { mutableFloatStateOf(0f) }
    var currentScaleIndex by remember { mutableIntStateOf(-1) }
    var currentSpeedIndex by remember { mutableIntStateOf(-1) }

    // Always built at a fixed virtual size, so it never depends on the actual window/layout
    // size — but which fixed size depends on orientation, so it keeps rebuilding (only) when
    // the device actually rotates between portrait and landscape, or ballCount changes.
    LaunchedEffect(ballCount, isPortrait) {
        val virtualSize = if (isPortrait) VIRTUAL_SIZE_PORTRAIT else VIRTUAL_SIZE_LANDSCAPE
        bouncingBallsScene = buildBouncingBallsScene(virtualSize, ballCount)
    }

    Box(modifier = Modifier.fillMaxSize().keepScreenOn()) {
        GameLoopCanvas(
            modifier = modifier
                .fillMaxSize()
                .onSizeChanged { isPortrait = it.height > it.width },
            gameLoopState = gameLoopState,
            onUpdate = { dt ->
                bouncingBallsScene?.let { s ->
                    timer += dt

                    // 1. Scale / Pitch updates (every 32 seconds)
                    val scaleIndex = (timer / 32f).toInt() % 2
                    if (scaleIndex != currentScaleIndex) {
                        currentScaleIndex = scaleIndex
                        val scale = when (scaleIndex) {
                            0 -> {
                                scaleLabel = "TRITONE SCALE"
                                MusicIntervals.TRITONE_SCALE
                            }
                            else -> {
                                scaleLabel = "WHOLE TONE SCALE"
                                MusicIntervals.WHOLE_TONE_SCALE
                            }
                        }
                        s.bouncingBalls.forEachIndexed { index, ball ->
                            ball.pitch = scale[index % scale.size]
                        }
                    }

                    // 2. Speed updates (every 15 seconds)
                    val speedIndex = (timer / 15f).toInt() % 3
                    if (speedIndex != currentSpeedIndex) {
                        currentSpeedIndex = speedIndex
                        BouncingBallsConfig.ANGULAR_SPEED_DEG_PER_SEC = when (speedIndex) {
                            0 -> { ballSpeed = "MEDIUM"; 120f }
                            1 -> { ballSpeed = "FAST"; 180f }
                            else -> { ballSpeed = "SLOW"; 90f }
                        }
                    }

                    s.bouncingBalls.forEach { updateBall(it, dt, onBounce) }
                }
            },
            onDraw = {
                drawBackground() // fills the whole actual canvas — doubles as the viewport's letterbox/pillarbox color

                val s = bouncingBallsScene ?: return@GameLoopCanvas
                if (size.width <= 0f || size.height <= 0f) return@GameLoopCanvas

                val virtualSize = if (isPortrait) VIRTUAL_SIZE_PORTRAIT else VIRTUAL_SIZE_LANDSCAPE

                // Fit-viewport transform: one uniform scale (no stretch), centered — same idea as libGDX's FitViewport.
                val fitScale = min(size.width / virtualSize.width, size.height / virtualSize.height)
                val offsetX = (size.width - virtualSize.width * fitScale) / 2f
                val offsetY = (size.height - virtualSize.height * fitScale) / 2f

                translate(left = offsetX, top = offsetY) {
                    scale(fitScale, fitScale, pivot = Offset.Zero) {
                        s.walls.forEach { drawWall(it) }
                        drawChainLines(s.bouncingBalls)
                        for (ball in s.bouncingBalls) { drawBall(ball) } // last, so balls render on top of walls/lines
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
            BasicText(text = "Balls of Fury - Thanos Psaridis", style = BouncingBallsConfig.DEBUG_TEXT_STYLE)
            BasicText(text = "FPS: ${gameLoopState.fps}", style = BouncingBallsConfig.DEBUG_TEXT_STYLE)
            BasicText(text = "Scale: $scaleLabel", style = BouncingBallsConfig.DEBUG_TEXT_STYLE)
            BasicText(text = "Ball speed: $ballSpeed", style = BouncingBallsConfig.DEBUG_TEXT_STYLE)
        }
    }
}