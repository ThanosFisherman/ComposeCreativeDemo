package io.github.thanosfisherman.demo

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import io.github.thanosfisherman.demo.Config.AMPLITUDE_MAX_FRACTION
import io.github.thanosfisherman.demo.Config.AMPLITUDE_MIN_FRACTION
import io.github.thanosfisherman.demo.Config.FIRST_ROW_Y_FRACTION
import io.github.thanosfisherman.demo.Config.PARTICLE_RADIUS
import io.github.thanosfisherman.demo.Config.ROW_APEX_MARGIN
import io.github.thanosfisherman.demo.Config.ROW_SPACING
import io.github.thanosfisherman.demo.Config.ROW_TOP_MARGIN
import io.github.thanosfisherman.demo.Config.SPEED_MODIFIER_MAX
import io.github.thanosfisherman.demo.Config.SPEED_MODIFIER_MIN
import io.github.thanosfisherman.demo.Config.WALL_APEX_MAX_Y_FRACTION
import io.github.thanosfisherman.demo.Config.WALL_HALF_WIDTH_FRACTION
import io.github.thanosfisherman.demo.Config.WALL_TOP_MIN_Y_FRACTION
import io.github.thanosfisherman.demo.audioUtils.MusicIntervals

data class Scene(val walls: List<Wall>, val balls: List<Ball>)


fun buildScene(size: Size, ballCount: Int): Scene {
    val count = ballCount.coerceAtLeast(1)

    // Rows are packed a constant ROW_SPACING apart starting at a constant fraction of
    // the canvas height, so the vertical extent they occupy grows with `count`.
    val firstRowY = size.height * FIRST_ROW_Y_FRACTION
    val lastRowY = firstRowY + (count - 1) * ROW_SPACING

    // The wall geometry hugs that extent, with a small margin on each end.
    val topY = (firstRowY - ROW_TOP_MARGIN).coerceAtLeast(size.height * WALL_TOP_MIN_Y_FRACTION)
    val apexY = (lastRowY + ROW_APEX_MARGIN).coerceAtMost(size.height * WALL_APEX_MAX_Y_FRACTION)

    val apex = Offset(size.width / 2f, apexY)
    val topLeft = Offset(size.width * (0.5f - WALL_HALF_WIDTH_FRACTION), topY)
    val topRight = Offset(size.width * (0.5f + WALL_HALF_WIDTH_FRACTION), topY)

    val leftWall = Wall(apex, topLeft)
    val rightWall = Wall(apex, topRight)

    val ampMax = size.height * AMPLITUDE_MAX_FRACTION
    val ampMin = size.height * AMPLITUDE_MIN_FRACTION

    val balls = List(count) { i ->
        val t = if (count <= 1) 0f else i / (count - 1f)
        val rowY = firstRowY + i * ROW_SPACING

        val leftBound = wallXAtY(leftWall, rowY) + PARTICLE_RADIUS
        val rightBound = wallXAtY(rightWall, rowY) - PARTICLE_RADIUS
        val notes = MusicIntervals.TRITONE_SCALE
        val noteIndex = i % notes.size
        Ball(
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

    return Scene(listOf(leftWall, rightWall), balls)
}