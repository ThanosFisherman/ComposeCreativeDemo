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
import androidx.compose.ui.layout.onSizeChanged
import kotlinx.coroutines.isActive
import kotlin.math.PI
import kotlin.math.max
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
 * A dense stack of balls (small vertical stagger between rows, decreasing
 * speed/amplitude by index) is what reads visually as one continuous curvy
 * "rope" rather than a physically-linked chain.
 *
 * Structure:
 *  - Ball: pure kinematic state (angle, forward, per-ball tuning + bounds)
 *  - Scene: geometry (walls) + all balls for the current canvas size
 *  - LaunchedEffect(canvasSize): (re)builds the scene when layout size is known/changes
 *  - LaunchedEffect(Unit): the game loop — withFrameNanos + delta time
 *  - Canvas: pure rendering, reads `frameTick` to know when to redraw
 *
 * Tune the look via the constants below.
 */

// ---------- Tuning ----------
private const val BALL_COUNT = 3
private const val PARTICLE_RADIUS = 18f
private const val ANGULAR_SPEED_DEG_PER_SEC = 90f // matches the original: a full 0->180 sweep in ~1s at speed 1.0
private const val SPEED_MODIFIER_MAX = 1f          // ball 0 (top row) sweeps fastest
private const val SPEED_MODIFIER_MIN = 0.75f       // last ball (bottom row) sweeps slowest
private const val MAX_DT = 1f / 30f                // clamp so a hitch doesn't blow up the sim
private const val PULSE_DECAY_PER_SEC = 5f         // how fast the on-bounce flash fades

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

private fun buildScene(size: Size): Scene {
    val apex = Offset(size.width / 2f, size.height * 0.92f)
    val topLeft = Offset(size.width * 0.06f, size.height * 0.05f)
    val topRight = Offset(size.width * 0.94f, size.height * 0.05f)

    val leftWall = Wall(apex, topLeft)
    val rightWall = Wall(apex, topRight)

    val topRowY = size.height * 0.16f
    val bottomRowY = size.height * 0.62f
    val ampMax = size.height * 0.11f
    val ampMin = size.height * 0.025f

    val balls = List(BALL_COUNT) { i ->
        val t = i / (BALL_COUNT - 1f)
        val rowY = topRowY + (bottomRowY - topRowY) * t

        val leftBound = wallXAtY(leftWall, rowY) + PARTICLE_RADIUS
        val rightBound = wallXAtY(rightWall, rowY) - PARTICLE_RADIUS

        Ball(
            startingX = leftBound,
            finalX = rightBound,
            startingY = rowY,
            amplitude = ampMax - (ampMax - ampMin) * t,
            speedModifier = mapRange(
                0f,
                (BALL_COUNT - 1).coerceAtLeast(1).toFloat(),
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
fun BouncingBallsInVGame(modifier: Modifier = Modifier, onBounce: (ballIndex: Int) -> Unit = {}) {
    var canvasSize by remember { mutableStateOf(Size.Zero) }
    var scene by remember { mutableStateOf<Scene?>(null) }
    var frameTick by remember { mutableStateOf(0L) }

    // (Re)build the V geometry + balls whenever the layout size becomes known / changes.
    LaunchedEffect(canvasSize) {
        if (canvasSize.width > 0f && canvasSize.height > 0f) {
            scene = buildScene(canvasSize)
        }
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

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { canvasSize = Size(it.width.toFloat(), it.height.toFloat()) }
    ) {
        // Reading frameTick subscribes this draw scope to it, so every increment
        // from the game loop above triggers a fresh draw at a steady rate.
        @Suppress("UNUSED_EXPRESSION")
        frameTick

        drawBackground()
        val s = scene ?: return@Canvas
        s.walls.forEach { drawWall(it) }
        s.balls.forEach { drawBall(it) }
    }
}

/*
 * Usage, anywhere in commonMain:
 *
 * @Composable
 * fun App() {
 *     BouncingBallsInVGame(
 *         modifier = Modifier.fillMaxSize(),
 *         onBounce = { index -> /* play your beep sound for this ball's pitch */ },
 *     )
 * }
 */