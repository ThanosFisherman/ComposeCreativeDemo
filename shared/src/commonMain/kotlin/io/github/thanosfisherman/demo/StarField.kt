package io.github.thanosfisherman.demo

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.random.Random


data class Star(val position: Offset, val sizePx: Float, val alpha: Float)

fun generateStars(size: Size, count: Int = 100, seed: Long = 1L): List<Star> {
    val random = Random(seed)
    return List(count) {
        Star(
            position = Offset(
                x = random.nextFloat() * size.width,
                y = random.nextFloat() * size.height,
            ),
            sizePx = random.nextFloat() * 1.5f + 0.5f, // 0.5..2.0 — genuinely tiny; square vs. round is invisible at this size
            alpha = random.nextFloat() * 0.5f + 0.3f,  // 0.3..0.8 — subtle brightness variance, still all white
        )
    }
}

/**
 * Draws a fixed set of tiny white star squares. Call this INSIDE your fit-viewport transform
 * (after your background fill, which should stay outside it so it still covers letterboxing).
 */
fun DrawScope.drawStars(stars: List<Star>) {
    for (star in stars) {
        drawRect(
            color = Color.White.copy(alpha = star.alpha),
            topLeft = Offset(star.position.x - star.sizePx / 2f, star.position.y - star.sizePx / 2f),
            size = Size(star.sizePx, star.sizePx),
        )
    }
}