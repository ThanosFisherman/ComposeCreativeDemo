package io.github.thanosfisherman.demo

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.thanosfisherman.demo.audioUtils.MusicIntervals
import kotlinx.coroutines.isActive
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

/*
 * Ported from a libGDX "beeping balls" update() loop: this is deliberately
 * NOT a physics simulation. Each ball is driven purely kinematically by an
 * `angle` that sweeps 0..180 and bounces back (mirrors) whenever it hits
 * either end — that reflection IS the "hit the wall, change direction"
 * moment. Horizontal position is angle mapped linearly between the ball's
 * left/right bounds (which we derive from the V-wall geometry, per ball
 * row); vertical position is sin(angle) * amplitude, so each ball traces a
 * smooth arc between the two walls and back — no gravity, nothing falls in.
 *
 * The V's own geometry (how far down it starts, how tall it is) is derived
 * from the ball rows rather than fixed: rows are spaced a constant
 * ROW_SPACING apart starting at a constant fraction of the canvas height,
 * so more balls -> a taller wedge that still hugs the rows closely, fewer
 * balls -> a shorter one. That's what makes "the walls" respond to
 * `ballCount` instead of always spanning a fixed chunk of the screen.
 *
 * Fixed virtual viewport (like libGDX's FitViewport): the scene is always
 * built at one of two constant sizes — VIRTUAL_SIZE_LANDSCAPE or
 * VIRTUAL_SIZE_PORTRAIT, picked to match the device's current orientation —
 * so its proportions never change while that orientation holds. At draw
 * time we compute a single uniform scale factor — min(actualWidth /
 * virtualWidth, actualHeight / virtualHeight) — and a centering offset,
 * then draw everything through that transform. Resizing/maximizing the
 * real window just changes how much letterboxing (or pillarboxing) there
 * is around the same-shaped scene; nothing in the scene itself stretches.
 * Rotating the device swaps which fixed size is in play and rebuilds the
 * scene for it — that's the only time the scene's own shape changes.
 *
 * Structure:
 *  - Ball: pure kinematic state (angle, forward, per-ball tuning + bounds)
 *  - Scene: geometry (walls) + all balls, built at whichever virtual size matches orientation
 *  - LaunchedEffect(ballCount, isPortrait): (re)builds the scene when ball count or orientation changes
 *  - LaunchedEffect(Unit): the game loop — withFrameNanos + delta time
 *  - Canvas: computes the fit-viewport transform from its own actual size
 *    each frame, then draws the scene through it. Reads `frameTick` to know
 *    when to redraw.
 *
 * Tune the look via the constants below.
 */

// ---------- Virtual viewport ----------
// Two fixed virtual sizes, one per orientation, so the fit-viewport scale isn't fighting a
// mismatched aspect ratio. Using the landscape (wide) virtual size on a portrait phone is what
// made the scene look tiny — width became the limiting term in the fit-scale calculation, so
// almost all of the screen's height went unused as letterboxing. Each virtual size still keeps
// its own shape constant regardless of the real window size, exactly as before.
private val VIRTUAL_SIZE_LANDSCAPE = Size(1024f, 480f)
private val VIRTUAL_SIZE_PORTRAIT = Size(480f, 1024f)

// ---------- Tuning ----------
private const val PARTICLE_RADIUS = 12f
var ANGULAR_SPEED_DEG_PER_SEC = 160f // matches the original: a full 0->180 sweep in ~1s at speed 1.0
private const val SPEED_MODIFIER_MAX = 1f          // ball 0 (top row) sweeps fastest
private const val SPEED_MODIFIER_MIN = 0.75f       // last ball (bottom row) sweeps slowest
private const val MAX_DT = 1f / 30f                // clamp so a hitch doesn't blow up the sim
private const val PULSE_DECAY_PER_SEC = 3f         // how fast the on-bounce flash fades

