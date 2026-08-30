package io.github.thanosfisherman.demo

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import kotlinx.coroutines.isActive
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.min
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
 * Fixed virtual viewport (like libGDX's FitViewport): the whole scene is
 * always built at a constant VIRTUAL_WIDTH x VIRTUAL_HEIGHT, so its
 * proportions never change. At draw time we compute a single uniform scale
 * factor — min(actualWidth / VIRTUAL_WIDTH, actualHeight / VIRTUAL_HEIGHT)
 * — and a centering offset, then draw everything through that transform.
 * Resizing/maximizing the real window just changes how much letterboxing
 * (or pillarboxing) there is around the same-shaped scene; nothing in the
 * scene itself stretches.
 *
 * Structure:
 *  - Ball: pure kinematic state (angle, forward, per-ball tuning + bounds)
 *  - Scene: geometry (walls) + all balls, always built at VIRTUAL_WIDTH x VIRTUAL_HEIGHT
 *  - LaunchedEffect(ballCount): (re)builds the scene when ball count changes
 *  - LaunchedEffect(Unit): the game loop — withFrameNanos + delta time
 *  - Canvas: computes the fit-viewport transform from its own actual size
 *    each frame, then draws the scene through it. Reads `frameTick` to know
 *    when to redraw.
 *
 * Tune the look via the constants below.
 */

// ---------- Virtual viewport ----------
private const val VIRTUAL_WIDTH = 1280f
private const val VIRTUAL_HEIGHT = 720f

// ---------- Tuning ----------
private const val PARTICLE_RADIUS = 18f
private const val ANGULAR_SPEED_DEG_PER_SEC = 180f // matches the original: a full 0->180 sweep in ~1s at speed 1.0
private const val SPEED_MODIFIER_MAX = 1f          // ball 0 (top row) sweeps fastest
private const val SPEED_MODIFIER_MIN = 0.75f       // last ball (bottom row) sweeps slowest
private const val MAX_DT = 1f / 30f                // clamp so a hitch doesn't blow up the sim
private const val PULSE_DECAY_PER_SEC = 5f         // how fast the on-bounce flash fades

private const val ROW_SPACING = 14f                // px between neighboring balls' rows — smaller = closer together
private const val FIRST_ROW_Y_FRACTION = 0.5f      // how far down the canvas the first (top) row starts
private const val ROW_TOP_MARGIN = 50f             // clearance between the first row and the wall's top edge
private const val ROW_APEX_MARGIN = 90f            // clearance between the last row and the apex, so it still has width there
private const val WALL_TOP_MIN_Y_FRACTION = 0.02f  // never let the wall's top edge go above this
private const val WALL_APEX_MAX_Y_FRACTION = 0.97f // never let the apex go below this
private const val WALL_HALF_WIDTH_FRACTION = 0.34f // how far topLeft/topRight sit from center, as a fraction of canvas width — smaller = pointier V

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
) {
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

    val ampMax = size.height * 0.11f
    val ampMin = size.height * 0.025f

    val balls = List(count) { i ->
        val t = if (count <= 1) 0f else i / (count - 1f)
        val rowY = firstRowY + i * ROW_SPACING

        val leftBound = wallXAtY(leftWall, rowY) + PARTICLE_RADIUS
        val rightBound = wallXAtY(rightWall, rowY) - PARTICLE_RADIUS

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
        )
    }

    return Scene(listOf(leftWall, rightWall), balls)
}

// ---------- Update (ported from the reference update() loop) ----------

private fun updateBall(ball: Ball, dt: Float, onBounce: (Ball) -> Unit) {
    if (ball.forward) {
        ball.angle += dt * ANGULAR_SPEED_DEG_PER_SEC * ball.speedModifier
        if (ball.angle > 180f) {
            ball.angle = 180f - (ball.angle - 180f) // mirror back into range
            ball.forward = false
            onBounce(ball)
        }
    } else {
        ball.angle -= dt * ANGULAR_SPEED_DEG_PER_SEC * ball.speedModifier
        if (ball.angle < 0f) {
            ball.angle = -ball.angle // mirror back into range
            ball.forward = true
            onBounce(ball)
        }
    }

    val x = mapRange(0f, 180f, ball.startingX, ball.finalX, ball.angle)
    val y = ball.startingY - sinDeg(ball.angle) * ball.amplitude // minus: arc bulges upward
    ball.position = Offset(x, y)

    ball.pulse = max(0f, ball.pulse - dt * PULSE_DECAY_PER_SEC)
}

// ---------- Rendering ----------

private fun DrawScope.drawBackground() {
    drawRect(color = Color.Black)
}

