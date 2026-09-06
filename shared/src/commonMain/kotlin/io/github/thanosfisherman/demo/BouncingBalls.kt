package io.github.thanosfisherman.demo

import androidx.compose.foundation.Canvas
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
import io.github.thanosfisherman.demo.Config.ANGULAR_SPEED_DEG_PER_SEC
import io.github.thanosfisherman.demo.Config.BALL_STROKE
import io.github.thanosfisherman.demo.Config.BALL_STROKE_WIDTH
import io.github.thanosfisherman.demo.Config.CHAIN_LINE_COLOR
import io.github.thanosfisherman.demo.Config.DEBUG_TEXT_STYLE
import io.github.thanosfisherman.demo.Config.MAX_DT
import io.github.thanosfisherman.demo.Config.PARTICLE_RADIUS
import io.github.thanosfisherman.demo.Config.PULSE_DECAY_PER_SEC
import io.github.thanosfisherman.demo.Config.WALL_COLOR
import io.github.thanosfisherman.demo.audioUtils.MusicIntervals
import kotlinx.coroutines.isActive
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

// ---------- Virtual viewport ----------
// Two fixed virtual sizes, one per orientation, so the fit-viewport scale isn't fighting a
// mismatched aspect ratio. Using the landscape (wide) virtual size on a portrait phone is what
// made the scene look tiny — width became the limiting term in the fit-scale calculation, so
// almost all of the screen's height went unused as letterboxing. Each virtual size still keeps
// its own shape constant regardless of the real window size, exactly as before.
private val VIRTUAL_SIZE_LANDSCAPE = Size(1024f, 480f)
private val VIRTUAL_SIZE_PORTRAIT = Size(480f, 1024f)

private fun updateBall(ball: Ball, dt: Float, onBounce: (Float) -> Unit) {
    if (ball.forward) {
        ball.angle += dt * ANGULAR_SPEED_DEG_PER_SEC * ball.speedModifier
        if (ball.angle > 180f) {
            ball.angle = 180f - (ball.angle - 180f) // mirror back into range
            ball.forward = false
            ball.pulse = 1f
            onBounce(ball.pitch)
        }
    } else {
        ball.angle -= dt * ANGULAR_SPEED_DEG_PER_SEC * ball.speedModifier
        if (ball.angle < 0f) {
            ball.angle = -ball.angle // mirror back into range
            ball.forward = true
            ball.pulse = 1f
            onBounce(ball.pitch)
        }
    }

    val x = mapRange(0f, 180f, ball.startingX, ball.finalX, ball.angle)
    val y = ball.startingY - sinDeg(ball.angle) * ball.amplitude // minus: arc bulges upward
    ball.position = Offset(x, y)

    ball.pulse = max(0f, ball.pulse - dt * PULSE_DECAY_PER_SEC)
}


private fun DrawScope.drawBackground() {
    drawRect(color = Color.Black)
}

private fun DrawScope.drawWall(wall: Wall) {
    drawLine(color = WALL_COLOR, start = wall.p1, end = wall.p2, strokeWidth = 2f)
}

/** Connects consecutive balls' centers — since each ball moves independently, this segment's
 *  length naturally contracts/expands frame to frame as the balls drift apart or together. */
private fun DrawScope.drawChainLines(balls: List<Ball>) {
    for (i in 0 until balls.size - 1) {
        drawLine(
            color = CHAIN_LINE_COLOR,
            start = balls[i].position,
            end = balls[i + 1].position,
            strokeWidth = 2f,
        )
    }
}

private fun DrawScope.drawBall(ball: Ball) {
    val radius = PARTICLE_RADIUS * (1f + ball.pulse * 0.3f)

    val glowRadius = radius * (1.5f + ball.pulse * 0.8f)
    val innerRadius = radius - BALL_STROKE_WIDTH / 2f
    val outerRadius = radius + BALL_STROKE_WIDTH / 2f

    drawCircle(
        brush = Brush.radialGradient(
            colorStops = arrayOf(
                0f to Color.Transparent,
                (innerRadius / glowRadius) to Color.Transparent,
                (outerRadius / glowRadius) to ball.color.copy(alpha = 0.55f + ball.pulse * 0.3f),
                1f to Color.Transparent,
            ),
            center = ball.position,
            radius = glowRadius,
        ),
        radius = glowRadius,
        center = ball.position,
    )

    drawCircle(color = ball.color, radius = radius, center = ball.position, style = BALL_STROKE)
}

// ---------- Composable ----------

