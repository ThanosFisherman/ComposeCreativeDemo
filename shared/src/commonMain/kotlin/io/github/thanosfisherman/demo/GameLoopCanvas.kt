package io.github.thanosfisherman.demo

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlinx.coroutines.isActive
import kotlin.math.roundToInt

/** Observable game-loop stats — read `fps`/`frameCount`/`lastDeltaSeconds` from anywhere. */
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
 * @param maxDeltaSeconds clamps the per-frame delta passed to [onUpdate] protects your own
 *   update logic from a single bad frame (a stall, a backgrounded tab) suddenly reporting a huge
 *   dt and causing a visible jump. This does NOT affect [GameLoopState.fps], which is measured
 *   from the real, unclamped frame times.
 * @param gameLoopState can be hoisted (via [rememberGameLoopState]) if needed, to read
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

    // LaunchedEffect(Unit) below only launches its coroutine ONCE and never restarts (that's
    // the whole point — recreating the loop every recomposition would be wasteful). But that
    // means if we captured `onUpdate` directly inside it, the coroutine would keep calling
    // whatever `onUpdate` closure existed at the very first composition, forever — ignoring
    // every newer one the caller passes on subsequent recompositions. rememberUpdatedState
    // gives us a stable reference that always reads through to the LATEST onUpdate, without
    // needing to restart the loop to get it.
    val currentOnUpdate by rememberUpdatedState(onUpdate)

    LaunchedEffect(Unit) {
        var lastFrameTimeNanos = 0L

        // Rolling 1-second window, same as BouncingBallsInVGame's original FPS logging.
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
                    currentOnUpdate(dt)
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
        // Reading frameTick subscribes this draw scope to it, so every increment from the
        // loop above triggers a fresh draw at a steady rate, same mechanism as before.
        @Suppress("UNUSED_EXPRESSION")
        frameTick

        // onDraw does NOT need the same rememberUpdatedState treatment, this whole Canvas
        // draw-content lambda is re-created fresh every time GameLoopCanvas recomposes, so it
        // already closes over whichever onDraw was most recently passed in. The staleness
        // problem only exists for things captured inside the once-only LaunchedEffect.
        onDraw()
    }
}