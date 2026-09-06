package io.github.thanosfisherman.demo

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

data class Ball(
    val startingX: Float,
    val finalX: Float,
    val startingY: Float,
    val amplitude: Float,
    val speedModifier: Float,
    val hue: Float,
    var pitch: Float
) {
    val color: Color = Color.hsv(hue, 0.85f, 1f) // computed once — hue never changes after construction
    var angle = 0f
    var forward = true
    var pulse = 0f // 0..1, flashes on bounce and decays
    var position = Offset(startingX, startingY)
}