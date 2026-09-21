package io.github.thanosfisherman.demo.swarm

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

class SwarmBall(
    var position: Offset,
    var velocity: Offset,
    val radius: Float,
    val color: Color,
    val filled: Boolean,
    var pitch: Float,
) {
   var pulse = 0f // 0..1, flashes on each boundary bounce and decays
}