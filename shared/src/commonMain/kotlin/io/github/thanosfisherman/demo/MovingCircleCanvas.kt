package io.github.thanosfisherman.demo

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

@Composable
fun ConstantSpeedCircleCanvas() {
    val density = LocalDensity.current

    // 1. Define physical speed (e.g., 300 independent pixels per second)
    val speedPxPerSec = remember(density) { with(density) { 300.dp.toPx() } }
    val circleRadius = 50f

    // 2. Track current position and direction vector
    var position by remember { mutableStateOf(Offset(200f, 200f)) }
    var directionX by remember { mutableStateOf(1f) } // 1 = right, -1 = left

    // 3. The Game Loop: Runs every single frame frame-rate independently
    LaunchedEffect(Unit) {
        var lastTimeNanos = 0L

        while (true) {
            withFrameNanos { frameTimeNanos ->
                if (lastTimeNanos != 0L) {
                    // Calculate exactly how much time passed since the last frame
                    val timeDeltaSec = (frameTimeNanos - lastTimeNanos) / 1_000_000_000f

                    // Move the position based strictly on velocity * time elapsed
                    position = position.copy(
                        x = position.x + (directionX * speedPxPerSec * timeDeltaSec)
                    )
                }
                lastTimeNanos = frameTimeNanos
            }
        }
    }

    // 4. Draw & check bounds reactively
    Canvas(modifier = Modifier.fillMaxSize()) {
        val minX = circleRadius
        val maxX = size.width - circleRadius

        // Handle wall collisions and reverse direction safely
        if (position.x > maxX && directionX > 0f) {
            position = position.copy(x = maxX)
            directionX = -1f
        } else if (position.x < minX && directionX < 0f) {
            position = position.copy(x = minX)
            directionX = 1f
        }

        // Always clamp centerY to current screen middle
        val centerY = size.height / 2

        drawCircle(
            color = Color(0xFF6200EE),
            radius = circleRadius,
            center = Offset(position.x, centerY)
        )
    }
}

@Composable
fun ZeroAllocationMovingCircle() {
    val density = LocalDensity.current
    val speedPxPerSec = remember(density) { with(density) { 300.dp.toPx() } }
    val circleRadius = 50f

    // Use separate primitive float states instead of an Offset object wrapper
    var xPos by remember { mutableFloatStateOf(200f) }
    var yPos by remember { mutableFloatStateOf(200f) }
    var directionX by remember { mutableFloatStateOf(1f) } // 1f or -1f

    LaunchedEffect(Unit) {
        var lastTimeNanos = 0L

        while (true) {
            withFrameNanos { frameTimeNanos ->
                if (lastTimeNanos != 0L) {
                    val timeDeltaSec = (frameTimeNanos - lastTimeNanos) / 1_000_000_000f

                    // Directly increment the primitive Float state values
                    xPos += directionX * speedPxPerSec * timeDeltaSec
                }
                lastTimeNanos = frameTimeNanos
            }
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val minX = circleRadius
        val maxX = size.width - circleRadius

        // Collision logic modifies the primitives directly
        if (xPos > maxX && directionX > 0f) {
            xPos = maxX
            directionX = -1f
        } else if (xPos < minX && directionX < 0f) {
            xPos = minX
            directionX = 1f
        }

        // Snap Y cleanly to center
        yPos = size.height / 2

        // Draw requires an Offset, but it is created inside the DrawScope frame allocation layout
        drawCircle(
            color = Color(0xFF6200EE),
            radius = circleRadius,
            center = Offset(xPos, yPos)
        )
    }
}

@Composable
fun MovingCircleCanvas() {
    // 1. Create an infinite transition for looping animations
    val infiniteTransition = rememberInfiniteTransition(label = "CircleMovement")

    // 2. Animate a progress fraction from 0.0 (left) to 1.0 (right)
    val animationProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse // Bounces back and forth
        ),
        label = "HorizontalProgress"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        // 3. Define dimensions dynamically based on parent container size
        val circleRadius = 50f
        val padding = circleRadius + 20f

        val startX = padding
        val endX = size.width - padding
        val centerY = size.height / 2

        // 4. Calculate the real-time animated X coordinate
        val currentX = startX + (endX - startX) * animationProgress

        // 5. Draw the circle at the changing coordinates
        drawCircle(
            color = Color(0xFF6200EE), // Purple
            radius = circleRadius,
            center = Offset(x = currentX, y = centerY)
        )
    }
}