package com.leaf.osumania.systems

import com.leaf.osumania.engine.GameConstants
import com.leaf.osumania.engine.GameEngine
import kotlin.math.sqrt
import kotlin.math.roundToInt
import kotlin.math.abs

class ScoreSystem(private val engine: GameEngine) {
    var bonus: Float = 100f; private set
    var score: Long = 0L; private set
    var combo: Int = 0; private set
    var maxCombo: Int = 0; private set
    var accuracy: Float = 1f; private set
    var multiplier: Float = 1f; private set

    private val judgementCounts = mutableMapOf(320 to 0, 300 to 0, 200 to 0, 100 to 0, 50 to 0, 0 to 0)
    val hitErrors = mutableListOf<Float>()

    var totalHitObjects: Int = 0
    var latestJudgement: Int = -1
    var latestEarlyOrLate: String? = null

    fun reset() {
        bonus = 100f; score = 0L; combo = 0; maxCombo = 0; accuracy = 1f
        judgementCounts.clear(); judgementCounts.putAll(mapOf(320 to 0, 300 to 0, 200 to 0, 100 to 0, 50 to 0, 0 to 0))
        hitErrors.clear()
    }

    fun hit(judgement: Int, earlyOrLate: String?, isForHold: Boolean = false) {
        if (engine.health <= GameConstants.MIN_HEALTH && !engine.mods.noFail) return

        val scoreToAdd = getScoreToAdd(judgement)
        score = (score + scoreToAdd).coerceIn(0, GameConstants.MAX_SCORE.toLong())

        engine.healthSystem.hit(judgement, isForHold)

        judgementCounts[judgement] = (judgementCounts[judgement] ?: 0) + 1

        if (judgement == 0) {
            combo = 0
        } else {
            combo++
            if (combo > maxCombo) maxCombo = combo
        }

        latestJudgement = judgement
        latestEarlyOrLate = earlyOrLate
        accuracy = calculateAccuracy()
    }

    private fun getScoreToAdd(judgement: Int): Float {
        val baseScore = (GameConstants.MAX_SCORE.toFloat() / 2f / totalHitObjects) * judgement.toFloat() / 320f
        bonus = (bonus + GameConstants.getHitBonusChange(judgement).toFloat()).coerceIn(0f, 100f)
        val bonusScore = (GameConstants.MAX_SCORE.toFloat() / 2f / totalHitObjects) * GameConstants.getHitBonusValue(judgement).toFloat() * sqrt(bonus) / 320f
        return (baseScore + bonusScore) * multiplier
    }

    private fun calculateAccuracy(): Float {
        val weight = 305f * (judgementCounts[320] ?: 0) + 300f * (judgementCounts[300] ?: 0) +
            200f * (judgementCounts[200] ?: 0) + 100f * (judgementCounts[100] ?: 0) +
            50f * (judgementCounts[50] ?: 0)
        val total = judgementCounts.values.sum()
        return if (total > 0) weight / (305f * total) else 1f
    }

    fun getJudgementCount(judgement: Int): Int = judgementCounts[judgement] ?: 0

    fun recordHitError(error: Float, judgement: Int) {
        hitErrors.add(error)
    }

    fun calculatePp(starRating: Float): Float {
        val acc320 = 320f * (judgementCounts[320] ?: 0)
        val acc300 = 300f * (judgementCounts[300] ?: 0)
        val acc200 = 200f * (judgementCounts[200] ?: 0)
        val acc100 = 100f * (judgementCounts[100] ?: 0)
        val acc50 = 50f * (judgementCounts[50] ?: 0)
        val total = judgementCounts.values.sum()
        if (total == 0) return 0f
        val accValue = (acc320 + acc300 + acc200 + acc100 + acc50) / (320f * total)
        val totalHits = judgementCounts.values.sum()
        var value = 8f * Math.pow(Math.max(0.0, (starRating - 0.15).toDouble()), 2.2).toFloat() *
            Math.max(0f, 5f * accValue - 4f) * (1f + 0.1f * Math.min(1f, totalHits / 1500f))
        if (engine.mods.noFail) value *= 0.75f
        if (engine.mods.easy) value *= 0.5f
        return Math.round(value).toFloat()
    }

    fun getLetterGrade(): String {
        val nonPerfect = (judgementCounts[200] ?: 0) + (judgementCounts[100] ?: 0) + (judgementCounts[50] ?: 0) + (judgementCounts[0] ?: 0)
        if (nonPerfect == 0) return "SS"
        return when {
            accuracy > 0.95f -> "S"
            accuracy > 0.90f -> "A"
            accuracy > 0.80f -> "B"
            accuracy > 0.70f -> "C"
            else -> "D"
        }
    }

    fun getHitErrorsArray(): FloatArray = hitErrors.toFloatArray()
}
