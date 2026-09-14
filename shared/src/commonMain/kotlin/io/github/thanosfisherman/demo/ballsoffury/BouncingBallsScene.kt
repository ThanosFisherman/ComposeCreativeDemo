package io.github.thanosfisherman.demo.ballsoffury

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import io.github.thanosfisherman.demo.Star
import io.github.thanosfisherman.demo.audioUtils.MusicIntervals
import io.github.thanosfisherman.demo.ballsoffury.BouncingBallsConfig.SPEED_MODIFIER_MAX
import io.github.thanosfisherman.demo.ballsoffury.BouncingBallsConfig.SPEED_MODIFIER_MIN
import io.github.thanosfisherman.demo.generateStars
import io.github.thanosfisherman.demo.mapRange
import io.github.thanosfisherman.demo.wallXAtY

data class BouncingBallsScene(val walls: List<Wall>, val bouncingBalls: List<BouncingBall>, val stars: List<Star>)


fun buildBouncingBallsScene(size: Size, ballCount: Int): BouncingBallsScene {
    val count = ballCount.coerceAtLeast(1)

    // Rows are packed a constant ROW_SPACING apart starting at a constant fraction of
    // the canvas height, so the vertical extent they occupy grows with `count`.
    val firstRowY = size.height * BouncingBallsConfig.FIRST_ROW_Y_FRACTION
    val lastRowY = firstRowY + (count - 1) * BouncingBallsConfig.ROW_SPACING

    // The wall geometry hugs that extent, with a small margin on each end.
    val topY =
        (firstRowY - BouncingBallsConfig.ROW_TOP_MARGIN).coerceAtLeast(size.height * BouncingBallsConfig.WALL_TOP_MIN_Y_FRACTION)
    val apexY =
        (lastRowY + BouncingBallsConfig.ROW_APEX_MARGIN).coerceAtMost(size.height * BouncingBallsConfig.WALL_APEX_MAX_Y_FRACTION)

    val apex = Offset(size.width / 2f, apexY)
    val topLeft = Offset(size.width * (0.5f - BouncingBallsConfig.WALL_HALF_WIDTH_FRACTION), topY)
    val topRight = Offset(size.width * (0.5f + BouncingBallsConfig.WALL_HALF_WIDTH_FRACTION), topY)

    val leftWall = Wall(apex, topLeft)
    val rightWall = Wall(apex, topRight)

    val ampMax = size.height * BouncingBallsConfig.AMPLITUDE_MAX_FRACTION
    val ampMin = size.height * BouncingBallsConfig.AMPLITUDE_MIN_FRACTION

    val bouncingBalls = List(count) { i ->
        val t = if (count <= 1) 0f else i / (count - 1f)
        val rowY = firstRowY + i * BouncingBallsConfig.ROW_SPACING

        val leftBound = wallXAtY(leftWall, rowY) + BouncingBallsConfig.PARTICLE_RADIUS
        val rightBound = wallXAtY(rightWall, rowY) - BouncingBallsConfig.PARTICLE_RADIUS
        val notes = MusicIntervals.TRITONE_SCALE
        val noteIndex = i % notes.size
        BouncingBall(
            startingX = leftBound,
            finalX = rightBound,
            startingY = rowY,
            amplitude = ampMax - (ampMax - ampMin) * t,
            speedModifier = mapRange(
                0f,
                (count - 1).coerceAtLeast(1).toFloat(),
                SPEED_MODIFIER_MAX,
                SPEED_MODIFIER_MIN,
                i.toFloat()
            ),
            hue = t * 300f,
            pitch = notes[noteIndex]
        )
    }
    val stars = generateStars(size)
    return BouncingBallsScene(listOf(leftWall, rightWall), bouncingBalls, stars)
}