package io.github.thanosfisherman.demo.pendulums

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

data class PendulumBall(
    val threadLength: Float,
    val speedModifier: Float,
    val color: Color,
    val initialRadius: Float,
    var pitch: Float,
    var phase: Float = 0f,
) {
    var previousSwingRad = 0f
    var position = Offset.Zero
    var pulse = 0f
    val radius: Float
        get() = initialRadius * (1f + pulse * 0.3f)
}