private const val ROW_SPACING = 14f                // px between neighboring balls' rows — smaller = closer together
private const val FIRST_ROW_Y_FRACTION = 0.5f      // how far down the canvas the first (top) row starts
private const val ROW_TOP_MARGIN = 50f             // clearance between the first row and the wall's top edge
private const val ROW_APEX_MARGIN =
    90f            // clearance between the last row and the apex, so it still has width there
private const val WALL_TOP_MIN_Y_FRACTION = 0.02f  // never let the wall's top edge go above this
private const val WALL_APEX_MAX_Y_FRACTION = 0.97f // never let the apex go below this
private const val WALL_HALF_WIDTH_FRACTION =
    0.44f // how far topLeft/topRight sit from center, as a fraction of canvas width — smaller = pointier V
private const val AMPLITUDE_MAX_FRACTION =
    0.21f   // arc height (bow) for ball 0, as a fraction of canvas height — bigger = curvier
private const val AMPLITUDE_MIN_FRACTION =
    0.055f  // arc height for the last ball — bigger = curvier, and closer to AMPLITUDE_MAX_FRACTION = less taper across the row

// ---------- Geometry ----------

private data class Wall(val p1: Offset, val p2: Offset)

/** Linearly interpolates the wall's x position at a given y (p1/p2 can be in either y order). */
private fun wallXAtY(wall: Wall, y: Float): Float {
    val denom = wall.p2.y - wall.p1.y
    if (kotlin.math.abs(denom) < 1e-6f) return wall.p1.x
    val t = ((y - wall.p1.y) / denom).coerceIn(0f, 1f)
    return wall.p1.x + (wall.p2.x - wall.p1.x) * t
}

private fun mapRange(inMin: Float, inMax: Float, outMin: Float, outMax: Float, value: Float): Float {
    val t = (value - inMin) / (inMax - inMin)
    return outMin + (outMax - outMin) * t
}

private fun sinDeg(degrees: Float): Float = sin(degrees * (PI.toFloat() / 180f))

// ---------- Ball ----------

