package io.github.thanosfisherman.demo.pendulums

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.layout.onSizeChanged
import io.github.thanosfisherman.demo.GameLoopCanvas
import io.github.thanosfisherman.demo.mapRange
import io.github.thanosfisherman.demo.toRadians
import kotlin.math.*

/*
 * "Pendulums" demo — same philosophy as BouncingBallsInVGame: purely kinematic, no physics,
 * built on the shared GameLoopCanvas.
 *
 * Each pendulum swings left/right driven by an oscillating angle (0..180, sweeping and mirroring —
 * the exact same state machine BouncingBallsInVGame's balls use) mapped to a swing deflection in degrees.
 * Pendulums have different thread lengths and speed modifiers, creating a mesmerizing pendulum wave pattern.
 * When a pendulum crosses the vertical center line, a glow pulse is triggered on that ball,
 * decaying the same way BouncingBallsInVGame's post-bounce flash does.
 */

// ---------- Virtual viewport (same pattern as BouncingBallsInVGame) ----------
private val VIRTUAL_SIZE_LANDSCAPE = Size(1024f, 480f)
private val VIRTUAL_SIZE_PORTRAIT = Size(480f, 1024f)

// ---------- Tuning ----------
private const val DEFAULT_PENDULUM_COUNT = 18
private const val PIVOT_LINE_Y_FRACTION = 0.001f      // how far down from the top the pivot sits
private const val THREAD_LENGTH_MIN_FRACTION = 0.34f  // shortest thread, as a fraction of canvas height
private const val THREAD_LENGTH_MAX_FRACTION = 0.70f  // longest thread
private const val MAX_SWING_DEG = 80f                 // how far left/right of vertical the swing goes
private const val ANGULAR_SPEED_RAD_PER_SEC = 0.8f
private const val SPEED_MODIFIER_MAX = 1f
private const val SPEED_MODIFIER_MIN = 0.65f
private const val PENDULUM_RADIUS = 8f
private const val PENDULUM_STROKE_WIDTH = 2.2f
private const val PULSE_DECAY_PER_SEC = 5f            // same fade rate as BouncingBallsInVGame's flash

private const val PIVOT_RADIUS = 14f
private const val PIVOT_GLOW_RADIUS_MULTIPLIER = 3f

private val GUIDE_LINE_COLOR = Color.White.copy(alpha = 0.55f)
private val THREAD_COLOR = Color.White.copy(alpha = 0.3f)
private val PIVOT_COLOR = Color(0xFF6EA8FF)


// ---------- Model ----------

private class PendulumBall(
    val threadLength: Float,
    val speedModifier: Float,
    val color: Color,
    var phase: Float = 0f,
) {
    var previousSwingRad = 0f
    var position = Offset.Zero
    var pulse = 0f
}

private class PendulumScene(
    val pivot: Offset,
    val balls: List<PendulumBall>,
)

private fun buildScene(size: Size, pendulumCount: Int): PendulumScene {
    val count = pendulumCount.coerceAtLeast(1)
    val pivot = Offset(size.width / 2f, size.height * PIVOT_LINE_Y_FRACTION)
    val minLength = size.height * THREAD_LENGTH_MIN_FRACTION
    val maxLength = size.height * THREAD_LENGTH_MAX_FRACTION

    val balls = List(count) { i ->
        val t = if (count <= 1) 0f else i / (count - 1f)
        val threadLength = minLength + (maxLength - minLength) * t
        val speedModifier = mapRange(
            0f,
            (count - 1).coerceAtLeast(1).toFloat(),
            SPEED_MODIFIER_MAX,
            SPEED_MODIFIER_MIN,
            i.toFloat()
        )
        val hue = 195f + t * 40f // stays in the blue family, matching the pivot's color
        val startLeft = i % 2 == 0
        PendulumBall(
            threadLength = threadLength,
            speedModifier = speedModifier,
            color = Color.hsv(hue, 0.7f, 1f),
            phase = if (startLeft) PI.toFloat() else 0f,
        )
    }

    return PendulumScene(pivot = pivot, balls = balls)
}

// ---------- Update ----------

private fun updateBall(
    ball: PendulumBall,
    pivot: Offset,
    dt: Float,
) {
    ball.phase += dt * ANGULAR_SPEED_RAD_PER_SEC * ball.speedModifier

    val swingRad = MAX_SWING_DEG.toRadians() * cos(ball.phase)

    val crossedCenter = ball.previousSwingRad * swingRad < 0f
    if (crossedCenter) {
        ball.pulse = 1f
    }

    ball.previousSwingRad = swingRad

    ball.position = Offset(
        x = pivot.x + sin(swingRad) * ball.threadLength,
        y = pivot.y + cos(swingRad) * ball.threadLength,
    )

    ball.pulse = max(
        0f,
        ball.pulse - dt * PULSE_DECAY_PER_SEC
    )
}

