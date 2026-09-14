package io.github.thanosfisherman.demo.pendulums

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color

object PendulumsConfig {

    // ---------- Virtual viewport (same pattern as BouncingBallsInVGame) ----------
    val VIRTUAL_SIZE_LANDSCAPE = Size(1024f, 480f)
    val VIRTUAL_SIZE_PORTRAIT = Size(480f, 1024f)

    // ---------- Tuning ----------
    const val DEFAULT_PENDULUM_COUNT = 18
    const val DEFAULT_BIG_PENDULUM_COUNT = 6
    const val PIVOT_LINE_Y_FRACTION = 0.090f      // how far down from the top the pivot sits
    const val THREAD_LENGTH_MIN_FRACTION = 0.34f  // shortest thread, as a fraction of min(width, height)
    const val THREAD_LENGTH_MAX_FRACTION = 0.60f  // longest thread, as a fraction of min(width, height)
    const val LONG_THREAD_LENGTH_MIN_FRACTION = 0.6f  // shortest thread, as a fraction of min(width, height)
    const val LONG_THREAD_LENGTH_MAX_FRACTION = 0.82f  // longest thread, as a fraction of min(width, height)
    const val MAX_SWING_DEG = 80f                 // how far left/right of vertical the swing goes
    const val BIG_MAX_SWING_DEG = 60f
    const val ANGULAR_SPEED_RAD_PER_SEC = 0.8f
    const val BIG_ANGULAR_SPEED_RAD_PER_SEC = 0.2f
    const val SPEED_MODIFIER_MAX = 1f
    const val SPEED_MODIFIER_MIN = 0.65f
    const val LONG_SPEED_MODIFIER_MAX = 0.4f
    const val LONG_SPEED_MODIFIER_MIN = 0.2f
    const val PENDULUM_RADIUS = 8f
    const val BIG_PENDULUM_RADIUS = 24f
    const val PENDULUM_STROKE_WIDTH = 2.2f
    const val PULSE_DECAY_PER_SEC = 5f

    const val PIVOT_RADIUS = 14f
    const val PIVOT_GLOW_RADIUS_MULTIPLIER = 3f

    val GUIDE_LINE_COLOR = Color.White.copy(alpha = 0.55f)
    val THREAD_COLOR = Color.White.copy(alpha = 0.3f)
    val PIVOT_COLOR = Color(0xFF6EA8FF)
}