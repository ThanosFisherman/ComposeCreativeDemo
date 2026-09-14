package io.github.thanosfisherman.demo.ballsoffury

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import io.github.thanosfisherman.demo.ballsoffury.Config.SPEED_MODIFIER_MAX
import io.github.thanosfisherman.demo.ballsoffury.Config.SPEED_MODIFIER_MIN
import io.github.thanosfisherman.demo.audioUtils.MusicIntervals
import io.github.thanosfisherman.demo.mapRange
import io.github.thanosfisherman.demo.wallXAtY

data class Scene(val walls: List<Wall>, val bouncingBalls: List<BouncingBall>)


fun buildScene(size: Size, ballCount: Int): Scene {
    val count = ballCount.coerceAtLeast(1)

    // Rows are packed a constant ROW_SPACING apart starting at a constant fraction of
    // the canvas height, so the vertical extent they occupy grows with `count`.
    val firstRowY = size.height * Config.FIRST_ROW_Y_FRACTION
    val lastRowY = firstRowY + (count - 1) * Config.ROW_SPACING

    // The wall geometry hugs that extent, with a small margin on each end.
    val topY = (firstRowY - Config.ROW_TOP_MARGIN).coerceAtLeast(size.height * Config.WALL_TOP_MIN_Y_FRACTION)
    val apexY = (lastRowY + Config.ROW_APEX_MARGIN).coerceAtMost(size.height * Config.WALL_APEX_MAX_Y_FRACTION)

    val apex = Offset(size.width / 2f, apexY)
    val topLeft = Offset(size.width * (0.5f - Config.WALL_HALF_WIDTH_FRACTION), topY)
    val topRight = Offset(size.width * (0.5f + Config.WALL_HALF_WIDTH_FRACTION), topY)

    val leftWall = Wall(apex, topLeft)
    val rightWall = Wall(apex, topRight)

    val ampMax = size.height * Config.AMPLITUDE_MAX_FRACTION
    val ampMin = size.height * Config.AMPLITUDE_MIN_FRACTION

    val bouncingBalls = List(count) { i ->
        val t = if (count <= 1) 0f else i / (count - 1f)
        val rowY = firstRowY + i * Config.ROW_SPACING

        val leftBound = wallXAtY(leftWall, rowY) + Config.PARTICLE_RADIUS
        val rightBound = wallXAtY(rightWall, rowY) - Config.PARTICLE_RADIUS
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

    return Scene(listOf(leftWall, rightWall), bouncingBalls)
}