// ---------- Rendering ----------

private fun DrawScope.drawBackground() {
    drawRect(color = Color.Black)
}

private fun DrawScope.drawGuideLines(pivot: Offset, size: Size) {
    drawLine(
        color = GUIDE_LINE_COLOR,
        start = Offset(pivot.x, 0f),
        end = Offset(pivot.x, size.height),
        strokeWidth = 1.5f,
    )
    drawLine(
        color = GUIDE_LINE_COLOR,
        start = Offset(0f, pivot.y),
        end = Offset(size.width, pivot.y),
        strokeWidth = 1.5f,
    )
}

private fun DrawScope.drawPivotCircle(center: Offset) {
    val glowRadius = PIVOT_RADIUS * PIVOT_GLOW_RADIUS_MULTIPLIER
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(PIVOT_COLOR.copy(alpha = 0.6f), Color.Transparent),
            center = center,
            radius = glowRadius,
        ),
        radius = glowRadius,
        center = center,
    )
    drawCircle(color = PIVOT_COLOR, radius = PIVOT_RADIUS, center = center) // solid, unlike the hanging balls
}

private fun DrawScope.drawThread(pivot: Offset, ball: PendulumBall) {
    drawLine(color = THREAD_COLOR, start = pivot, end = ball.position, strokeWidth = 0.82f)
}

/** Same ring-glow technique as BouncingBallsInVGame's drawBall: transparent center, transparent
 *  up to the stroke's inner edge, a colored band across the stroke, fading back to transparent
 *  outside it — pulse temporarily boosts radius/glow/alpha, same formula shape as the balls demo. */
private fun DrawScope.drawPendulumBall(ball: PendulumBall) {
    val radius = PENDULUM_RADIUS * (1f + ball.pulse * 0.3f)
    val glowRadius = radius * (1.5f + ball.pulse * 0.8f)
    val innerRadius = radius - PENDULUM_STROKE_WIDTH / 2f
    val outerRadius = radius + PENDULUM_STROKE_WIDTH / 2f

    drawCircle(
        brush = Brush.radialGradient(
            colorStops = arrayOf(
                0f to Color.Transparent,
                (innerRadius / glowRadius) to Color.Transparent,
                (outerRadius / glowRadius) to ball.color.copy(alpha = 0.5f + ball.pulse * 0.35f),
                1f to Color.Transparent,
            ),
            center = ball.position,
            radius = glowRadius,
        ),
        radius = glowRadius,
        center = ball.position,
    )

    drawCircle(
        color = ball.color,
        radius = radius,
        center = ball.position,
        style = Stroke(width = PENDULUM_STROKE_WIDTH),
    )
}

// ---------- Composable ----------

@Composable
fun PendulumsDemo(
    modifier: Modifier = Modifier,
    pendulumCount: Int = DEFAULT_PENDULUM_COUNT,
) {
    var isPortrait by remember { mutableStateOf(false) }
    var scene by remember { mutableStateOf<PendulumScene?>(null) }

    LaunchedEffect(pendulumCount, isPortrait) {
        val virtualSize = if (isPortrait) VIRTUAL_SIZE_PORTRAIT else VIRTUAL_SIZE_LANDSCAPE
        scene = buildScene(virtualSize, pendulumCount)
    }

    GameLoopCanvas(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { isPortrait = it.height > it.width },
        onUpdate = { dt ->
            scene?.let { s -> s.balls.forEach { updateBall(it, s.pivot, dt) } }
        },
        onDraw = {
            drawBackground()

            val s = scene ?: return@GameLoopCanvas
            if (size.width <= 0f || size.height <= 0f) return@GameLoopCanvas

            val virtualSize = if (isPortrait) VIRTUAL_SIZE_PORTRAIT else VIRTUAL_SIZE_LANDSCAPE
            val fitScale = min(size.width / virtualSize.width, size.height / virtualSize.height)
            val offsetX = (size.width - virtualSize.width * fitScale) / 2f
            val offsetY = (size.height - virtualSize.height * fitScale) / 2f

            translate(left = offsetX, top = offsetY) {
                scale(fitScale, fitScale, pivot = Offset.Zero) {
                    drawGuideLines(s.pivot, virtualSize)
                    s.balls.forEach { drawThread(s.pivot, it) }
                    s.balls.forEach { drawPendulumBall(it) }
                    drawPivotCircle(s.pivot) // last, so it sits on top of every thread converging on it
                }
            }
        },
    )
}