@Composable
fun BouncingBallsInVGame(
    modifier: Modifier = Modifier,
    ballCount: Int = 10,
    onBounce: (freq: Float) -> Unit = {},
) {
    var isPortrait by remember { mutableStateOf(false) }
    var scene by remember { mutableStateOf<Scene?>(null) }
    var frameTick by remember { mutableStateOf(false) }
    var isAssigned by remember { mutableStateOf(false) }
    var fps by remember { mutableIntStateOf(0) } // updated once/sec — cheap to recompose on, unlike frameTick
    var scaleLabel by remember { mutableStateOf("TRITONE SCALE") }
    var ballSpeed by remember { mutableStateOf("") }
    var dt by remember { mutableFloatStateOf(0f) }
    // Always built at a fixed virtual size, so it never depends on the actual window/layout
    // size — but which fixed size depends on orientation, so it keeps rebuilding (only) when
    // the device actually rotates between portrait and landscape, or ballCount changes.
    LaunchedEffect(ballCount, isPortrait) {
        val virtualSize = if (isPortrait) VIRTUAL_SIZE_PORTRAIT else VIRTUAL_SIZE_LANDSCAPE
        scene = buildScene(virtualSize, ballCount)
    }

    // The game loop: one step per display frame, advanced by measured delta time.
    LaunchedEffect(Unit) {
        var lastFrameTimeNanos = 0L

        // FPS logging: accumulated over a rolling 1-second window using the *raw*
        // (unclamped) frame delta — MAX_DT below is a physics safety clamp, not a
        // measurement of what the display is actually doing.
        var fpsAccumSeconds = 0f
        var fpsFrameCount = 0
        var timer = 0f

        // Track the active state index so logic only triggers on changes
        var currentScaleIndex = -1
        var currentSpeedIndex = -1

        while (isActive) {
            withFrameNanos { frameTimeNanos ->
                val rawDt = if (lastFrameTimeNanos == 0L) {
                    0f
                } else {
                    (frameTimeNanos - lastFrameTimeNanos) / 1_000_000_000f
                }
                dt = rawDt.coerceAtMost(MAX_DT)
                lastFrameTimeNanos = frameTimeNanos

                scene?.let { s ->
                    timer += dt
                    // 1. Scale / Pitch updates (every 30 seconds)
                    val scaleIndex = (timer / 32f).toInt() % 2
                    if (scaleIndex != currentScaleIndex) {
                        currentScaleIndex = scaleIndex
                        val scale = when (scaleIndex) {
                            0 -> {
                                //println("Tritone")
                                scaleLabel = "TRITONE SCALE"
                                MusicIntervals.TRITONE_SCALE
                            }

                            else -> {
                                // println("Whole Tone")
                                scaleLabel = "WHOLE TONE SCALE"
                                MusicIntervals.WHOLE_TONE_SCALE
                            }
                        }
                        s.balls.forEachIndexed { index, ball ->
                            ball.pitch = scale[index % scale.size]
                        }
                    }

                    // 2. Speed updates (every 15 seconds)
                    val speedIndex = (timer / 15f).toInt() % 3
                    if (speedIndex != currentSpeedIndex) {
                        currentSpeedIndex = speedIndex
                        ANGULAR_SPEED_DEG_PER_SEC = when (speedIndex) {
                            0 -> {
                                ballSpeed = "MEDIUM"
                                120f
                            }

                            1 -> {
                                ballSpeed = "FAST"
                                180f
                            }

                            else -> {
                                ballSpeed = "SLOW"
                                90f
                            }
                        }
                    }
                }

                if (rawDt > 0f) {
                    fpsAccumSeconds += rawDt
                    fpsFrameCount++
                    if (fpsAccumSeconds >= 1f) {
                        val measuredFps = fpsFrameCount / fpsAccumSeconds
                        fps = measuredFps.roundToInt()
                        //println("FPS: ${(fps * 10f).roundToInt() / 10f}")
                        fpsAccumSeconds = 0f
                        fpsFrameCount = 0
                    }
                }
                frameTick = !frameTick // bump so the Canvas below knows to redraw
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().keepScreenOn()) {

        Canvas(
            modifier = modifier
                .fillMaxSize()
                .onSizeChanged { isPortrait = it.height > it.width }
        ) {
            // Reading frameTick subscribes this draw scope to it, so every increment
            // from the game loop above triggers a fresh draw at a steady rate.
            @Suppress("UNUSED_EXPRESSION")
            frameTick

            drawBackground() // fills the whole actual canvas — doubles as the viewport's letterbox/pillarbox color

            val s = scene ?: return@Canvas
            if (size.width <= 0f || size.height <= 0f) return@Canvas

            val virtualSize = if (isPortrait) VIRTUAL_SIZE_PORTRAIT else VIRTUAL_SIZE_LANDSCAPE

            // Fit-viewport transform: one uniform scale (no stretch), centered — same idea as libGDX's FitViewport.
            val fitScale = min(size.width / virtualSize.width, size.height / virtualSize.height)
            val offsetX = (size.width - virtualSize.width * fitScale) / 2f
            val offsetY = (size.height - virtualSize.height * fitScale) / 2f

            translate(left = offsetX, top = offsetY) {
                scale(fitScale, fitScale, pivot = Offset.Zero) {
                    for (i in 0 until s.balls.size - 1) {
                        updateBall(s.balls[i], dt, onBounce)
                        drawBall(s.balls[i])
                    }
                    updateBall(s.balls[s.balls.size - 1], dt, onBounce)
                    drawBall(s.balls[s.balls.size - 1])
                    s.walls.forEach { drawWall(it) }
                    drawChainLines(s.balls)
                }
            }
        }
        // Debug overlay — plain Compose text on top of the Canvas, not drawn via DrawScope,
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .safeDrawingPadding()
                .padding(8.dp)
        ) {
            BasicText(text = "Balls of Fury - Thanos Psaridis", style = DEBUG_TEXT_STYLE)
            BasicText(text = "FPS: $fps", style = DEBUG_TEXT_STYLE)
            BasicText(text = "Scale: $scaleLabel", style = DEBUG_TEXT_STYLE)
            BasicText(text = "Ball speed: $ballSpeed", style = DEBUG_TEXT_STYLE)
        }
    }
}