package io.github.thanosfisherman.demo

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlinx.coroutines.isActive
import kotlin.math.roundToInt

/*
 * Extracted from BouncingBallsInVGame's game loop so every demo gets delta-time stepping and
 * FPS tracking for free instead of re-implementing withFrameNanos + dt clamping + FPS
 * accumulation each time.
 *
 * Deliberately does NOT own any scene/geometry setup (virtual viewports, wall geometry, ball
 * lists, whatever) — that stays specific to each demo, exactly like BouncingBallsInVGame
 * builds its own Scene independently. This only owns timing and the redraw trigger.
 *
 * Usage is the same shape as a plain Canvas, with one extra callback:
 *
 *   val loop = rememberGameLoopState()
 *   GameLoopCanvas(
 *       modifier = Modifier.fillMaxSize(),
 *       gameLoopState = loop,
 *       onUpdate = { dt -> ball.position += ball.velocity * dt },   // mutate your own state here
 *       onDraw = { drawCircle(color = Color.White, radius = 10f, center = ball.position) },
 *   )
 *   // elsewhere in the same composition, e.g. a debug overlay:
 *   Text("FPS: ${loop.fps}")
 */


@Stable
class GameLoopState internal constructor() {
    /** Recomputed once per second from actual measured frame times — see GameLoopCanvas's doc for why. */
    var fps: Int by mutableIntStateOf(0)
        internal set

    /** The clamped delta (seconds) used for the most recent onUpdate call. */
    var lastDeltaSeconds: Float by mutableFloatStateOf(0f)
        internal set

    /** Total frames stepped since this GameLoopCanvas started running. */
    var frameCount: Long by mutableLongStateOf(0L)
        internal set
}

@Composable
fun rememberGameLoopState(): GameLoopState = remember { GameLoopState() }

/**
 * A Canvas that drives itself with a delta-time game loop.
 *
 * @param maxDeltaSeconds clamps the per-frame delta passed to [onUpdate] and protects
 *   update logic from a single bad frame (a stall, a backgrounded tab) suddenly reporting a huge
 *   dt and causing a visible jump. This does NOT affect [GameLoopState.fps], which is measured
 *   from the real, unclamped frame times. Same "physics safety clamp vs. honest measurement"
 *   split BouncingBallsInVGame's own loop used.
 * @param gameLoopState could be hoisted (via [rememberGameLoopState]) if need to read
 *   fps/frameCount from elsewhere in your composition, e.g. a debug overlay.
 * @param onUpdate called once per frame, before [onDraw], with the clamped delta in seconds.
 * @param onDraw called once per frame, after [onUpdate], to render the current state.
 */
@Composable
fun GameLoopCanvas(
    modifier: Modifier = Modifier,
    maxDeltaSeconds: Float = 1f / 30f,
    gameLoopState: GameLoopState = rememberGameLoopState(),
    onUpdate: (deltaSeconds: Float) -> Unit,
    onDraw: DrawScope.() -> Unit,
) {
    var frameTick by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        var lastFrameTimeNanos = 0L

        // Rolling 1-second window
        var fpsAccumSeconds = 0f
        var fpsFrameCount = 0

        while (isActive) {
            withFrameNanos { frameTimeNanos ->
                val rawDt = if (lastFrameTimeNanos == 0L) {
                    0f
                } else {
                    (frameTimeNanos - lastFrameTimeNanos) / 1_000_000_000f
                }
                lastFrameTimeNanos = frameTimeNanos

                val dt = rawDt.coerceAtMost(maxDeltaSeconds)
                gameLoopState.lastDeltaSeconds = dt

                if (dt > 0f) {
                    onUpdate(dt)
                }

                if (rawDt > 0f) {
                    fpsAccumSeconds += rawDt
                    fpsFrameCount++
                    if (fpsAccumSeconds >= 1f) {
                        gameLoopState.fps = (fpsFrameCount / fpsAccumSeconds).roundToInt()
                        fpsAccumSeconds = 0f
                        fpsFrameCount = 0
                    }
                }

                gameLoopState.frameCount++
                frameTick = !frameTick // bump so the Canvas below knows to redraw
            }
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        @Suppress("UNUSED_EXPRESSION")
        frameTick
        onDraw()
    }
}