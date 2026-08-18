package com.leaf.osumania.math

import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.sqrt
import kotlin.math.pow
import kotlin.math.min
import kotlin.math.max
import kotlin.math.exp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.round

fun clamp(value: Float, min: Float, max: Float): Float {
    return max(min, min(max, value))
}

fun clampInt(value: Int, min: Int, max: Int): Int {
    return max(min, min(max, value))
}

fun mean(floats: FloatArray): Float {
    if (floats.isEmpty()) return 0f
    var sum = 0f
    for (f in floats) {
        sum += f
    }
    return sum / floats.size
}

fun stdev(floats: FloatArray): Float {
    if (floats.size < 2) return 0f
    val m = mean(floats)
    var sumSq = 0f
    for (f in floats) {
        val d = f - m
        sumSq += d * d
    }
    return sqrt(sumSq / floats.size)
}

fun roundTo(value: Float, decimals: Int): Float {
    val factor = 10f.pow(decimals.toFloat())
    return round(value * factor) / factor
}

fun lerp(a: Float, b: Float, t: Float): Float {
    return a + (b - a) * t
}

fun map(value: Float, inMin: Float, inMax: Float, outMin: Float, outMax: Float): Float {
    return outMin + (outMax - outMin) * ((value - inMin) / (inMax - inMin))
}

fun difficultyRange(difficulty: Float, min: Float, mid: Float, max: Float): Float {
    return when {
        difficulty > 5f -> lerp(mid, max, (difficulty - 5f) / 5f)
        difficulty < 5f -> lerp(min, mid, difficulty / 5f)
        else -> mid
    }
}
