package io.github.thanosfisherman.demo


import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * Placeholder second demo — just a circle on a black background for now. Swap the contents of
 * this function out once you're ready to flesh it into whatever the real demo is.
 */
@Composable
fun CircleDemo(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        drawRect(color = Color.Black)
        drawCircle(
            color = Color.White,
            radius = size.minDimension * 0.2f,
            center = center,
        )
    }
}