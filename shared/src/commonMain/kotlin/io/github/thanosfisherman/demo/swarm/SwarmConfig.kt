package io.github.thanosfisherman.demo.swarm

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color

object SwarmConfig {
    // ---------- Virtual viewport ----------
    val VIRTUAL_SIZE = Size(1000f, 1000f)

    // ---------- Tuning ----------
    const val DEFAULT_BALL_COUNT = 90
    const val BOUNDARY_RADIUS_FRACTION = 0.50f // of the shorter virtual dimension — leaves a visible margin
    const val BOUNDARY_STROKE_WIDTH = 2f
    const val BALL_BASE_RADIUS = 10f
    const val BALL_RADIUS_VARIATION = 0.6f     // balls range from 0.8x to 1.4x BALL_BASE_RADIUS
    const val BALL_STROKE_WIDTH = 2f
    const val SPEED_MIN = 30f                  // px/s (virtual units)
    const val SPEED_MAX = 90f
    const val FILLED_BALL_PROBABILITY = 0.55f  // rest render as outline-only rings, matching the reference image's mix
    const val PULSE_DECAY_PER_SEC = 5f         // same fade rate as the other demos' post-bounce flash
    const val PULSE_FLASH_THRESHOLD = 0.02f    // below this, skip the glow gradient entirely — see drawSwarmBall
    const val STAR_COUNT = 100
    const val STAR_SEED = 1L

    val BOUNDARY_COLOR = Color.White.copy(alpha = 0.7f)// ---------- Virtual viewport ----------
}