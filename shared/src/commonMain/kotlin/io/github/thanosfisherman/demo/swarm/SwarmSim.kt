package io.github.thanosfisherman.demo.swarm

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import io.github.thanosfisherman.demo.audioUtils.MusicIntervals
import io.github.thanosfisherman.demo.swarm.SwarmConfig.BALL_STROKE_WIDTH
import io.github.thanosfisherman.demo.swarm.SwarmConfig.BOUNDARY_COLOR
import io.github.thanosfisherman.demo.swarm.SwarmConfig.BOUNDARY_STROKE_WIDTH
import io.github.thanosfisherman.demo.swarm.SwarmConfig.PULSE_DECAY_PER_SEC
import io.github.thanosfisherman.demo.swarm.SwarmConfig.PULSE_FLASH_THRESHOLD
import kotlin.math.max

class SwarmSim(val size: Size, val swarmScene: SwarmScene?) {

    var timer = 0f
    var currentScaleIndex = -1
    var scaleLabel: String by mutableStateOf("")
        private set

    fun update(dt: Float, onBounce: (Float) -> Unit) {
        swarmScene?.let { scene ->
            timer += dt
            val scaleIndex = (timer / 6f).toInt() % 2
            if (scaleIndex != currentScaleIndex) {
                currentScaleIndex = scaleIndex
                val rand = (0..2).random()
                val scale = when (rand) {
                    0 -> {
                        scaleLabel = "MAJOR PENTATONIC I"
                        MusicIntervals.MAJOR_PENTATONIC_SCALE
                    }

                    1 -> {
                        scaleLabel = "MINOR ADD 2,6 ARPEGGIO IV"
                        MusicIntervals.MINOR_ADD_2_6_ARPEGGIO.map { MusicIntervals.frequencyMultiplier(-7) * it }
                            .toFloatArray()
                    }

                    else -> {
                        scaleLabel = "MAJOR ADD 2 ARPEGGIO V"
                        MusicIntervals.MAJOR_ADD_2_ARPEGGIO.map { MusicIntervals.frequencyMultiplier(-5) * it }
                            .toFloatArray()
                    }
                }
                scene.balls.forEachIndexed { index, ball ->
                    ball.pitch = scale[index % scale.size]
                }
            }
            scene.balls.forEach { updateBall(scene, it, dt, onBounce) }
        }
    }

    private fun updateBall(scene: SwarmScene, ball: SwarmBall, dt: Float, onBounce: (Float) -> Unit) {
        ball.position += ball.velocity * dt

        val toCenter = ball.position - scene.center
        val distanceFromCenter = toCenter.getDistance()
        val maxDistance = scene.boundaryRadius - ball.radius

        if (distanceFromCenter > maxDistance && distanceFromCenter > 0f) {
            val normal = toCenter / distanceFromCenter // outward unit normal at the contact point

            // Clamp back inside the boundary — prevents visible overshoot past the edge on a slow frame.
            ball.position = scene.center + normal * maxDistance

            // Reflect: v' = v - 2*(v.n)*n — standard vector reflection off the boundary normal.
            val vDotN = ball.velocity.x * normal.x + ball.velocity.y * normal.y
            if (vDotN > 0f) { // only reflect if actually moving outward into the wall
                ball.velocity -= normal * (2f * vDotN)
                ball.pulse = 1f
                onBounce(ball.pitch)
            }
        }

        ball.pulse = max(0f, ball.pulse - dt * PULSE_DECAY_PER_SEC)
    }
}

// ---------- Rendering ----------

fun DrawScope.drawBackground() {
    drawRect(color = Color.Black)
}

fun DrawScope.drawBoundary(center: Offset, radius: Float) {
    // No fill — stroke only, per the reference image.
    drawCircle(color = BOUNDARY_COLOR, radius = radius, center = center, style = Stroke(width = BOUNDARY_STROKE_WIDTH))
}

/**
 * The glow gradient is only built during the brief post-bounce flash (pulse >
 * PULSE_FLASH_THRESHOLD), not every frame for every ball — with ~80 balls on screen, an
 * always-on gradient here would reintroduce the same GC-pressure/FPS problem the bouncing-balls
 * demo hit before that fix; better to build this in from the start than patch it in later.
 */
fun DrawScope.drawSwarmBall(ball: SwarmBall) {
    val radius = ball.radius * (1f + ball.pulse * 0.4f)

    if (ball.pulse > PULSE_FLASH_THRESHOLD) {
        val glowRadius = radius * (1.6f + ball.pulse * 1.4f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(ball.color.copy(alpha = 0.5f + ball.pulse * 0.4f), Color.Transparent),
                center = ball.position,
                radius = glowRadius,
            ),
            radius = glowRadius,
            center = ball.position,
        )
    }

    if (ball.filled) {
        drawCircle(color = ball.color, radius = radius, center = ball.position)
    } else {
        drawCircle(
            color = ball.color,
            radius = radius,
            center = ball.position,
            style = Stroke(width = BALL_STROKE_WIDTH)
        )
    }
}