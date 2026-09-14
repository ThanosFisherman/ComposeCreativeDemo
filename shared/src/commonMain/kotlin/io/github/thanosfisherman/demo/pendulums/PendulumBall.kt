package io.github.thanosfisherman.demo.pendulums

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import io.github.thanosfisherman.demo.pendulums.PendulumsConfig.PENDULUM_RADIUS

data class PendulumBall(
    val threadLength: Float,
    val speedModifier: Float,
    val color: Color,
    var phase: Float = 0f,
    var pitch: Float,
) {
    var previousSwingRad = 0f
    var position = Offset.Zero
    var pulse = 0f
    val radius: Float
        get() = PENDULUM_RADIUS * (1f + pulse * 0.3f)
}