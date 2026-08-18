package com.leaf.osumania.systems

import com.leaf.osumania.engine.GameConstants
import kotlin.math.abs

class ScoreSystem {
    var bonus: Float = 100f; private set
    var score: Long = 0L; private set
    var maxCombo: Int = 0; private set
    var accuracy: Float = 1f; private set
    var multiplier: Float = 1f; private set
    var currentCombo: Int = 0; private set

    private val judgementCounts = mutableMapOf(
        320 to 0, 300 to 0, 200 to 0, 100 to 0, 50 to 0, 0 to 0
    )
    val hitErrors = mutableListOf<Float>()

    var totalHitObjects: Int = 0
    var latestJudgement: Int = -1
        private set
    var latestEarlyOrLate: String? = null
        private set

    fun reset() {
        bonus = 100f
        score = 0L
        currentCombo = 0
        maxCombo = 0
        accuracy = 1f
        multiplier = 1f
        judgementCounts.clear()
        judgementCounts.putAll(mapOf(320 to 0, 300 to 0, 200 to 0, 100 to 0, 50 to 0, 0 to 0))
        hitErrors.clear()
        totalHitObjects = 0
        latestJudgement = -1
        latestEarlyOrLate = null
    }

    fun hit(judgement: Int, combo: Int) {
        totalHitObjects++
        latestJudgement = judgement
        judgementCounts[judgement] = (judgementCounts[judgement] ?: 0) + 1

        currentCombo = combo
        if (combo > maxCombo) maxCombo = combo

        val bonusValue = GameConstants.getHitBonusValue(judgement)
        val bonusChange = GameConstants.getHitBonusChange(judgement)

        bonus = (bonus + bonusChange).coerceIn(0f, 100f)

        val comboScore = combo * (GameConstants.MAX_COMBO_BONUS / 1000f)
        val bonusScore = (bonus * multiplier).toInt().toLong()

        val scoreToAdd = when (judgement) {
            320 -> (300 * multiplier * (1 + comboScore / 25f)).toLong() + bonusScore
            300 -> (300 * multiplier * (1 + comboScore / 25f)).toLong() + bonusScore
            200 -> (200 * multiplier * (1 + comboScore / 25f)).toLong() + bonusScore
            100 -> (100 * multiplier * (1 + comboScore / 25f)).toLong() + bonusScore
            50 -> (50 * multiplier * (1 + comboScore / 25f)).toLong() + bonusScore
            else -> 0L
        }

        score = (score + scoreToAdd).coerceIn(0, GameConstants.MAX_SCORE.toLong())
        updateAccuracy()
    }

    fun miss() {
        totalHitObjects++
        latestJudgement = 0
        judgementCounts[0] = (judgementCounts[0] ?: 0) + 1
        bonus = (bonus - 100).coerceIn(0f, 100f)
        currentCombo = 0
        updateAccuracy()
    }

    fun holdComplete() {
        bonus = (bonus + 2).coerceIn(0f, 100f)
    }

    fun getJudgementCount(judgement: Int): Int {
        return judgementCounts[judgement] ?: 0
    }

    fun getLetterGrade(): String {
        val pct = accuracy * 100f
        return when {
            judgementCounts[320] == totalHitObjects && totalHitObjects > 0 -> "X"
            pct >= 95f -> "S"
            pct >= 90f -> "A"
            pct >= 80f -> "B"
            pct >= 70f -> "C"
            pct >= 60f -> "D"
            else -> "F"
        }
    }

    fun calculatePp(keyCount: Float): Float {
        if (totalHitObjects == 0) return 0f
        val basePp = totalHitObjects * 0.15f * (keyCount / 4f)
        return basePp * accuracy * multiplier
    }

    private fun updateAccuracy() {
        if (totalHitObjects == 0) {
            accuracy = 1f
            return
        }
        val weighted = (judgementCounts[320] ?: 0) * 320 +
            (judgementCounts[300] ?: 0) * 300 +
            (judgementCounts[200] ?: 0) * 200 +
            (judgementCounts[100] ?: 0) * 100 +
            (judgementCounts[50] ?: 0) * 50
        val maxPossible = totalHitObjects * 320
        accuracy = if (maxPossible > 0) weighted.toFloat() / maxPossible.toFloat() else 1f
    }
}
