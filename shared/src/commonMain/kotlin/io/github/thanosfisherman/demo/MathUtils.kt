package io.github.thanosfisherman.demo

import kotlin.math.PI
import kotlin.math.sin

fun mapRange(inMin: Float, inMax: Float, outMin: Float, outMax: Float, value: Float): Float {
    val t = (value - inMin) / (inMax - inMin)
    return outMin + (outMax - outMin) * t
}

fun sinDeg(degrees: Float): Float = sin(degrees * (PI.toFloat() / 180f))

/** Linearly interpolates the wall's x position at a given y (p1/p2 can be in either y order). */
fun wallXAtY(wall: Wall, y: Float): Float {
    val denom = wall.p2.y - wall.p1.y
    if (kotlin.math.abs(denom) < 1e-6f) return wall.p1.x
    val t = ((y - wall.p1.y) / denom).coerceIn(0f, 1f)
    return wall.p1.x + (wall.p2.x - wall.p1.x) * t
}
