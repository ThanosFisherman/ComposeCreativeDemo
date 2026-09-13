package io.github.thanosfisherman.demo.pendulums

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope

private const val PIVOT_LINE_Y_FRACTION = 0.06f // how far down from the top the horizontal line sits
private const val PIVOT_RADIUS = 14f
private const val PIVOT_GLOW_RADIUS_MULTIPLIER = 3f

private val GUIDE_LINE_COLOR = Color.White.copy(alpha = 0.35f)
private val PIVOT_COLOR = Color(0xFF6EA8FF)

@Composable
fun PendulumsDemo(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        drawRect(color = Color.Black)

        val centerX = size.width / 2f
        val pivotY = size.height * PIVOT_LINE_Y_FRACTION

        // Vertical center line — pendulums pass through here at the bottom of their swing.
        drawLine(
            color = GUIDE_LINE_COLOR,
            start = Offset(centerX, 0f),
            end = Offset(centerX, size.height),
            strokeWidth = 1.5f,
        )

        // Horizontal line near the top — the pivot circle sits on this.
        drawLine(
            color = GUIDE_LINE_COLOR,
            start = Offset(0f, pivotY),
            end = Offset(size.width, pivotY),
            strokeWidth = 1.5f,
        )

        // The solid pivot circle other pendulum circles will "hang" from — drawn last so it
        // sits on top of both guide lines at their intersection.
        drawGlowingPivot(center = Offset(centerX, pivotY))
    }
}

private fun DrawScope.drawGlowingPivot(center: Offset) {
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
    drawCircle(color = PIVOT_COLOR, radius = PIVOT_RADIUS, center = center) // solid — no Stroke style
}