package io.github.thanosfisherman.demo.swarm

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import io.github.thanosfisherman.demo.audioUtils.MusicIntervals
import io.github.thanosfisherman.demo.swarm.SwarmConfig.BALL_BASE_RADIUS
import io.github.thanosfisherman.demo.swarm.SwarmConfig.BALL_RADIUS_VARIATION
import io.github.thanosfisherman.demo.swarm.SwarmConfig.BOUNDARY_RADIUS_FRACTION
import io.github.thanosfisherman.demo.swarm.SwarmConfig.FILLED_BALL_PROBABILITY
import io.github.thanosfisherman.demo.swarm.SwarmConfig.SPEED_MAX
import io.github.thanosfisherman.demo.swarm.SwarmConfig.SPEED_MIN
import io.github.thanosfisherman.demo.swarm.SwarmConfig.STAR_SEED
import kotlin.math.*
import kotlin.random.Random

class SwarmScene(
    val center: Offset,
    val boundaryRadius: Float,
    val balls: List<SwarmBall>,
)

fun buildSwarmScene(size: Size, ballCount: Int): SwarmScene {
    val count = ballCount.coerceAtLeast(1)
    val center = Offset(size.width / 2f, size.height / 2f)
    val boundaryRadius = min(size.width, size.height) * BOUNDARY_RADIUS_FRACTION

    val random = Random(STAR_SEED) // reuse the same fixed seed as the star field, for a reproducible layout
    val notes = MusicIntervals.MAJOR_SCALE

    val balls = List(count) { i ->
        val ballRadius =
            BALL_BASE_RADIUS * (1f - BALL_RADIUS_VARIATION / 2f + random.nextFloat() * BALL_RADIUS_VARIATION)

        // Uniform-by-AREA random start position inside the circle — sqrt() on the radius
        // fraction is what avoids over-clustering points near the center (a naive linear
        // random radius biases points toward the middle).
        val startRadius = (boundaryRadius - ballRadius) * sqrt(random.nextFloat())
        val startAngle = random.nextFloat() * 2f * PI.toFloat()
        val position = center + Offset(cos(startAngle) * startRadius, sin(startAngle) * startRadius)

        val moveAngle = random.nextFloat() * 2f * PI.toFloat()
        val speed = SPEED_MIN + random.nextFloat() * (SPEED_MAX - SPEED_MIN)
        val velocity = Offset(cos(moveAngle) * speed, sin(moveAngle) * speed)

        val hue = (132f + random.nextFloat() * 120f) % 360f
        val color = Color.hsv(hue, 0.95f, 1f)

        SwarmBall(
            position = position,
            velocity = velocity,
            radius = ballRadius,
            color = color,
            filled = random.nextFloat() < FILLED_BALL_PROBABILITY,
            pitch = notes[i % notes.size],
        )
    }

    return SwarmScene(
        center = center,
        boundaryRadius = boundaryRadius,
        balls = balls,
    )
}