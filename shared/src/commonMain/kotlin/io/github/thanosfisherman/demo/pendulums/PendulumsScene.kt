package io.github.thanosfisherman.demo.pendulums

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import io.github.thanosfisherman.demo.Star
import io.github.thanosfisherman.demo.audioUtils.MusicIntervals
import io.github.thanosfisherman.demo.generateStars
import io.github.thanosfisherman.demo.mapRange
import io.github.thanosfisherman.demo.pendulums.PendulumsConfig.PIVOT_LINE_Y_FRACTION
import io.github.thanosfisherman.demo.pendulums.PendulumsConfig.SPEED_MODIFIER_MAX
import io.github.thanosfisherman.demo.pendulums.PendulumsConfig.SPEED_MODIFIER_MIN
import io.github.thanosfisherman.demo.pendulums.PendulumsConfig.THREAD_LENGTH_MAX_FRACTION
import io.github.thanosfisherman.demo.pendulums.PendulumsConfig.THREAD_LENGTH_MIN_FRACTION
import kotlin.math.PI
import kotlin.math.min

class PendulumScene(
    val pivot: Offset,
    val balls: List<PendulumBall>,
    val stars: List<Star>
)

fun buildPendulumsScene(size: Size, pendulumCount: Int): PendulumScene {
    val count = pendulumCount.coerceAtLeast(1)
    val pivot = Offset(size.width / 2f, size.height * PIVOT_LINE_Y_FRACTION)
    val referenceDim = min(size.width, size.height)
    val minLength = referenceDim * THREAD_LENGTH_MIN_FRACTION
    val maxLength = referenceDim * THREAD_LENGTH_MAX_FRACTION

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
        val notes = MusicIntervals.MAJOR_PENTATONIC_SCALE
        val noteIndex = i % notes.size
        PendulumBall(
            threadLength = threadLength,
            speedModifier = speedModifier,
            color = Color.hsv(hue, 0.7f, 1f),
            phase = if (startLeft) PI.toFloat() else 0f,
            pitch = notes[noteIndex],
        )
    }

    val stars = generateStars(size)

    return PendulumScene(pivot = pivot, balls = balls, stars = stars)
}