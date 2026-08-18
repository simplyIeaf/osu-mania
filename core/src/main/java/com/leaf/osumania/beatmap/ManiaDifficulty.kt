package com.leaf.osumania.beatmap

import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.max

private const val DIFFICULTY_MULTIPLIER = 0.018f
private const val INDIVIDUAL_DECAY_BASE = 0.125f
private const val OVERALL_DECAY_BASE = 0.3f
private const val DECAY_WEIGHT = 0.9f
private const val SECTION_LENGTH = 400f
private const val RELEASE_THRESHOLD = 30f

private fun applyDecay(value: Float, deltaTime: Float, decayBase: Float): Float {
    return value * decayBase.pow((deltaTime / 1000f).toDouble()).toFloat()
}

private fun evaluateIndividualDifficulty(holdFactor: Float): Float {
    return 2f * holdFactor
}

private fun evaluateOverallDifficulty(holdAddition: Float, holdFactor: Float): Float {
    return (1f + holdAddition) * holdFactor
}

private fun logistic(x: Float, midpointOffset: Float, multiplier: Float, maxValue: Float = 1f): Float {
    return maxValue / (1f + exp((multiplier * (midpointOffset - x)).toDouble())).toFloat()
}

data class DifficultyHitObject(
    val baseObject: TapData,
    val deltaTime: Float,
    val columnStrainTime: Float,
    val column: Int
)

fun calculateStarRating(hitObjects: List<TapData>, keyCount: Int): Float {
    if (hitObjects.isEmpty()) return 0f

    val sorted = hitObjects.sortedBy { it.time }

    val diffObjects = mutableListOf<DifficultyHitObject>()
    val lastTimePerColumn = FloatArray(keyCount) { -1000f }

    for (i in sorted.indices) {
        val obj = sorted[i]
        val col = obj.column.coerceIn(0, keyCount - 1)
        val prevTime = lastTimePerColumn[col]
        val colStrainTime = max(obj.time - prevTime, 1f)
        val deltaTime = if (i > 0) max(obj.time - sorted[i - 1].time, 1f) else 1f

        diffObjects.add(
            DifficultyHitObject(
                baseObject = obj,
                deltaTime = deltaTime,
                columnStrainTime = colStrainTime,
                column = col
            )
        )
        lastTimePerColumn[col] = obj.time
    }

    val individualStrains = Array(keyCount) { FloatArray(diffObjects.size) }
    val overallStrain = FloatArray(diffObjects.size)

    individualStrains.forEach { it[0] = 1f }
    overallStrain[0] = 1f

    for (i in 1 until diffObjects.size) {
        val obj = diffObjects[i]
        val col = obj.column

        for (c in 0 until keyCount) {
            individualStrains[c][i] = applyDecay(individualStrains[c][i - 1], obj.deltaTime, INDIVIDUAL_DECAY_BASE)
        }

        overallStrain[i] = applyDecay(overallStrain[i - 1], obj.deltaTime, OVERALL_DECAY_BASE)

        var holdAddition = 0f
        var holdFactor = 1f
        val strainTime = obj.columnStrainTime

        holdFactor = 1f + logistic(strainTime, 1000f, 0.03f, 2f)
        holdAddition = 0.5f * logistic(strainTime, 600f, 0.005f, 1f)

        val individualStrainVal = evaluateIndividualDifficulty(holdFactor)
        val overallStrainVal = evaluateOverallDifficulty(holdAddition, holdFactor)

        individualStrains[col][i] += individualStrainVal * DIFFICULTY_MULTIPLIER
        overallStrain[i] += overallStrainVal * DIFFICULTY_MULTIPLIER
    }

    val sectionCount = ((diffObjects.last().baseObject.time - diffObjects.first().baseObject.time) / SECTION_LENGTH).toInt() + 1
    val peakStrains = FloatArray(max(sectionCount, 1))

    var sectionIndex = 0
    var sectionTime = diffObjects.first().baseObject.time
    var currentMaxOverall = 0f
    val currentMaxIndividual = FloatArray(keyCount) { 0f }

    for (i in diffObjects.indices) {
        val obj = diffObjects[i]
        if (obj.baseObject.time - sectionTime >= SECTION_LENGTH) {
            peakStrains[sectionIndex] = max(currentMaxOverall, currentMaxIndividual.maxOrNull() ?: 0f)
            sectionIndex++
            sectionTime = obj.baseObject.time
            currentMaxOverall = 0f
            for (c in 0 until keyCount) {
                currentMaxIndividual[c] = 0f
            }
        }

        currentMaxOverall = max(currentMaxOverall, overallStrain[i])
        currentMaxIndividual[obj.column] = max(currentMaxIndividual[obj.column], individualStrains[obj.column][i])
    }

    if (sectionIndex < peakStrains.size) {
        peakStrains[sectionIndex] = max(currentMaxOverall, currentMaxIndividual.maxOrNull() ?: 0f)
    }

    var difficulty = 0f
    var weight = 1f
    val sortedStrains = peakStrains.filter { it > 0f }.sortedDescending()

    for (strain in sortedStrains) {
        difficulty += strain * weight
        weight *= DECAY_WEIGHT
    }

    return difficulty
}
