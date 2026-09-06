package io.github.thanosfisherman.demo

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp

object Config {

    // ---------- Tuning ----------
    const val PARTICLE_RADIUS = 12f
    var ANGULAR_SPEED_DEG_PER_SEC = 160f // matches the original: a full 0->180 sweep in ~1s at speed 1.0
    const val SPEED_MODIFIER_MAX = 1f          // ball 0 (top row) sweeps fastest
    const val SPEED_MODIFIER_MIN = 0.75f       // last ball (bottom row) sweeps slowest
    const val MAX_DT = 1f / 30f                // clamp so a hitch doesn't blow up the sim
    const val PULSE_DECAY_PER_SEC = 4f         // how fast the on-bounce flash fades

    const val ROW_SPACING = 14f                // px between neighboring balls' rows — smaller = closer together
    const val FIRST_ROW_Y_FRACTION = 0.5f      // how far down the canvas the first (top) row starts
    const val ROW_TOP_MARGIN = 50f             // clearance between the first row and the wall's top edge
    const val ROW_APEX_MARGIN =
        90f            // clearance between the last row and the apex, so it still has width there
    const val WALL_TOP_MIN_Y_FRACTION = 0.02f  // never let the wall's top edge go above this
    const val WALL_APEX_MAX_Y_FRACTION = 0.97f // never let the apex go below this
    const val WALL_HALF_WIDTH_FRACTION =
        0.44f // how far topLeft/topRight sit from center, as a fraction of canvas width — smaller = pointier V
    const val AMPLITUDE_MAX_FRACTION =
        0.21f   // arc height (bow) for ball 0, as a fraction of canvas height — bigger = curvier
    const val AMPLITUDE_MIN_FRACTION =
        0.055f  // arc height for the last ball — bigger = curvier, and closer to AMPLITUDE_MAX_FRACTION = less taper across the row

    // ---------- Rendering ----------

    val WALL_COLOR = Color.White.copy(alpha = 0.85f)
    val CHAIN_LINE_COLOR = Color.White.copy(alpha = 0.7f)
    const val BALL_STROKE_WIDTH = 1f
    val BALL_STROKE = Stroke(width = BALL_STROKE_WIDTH)
    val DEBUG_TEXT_STYLE = TextStyle(color = Color.White, fontSize = 14.sp, fontFamily = FontFamily.Monospace)
    private const val PULSE_FLASH_THRESHOLD = 0.02f // below this, treat the ball as "at rest" for rendering purposes
}