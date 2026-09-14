package io.github.thanosfisherman.demo.pendulums

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import io.github.thanosfisherman.demo.audioUtils.MusicIntervals
import io.github.thanosfisherman.demo.pendulums.PendulumsConfig.ANGULAR_SPEED_RAD_PER_SEC
import io.github.thanosfisherman.demo.pendulums.PendulumsConfig.GUIDE_LINE_COLOR
import io.github.thanosfisherman.demo.pendulums.PendulumsConfig.MAX_SWING_DEG
import io.github.thanosfisherman.demo.pendulums.PendulumsConfig.PENDULUM_RADIUS
import io.github.thanosfisherman.demo.pendulums.PendulumsConfig.PENDULUM_STROKE_WIDTH
import io.github.thanosfisherman.demo.pendulums.PendulumsConfig.PIVOT_COLOR
import io.github.thanosfisherman.demo.pendulums.PendulumsConfig.PIVOT_GLOW_RADIUS_MULTIPLIER
import io.github.thanosfisherman.demo.pendulums.PendulumsConfig.PIVOT_RADIUS
import io.github.thanosfisherman.demo.pendulums.PendulumsConfig.PULSE_DECAY_PER_SEC
import io.github.thanosfisherman.demo.pendulums.PendulumsConfig.THREAD_COLOR
import io.github.thanosfisherman.demo.toRadians
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

class PendulumsSim(val scene: PendulumScene?) {

    var timer = 0f
    var currentScaleIndex = -1
    var scaleLabel = ""

    private fun updateBall(
        ball: PendulumBall,
        pivot: Offset,
        dt: Float,
        onCrossedCenter: (Float) -> Unit
    ) {
        ball.phase += dt * ANGULAR_SPEED_RAD_PER_SEC * ball.speedModifier

        val swingRad = MAX_SWING_DEG.toRadians() * cos(ball.phase)

        val crossedCenter = ball.previousSwingRad * swingRad < 0f
        if (crossedCenter) {
            ball.pulse = 1f
            onCrossedCenter(ball.pitch)
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

    fun update(dt: Float, onCrossedCenter: (Float) -> Unit) {
        scene?.let { s ->
            timer += dt
            // 1. Scale / Pitch updates (every 32 seconds)
            val scaleIndex = (timer / 32f).toInt() % 2
            if (scaleIndex != currentScaleIndex) {
                currentScaleIndex = scaleIndex
                val scale = when (scaleIndex) {
                    0 -> {
                        scaleLabel = "MAJOR PENTATONIC"
                        MusicIntervals.MAJOR_PENTATONIC_SCALE
                    }

                    else -> {
                        scaleLabel = "MAJOR ADD 2 ARPEGGIO"
                        MusicIntervals.MAJOR_ADD_2_ARPEGGIO
                    }
                }
                s.balls.forEachIndexed { index, ball ->
                    ball.pitch = scale[index % scale.size]
                }
            }
            s.balls.forEach { updateBall(it, s.pivot, dt, onCrossedCenter) }
        }
    }
}

// ---------- Rendering ----------

fun DrawScope.drawBackground() {
    drawRect(color = Color.Black)
}

fun DrawScope.drawGuideLines(pivot: Offset, size: Size) {
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

fun DrawScope.drawPivotCircle(center: Offset) {
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

fun DrawScope.drawThread(pivot: Offset, ball: PendulumBall) {
    drawLine(color = THREAD_COLOR, start = pivot, end = ball.position, strokeWidth = 0.82f)
}

/** Same ring-glow technique as BouncingBallsInVGame's drawBall: transparent center, transparent
 *  up to the stroke's inner edge, a colored band across the stroke, fading back to transparent
 *  outside it — pulse temporarily boosts radius/glow/alpha, same formula shape as the balls demo. */
fun DrawScope.drawPendulumBall(ball: PendulumBall) {
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