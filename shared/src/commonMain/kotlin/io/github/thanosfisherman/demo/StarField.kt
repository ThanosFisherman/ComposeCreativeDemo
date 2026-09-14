package io.github.thanosfisherman.demo

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.random.Random

data class Star(val position: Offset, val sizePx: Float, val alpha: Float)

fun generateStars(size: Size, count: Int = 100, seed: Long = 1132L): List<Star> {
    val random = Random(seed)
    return List(count) {
        Star(
            position = Offset(
                x = random.nextFloat() * size.width,
                y = random.nextFloat() * size.height,
            ),
            sizePx = random.nextFloat() * (1.5f - 0.5f) + 0.5f, // [0.5..1.5]
            alpha = random.nextFloat() * 0.5f + 0.3f,  // 0.3..0.8
        )
    }
}

fun DrawScope.drawStars(stars: List<Star>) {
    for (star in stars) {
        drawRect(
            color = Color.White.copy(alpha = star.alpha),
            topLeft = Offset(star.position.x - star.sizePx / 2f, star.position.y - star.sizePx / 2f),
            size = Size(star.sizePx, star.sizePx),
        )
    }
}