private class Ball(
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

private class Scene(val walls: List<Wall>, val balls: List<Ball>)

private fun buildScene(size: Size, ballCount: Int): Scene {
    val count = ballCount.coerceAtLeast(1)

    // Rows are packed a constant ROW_SPACING apart starting at a constant fraction of
    // the canvas height, so the vertical extent they occupy grows with `count`.
    val firstRowY = size.height * FIRST_ROW_Y_FRACTION
    val lastRowY = firstRowY + (count - 1) * ROW_SPACING

    // The wall geometry hugs that extent, with a small margin on each end.
    val topY = (firstRowY - ROW_TOP_MARGIN).coerceAtLeast(size.height * WALL_TOP_MIN_Y_FRACTION)
    val apexY = (lastRowY + ROW_APEX_MARGIN).coerceAtMost(size.height * WALL_APEX_MAX_Y_FRACTION)

    val apex = Offset(size.width / 2f, apexY)
    val topLeft = Offset(size.width * (0.5f - WALL_HALF_WIDTH_FRACTION), topY)
    val topRight = Offset(size.width * (0.5f + WALL_HALF_WIDTH_FRACTION), topY)

    val leftWall = Wall(apex, topLeft)
    val rightWall = Wall(apex, topRight)

    val ampMax = size.height * AMPLITUDE_MAX_FRACTION
    val ampMin = size.height * AMPLITUDE_MIN_FRACTION

    val balls = List(count) { i ->
        val t = if (count <= 1) 0f else i / (count - 1f)
        val rowY = firstRowY + i * ROW_SPACING

        val leftBound = wallXAtY(leftWall, rowY) + PARTICLE_RADIUS
        val rightBound = wallXAtY(rightWall, rowY) - PARTICLE_RADIUS
        val notes = MusicIntervals.TRITONE_SCALE
        val noteIndex = i % notes.size
        Ball(
            startingX = leftBound,
            finalX = rightBound,
            startingY = rowY,
            amplitude = ampMax - (ampMax - ampMin) * t,
            speedModifier = mapRange(
                0f,
                (count - 1).coerceAtLeast(1).toFloat(),
                SPEED_MODIFIER_MAX,
                SPEED_MODIFIER_MIN,
                i.toFloat()
            ),
            hue = t * 300f,
            pitch = notes[noteIndex]
        )
    }

    return Scene(listOf(leftWall, rightWall), balls)
}

// ---------- Update (ported from the reference update() loop) ----------

private fun updateBall(ball: Ball, dt: Float, onBounce: (Float) -> Unit) {
    if (ball.forward) {
        ball.angle += dt * ANGULAR_SPEED_DEG_PER_SEC * ball.speedModifier
        if (ball.angle > 180f) {
            ball.angle = 180f - (ball.angle - 180f) // mirror back into range
            ball.forward = false
            ball.pulse = 1f
            onBounce(ball.pitch)
        }
    } else {
        ball.angle -= dt * ANGULAR_SPEED_DEG_PER_SEC * ball.speedModifier
        if (ball.angle < 0f) {
            ball.angle = -ball.angle // mirror back into range
            ball.forward = true
            ball.pulse = 1f
            onBounce(ball.pitch)
        }
    }

    val x = mapRange(0f, 180f, ball.startingX, ball.finalX, ball.angle)
    val y = ball.startingY - sinDeg(ball.angle) * ball.amplitude // minus: arc bulges upward
    ball.position = Offset(x, y)

    ball.pulse = max(0f, ball.pulse - dt * PULSE_DECAY_PER_SEC)
}

// ---------- Rendering ----------

// Reused every frame instead of reallocated — these never change, so building them once is free.
private val WALL_COLOR = Color.White.copy(alpha = 0.85f)
private val CHAIN_LINE_COLOR = Color.White.copy(alpha = 0.7f)
private const val BALL_STROKE_WIDTH = 2.2f
private val BALL_STROKE = Stroke(width = BALL_STROKE_WIDTH)
private val DEBUG_TEXT_STYLE = TextStyle(color = Color.White, fontSize = 14.sp, fontFamily = FontFamily.Monospace)

private fun DrawScope.drawBackground() {
    drawRect(color = Color.Black)
}

private fun DrawScope.drawWall(wall: Wall) {
    drawLine(color = WALL_COLOR, start = wall.p1, end = wall.p2, strokeWidth = 2f)
}

/** Connects consecutive balls' centers — since each ball moves independently, this segment's
 *  length naturally contracts/expands frame to frame as the balls drift apart or together. */
private fun DrawScope.drawChainLines(balls: List<Ball>) {
    for (i in 0 until balls.size - 1) {
        drawLine(
            color = CHAIN_LINE_COLOR,
            start = balls[i].position,
            end = balls[i + 1].position,
            strokeWidth = 2f,
        )
    }
}

private const val PULSE_FLASH_THRESHOLD = 0.02f // below this, treat the ball as "at rest" for rendering purposes

/**
 * Renders one ball. The ring-shaped glow (transparent center, transparent up to the stroke's
 * inner edge, a colored band across the stroke, fading back to transparent outside it) is
 * the expensive part — a fresh Brush + colorStops array every time it's built — so it's only
 * actually built during the brief post-bounce "flash" (pulse > PULSE_FLASH_THRESHOLD, roughly
 * a dozen frames right after a bounce). The rest of the time — the vast majority of frames,
 * since most balls are mid-swing rather than mid-bounce at any given moment — the glow is
 * approximated with a couple of flat, alpha-stepped circles instead, which allocate nothing
 * beyond the Color value itself. This trades a slightly less exact "resting" glow for a much
 * lower steady-state allocation rate; the bounce flash itself (arguably the moment worth
 * spending on) still looks exactly as before.
 */
private fun DrawScope.drawBall(ball: Ball) {
    val radius = PARTICLE_RADIUS * (1f + ball.pulse * 0.3f)

    if (ball.pulse > PULSE_FLASH_THRESHOLD) {
        val glowRadius = radius * (1.5f + ball.pulse * 0.8f)
        val innerRadius = radius - BALL_STROKE_WIDTH / 2f
        val outerRadius = radius + BALL_STROKE_WIDTH / 2f

        drawCircle(
            brush = Brush.radialGradient(
                colorStops = arrayOf(
                    0f to Color.Transparent,
                    (innerRadius / glowRadius) to Color.Transparent,
                    (outerRadius / glowRadius) to ball.color.copy(alpha = 0.55f + ball.pulse * 0.3f),
                    1f to Color.Transparent,
                ),
                center = ball.position,
                radius = glowRadius,
            ),
            radius = glowRadius,
            center = ball.position,
        )
    } else {
        drawCircle(color = ball.color, radius = radius, center = ball.position, style = BALL_STROKE)
    }
}

// ---------- Composable ----------

@Composable
fun BouncingBallsInVGame(
    modifier: Modifier = Modifier,
    ballCount: Int = 10,
    onBounce: (freq: Float) -> Unit = {},
) {
    var isPortrait by remember { mutableStateOf(false) }
    var scene by remember { mutableStateOf<Scene?>(null) }
    var frameTick by remember { mutableStateOf(false) }
    var isAssigned by remember { mutableStateOf(false) }
    var fps by remember { mutableIntStateOf(0) } // updated once/sec — cheap to recompose on, unlike frameTick
    var scaleLabel by remember { mutableStateOf("TRITONE SCALE") }
    var ballSpeed by remember { mutableStateOf("") }
    var dt by remember { mutableFloatStateOf(0f) }
    // Always built at a fixed virtual size, so it never depends on the actual window/layout
    // size — but which fixed size depends on orientation, so it keeps rebuilding (only) when
    // the device actually rotates between portrait and landscape, or ballCount changes.
    LaunchedEffect(ballCount, isPortrait) {
        val virtualSize = if (isPortrait) VIRTUAL_SIZE_PORTRAIT else VIRTUAL_SIZE_LANDSCAPE
        scene = buildScene(virtualSize, ballCount)
    }

    // The game loop: one step per display frame, advanced by measured delta time.
    LaunchedEffect(Unit) {
        var lastFrameTimeNanos = 0L

        // FPS logging: accumulated over a rolling 1-second window using the *raw*
        // (unclamped) frame delta — MAX_DT below is a physics safety clamp, not a
        // measurement of what the display is actually doing.
        var fpsAccumSeconds = 0f
        var fpsFrameCount = 0
        var timer = 0f

        // Track the active state index so logic only triggers on changes
        var currentScaleIndex = -1
        var currentSpeedIndex = -1

        while (isActive) {
            withFrameNanos { frameTimeNanos ->
                val rawDt = if (lastFrameTimeNanos == 0L) {
                    0f
                } else {
                    (frameTimeNanos - lastFrameTimeNanos) / 1_000_000_000f
                }
                dt = rawDt.coerceAtMost(MAX_DT)
                lastFrameTimeNanos = frameTimeNanos

                scene?.let { s ->
                    timer += dt
                    // 1. Scale / Pitch updates (every 30 seconds)
                    val scaleIndex = (timer / 30f).toInt() % 2
                    if (scaleIndex != currentScaleIndex) {
                        currentScaleIndex = scaleIndex
                        val scale = when (scaleIndex) {
                            0 -> {
                                //println("Tritone")
                                scaleLabel = "TRITONE SCALE"
                                MusicIntervals.TRITONE_SCALE
                            }

                            else -> {
                                // println("Whole Tone")
                                scaleLabel = "WHOLE TONE SCALE"
                                MusicIntervals.WHOLE_TONE_SCALE
                            }
                        }
                        s.balls.forEachIndexed { index, ball ->
                            ball.pitch = scale[index % scale.size]
                        }
                    }

                    // 2. Speed updates (every 15 seconds)
                    val speedIndex = (timer / 15f).toInt() % 3
                    if (speedIndex != currentSpeedIndex) {
                        currentSpeedIndex = speedIndex
                        ANGULAR_SPEED_DEG_PER_SEC = when (speedIndex) {
                            0 -> {
                                ballSpeed = "MEDIUM"
                                120f
                            }

                            1 -> {
                                ballSpeed = "FAST"
                                180f
                            }

                            else -> {
                                ballSpeed = "SLOW"
                                90f
                            }
                        }
                    }
                }

                if (rawDt > 0f) {
                    fpsAccumSeconds += rawDt
                    fpsFrameCount++
                    if (fpsAccumSeconds >= 1f) {
                        val measuredFps = fpsFrameCount / fpsAccumSeconds
                        fps = measuredFps.roundToInt()
                        //println("FPS: ${(fps * 10f).roundToInt() / 10f}")
                        fpsAccumSeconds = 0f
                        fpsFrameCount = 0
                    }
                }
                frameTick = !frameTick // bump so the Canvas below knows to redraw
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        Canvas(
            modifier = modifier
                .fillMaxSize()
                .onSizeChanged { isPortrait = it.height > it.width }
        ) {
            // Reading frameTick subscribes this draw scope to it, so every increment
            // from the game loop above triggers a fresh draw at a steady rate.
            @Suppress("UNUSED_EXPRESSION")
            frameTick

            drawBackground() // fills the whole actual canvas — doubles as the viewport's letterbox/pillarbox color

            val s = scene ?: return@Canvas
            if (size.width <= 0f || size.height <= 0f) return@Canvas

            val virtualSize = if (isPortrait) VIRTUAL_SIZE_PORTRAIT else VIRTUAL_SIZE_LANDSCAPE

            // Fit-viewport transform: one uniform scale (no stretch), centered — same idea as libGDX's FitViewport.
            val fitScale = min(size.width / virtualSize.width, size.height / virtualSize.height)
            val offsetX = (size.width - virtualSize.width * fitScale) / 2f
            val offsetY = (size.height - virtualSize.height * fitScale) / 2f

            translate(left = offsetX, top = offsetY) {
                scale(fitScale, fitScale, pivot = Offset.Zero) {
                    for (i in 0 until s.balls.size - 1) {
                        updateBall(s.balls[i], dt, onBounce)
                        drawBall(s.balls[i])
                    }
                    updateBall(s.balls[s.balls.size - 1], dt, onBounce)
                    drawBall(s.balls[s.balls.size - 1])
                    s.walls.forEach { drawWall(it) }
                    drawChainLines(s.balls)
                }
            }
        }

        // Debug overlay — plain Compose text on top of the Canvas, not drawn via DrawScope,
        // so it costs nothing extra on the per-frame render path (only "FPS" recomposes,
        // and only once a second).
        //
        // safeDrawingPadding() (instead of a flat 8.dp) is what keeps this clear of the status
        // bar / cutouts on Android — Canvas draws full-bleed behind system bars, so without
        // this the first line sits right under (or behind) the status bar. On Desktop/Web,
        // where there's no such inset, this resolves to 0dp and behaves exactly like before.
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .safeDrawingPadding()
                .padding(8.dp)
        ) {
            BasicText(text = "Balls of Fury - Thanos Psaridis", style = DEBUG_TEXT_STYLE)
            BasicText(text = "FPS: $fps", style = DEBUG_TEXT_STYLE)
            BasicText(text = "Scale: $scaleLabel", style = DEBUG_TEXT_STYLE)
            BasicText(text = "Ball speed: $ballSpeed", style = DEBUG_TEXT_STYLE)
        }
    }
}