private fun DrawScope.drawWall(wall: Wall) {
    drawLine(color = Color.White.copy(alpha = 0.85f), start = wall.p1, end = wall.p2, strokeWidth = 2f)
}

/** Connects consecutive balls' centers — since each ball moves independently, this segment's
 *  length naturally contracts/expands frame to frame as the balls drift apart or together. */
private fun DrawScope.drawChainLines(balls: List<Ball>) {
    for (i in 0 until balls.size - 1) {
        drawLine(
            color = Color.White.copy(alpha = 0.35f),
            start = balls[i].position,
            end = balls[i + 1].position,
            strokeWidth = 2f,
        )
    }
}

private fun DrawScope.drawBall(ball: Ball) {
    val color = Color.hsv(ball.hue, 0.85f, 1f)
    val strokeWidth = 2.2f
    val radius = PARTICLE_RADIUS * (1f + ball.pulse * 0.3f)
    val glowRadius = radius * (1.5f + ball.pulse * 0.8f)

    val innerRadius = radius - strokeWidth / 2f
    val outerRadius = radius + strokeWidth / 2f

    drawCircle(
        brush = Brush.radialGradient(
            colorStops = arrayOf(
                0f to Color.Transparent,
                (innerRadius / glowRadius) to Color.Transparent,
                (outerRadius / glowRadius) to color.copy(
                    alpha = 0.55f + ball.pulse * 0.3f
                ),
                1f to Color.Transparent,
            ),
            center = ball.position,
            radius = glowRadius,
        ),
        radius = glowRadius,
        center = ball.position,
    )

    drawCircle(
        color = color,
        radius = radius,
        center = ball.position,
        style = Stroke(width = strokeWidth),
    )
}

// ---------- Composable ----------

@Composable
fun BouncingBallsInVGame(
    modifier: Modifier = Modifier,
    ballCount: Int = 10,
    onBounce: (ballIndex: Int) -> Unit = {},
) {
    var scene by remember { mutableStateOf<Scene?>(null) }
    var frameTick by remember { mutableStateOf(0L) }

    // Always built at the fixed virtual size, so it never depends on the actual window/layout size.
    LaunchedEffect(ballCount) {
        scene = buildScene(Size(VIRTUAL_WIDTH, VIRTUAL_HEIGHT), ballCount)
    }

    // The game loop: one step per display frame, advanced by measured delta time.
    LaunchedEffect(Unit) {
        var lastFrameTimeNanos = 0L
        while (isActive) {
            withFrameNanos { frameTimeNanos ->
                val dt = if (lastFrameTimeNanos == 0L) {
                    0f
                } else {
                    ((frameTimeNanos - lastFrameTimeNanos) / 1_000_000_000f).coerceAtMost(MAX_DT)
                }
                lastFrameTimeNanos = frameTimeNanos

                scene?.let { s ->
                    if (dt > 0f) {
                        s.balls.forEachIndexed { i, ball ->
                            updateBall(ball, dt) {
                                it.pulse = 1f
                                onBounce(i) // hook a real "beep" sound up here if you like
                            }
                        }
                    }
                }
                frameTick++ // bump so the Canvas below knows to redraw
            }
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        // Reading frameTick subscribes this draw scope to it, so every increment
        // from the game loop above triggers a fresh draw at a steady rate.
        @Suppress("UNUSED_EXPRESSION")
        frameTick

        drawBackground() // fills the whole actual canvas — doubles as the viewport's letterbox/pillarbox color

        val s = scene ?: return@Canvas
        if (size.width <= 0f || size.height <= 0f) return@Canvas

        // Fit-viewport transform: one uniform scale (no stretch), centered — same idea as libGDX's FitViewport.
        val fitScale = min(size.width / VIRTUAL_WIDTH, size.height / VIRTUAL_HEIGHT)
        val offsetX = (size.width - VIRTUAL_WIDTH * fitScale) / 2f
        val offsetY = (size.height - VIRTUAL_HEIGHT * fitScale) / 2f

        translate(left = offsetX, top = offsetY) {
            scale(fitScale, fitScale, pivot = Offset.Zero) {
                s.walls.forEach { drawWall(it) }
                drawChainLines(s.balls)
                s.balls.forEach { drawBall(it) }
            }
        }
    }
}

/*
 * Usage, anywhere in commonMain:
 *
 * @Composable
 * fun App() {
 *     BouncingBallsInVGame(
 *         modifier = Modifier.fillMaxSize(),
 *         ballCount = 3,
 *         onBounce = { index -> /* play your beep sound for this ball's pitch */ },
 *     )
 * }
 */