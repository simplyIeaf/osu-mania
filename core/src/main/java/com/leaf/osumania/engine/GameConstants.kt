package com.leaf.osumania.engine

import com.badlogic.gdx.graphics.Color

object GameConstants {
    const val OSU_WIDTH = 854f
    const val OSU_HEIGHT = 480f
    const val MAX_TIME_RANGE = 11485f
    const val MAX_SCORE = 1_000_000
    const val MAX_COMBO_BONUS = 10000f
    val JUDGEMENTS = intArrayOf(320, 300, 200, 100, 50, 0)

    val JUDGEMENT_COLORS = mapOf(
        320 to Color(0.74f, 1f, 1f, 1f),
        300 to Color(0.4f, 0.8f, 1f, 1f),
        200 to Color(0.75f, 0.85f, 0.35f, 1f),
        100 to Color(0.85f, 0.75f, 0.15f, 1f),
        50 to Color(0.9f, 0.65f, 0.1f, 1f),
        0 to Color(0.85f, 0.2f, 0.2f, 1f)
    )

    const val CIRCLE_COLUMN_RATIO = 0.8f
    const val ARROW_COLUMN_RATIO = 0.8f
    const val DIAMOND_COLUMN_RATIO = 0.85f

    val LANE_WIDTHS = intArrayOf(56, 56, 56, 56, 50, 47, 42, 40, 38, 35, 31, 30, 29, 27, 24, 22, 21, 20)

    val LANE_ARROW_DIRECTIONS = arrayOf(
        floatArrayOf(0f),
        floatArrayOf(270f, 90f),
        floatArrayOf(270f, 0f, 90f),
        floatArrayOf(270f, 0f, 0f, 90f),
        floatArrayOf(270f, 315f, 0f, 45f, 90f),
        floatArrayOf(270f, 315f, 0f, 0f, 45f, 90f),
        floatArrayOf(270f, 315f, 0f, 0f, 0f, 45f, 90f),
        floatArrayOf(270f, 315f, 0f, 0f, 0f, 0f, 45f, 90f),
        floatArrayOf(270f, 315f, 0f, 0f, 0f, 0f, 0f, 45f, 90f),
        floatArrayOf(270f, 315f, 350f, 0f, 0f, 0f, 10f, 45f, 90f)
    )

    const val HIT_BONUS_320 = 32
    const val HIT_BONUS_300 = 32
    const val HIT_BONUS_200 = 16
    const val HIT_BONUS_100 = 8
    const val HIT_BONUS_50 = 4
    const val HIT_BONUS_MISS = 0

    const val HIT_BONUS_CHANGE_320 = 2
    const val HIT_BONUS_CHANGE_300 = 1
    const val HIT_BONUS_CHANGE_200 = -8
    const val HIT_BONUS_CHANGE_100 = -24
    const val HIT_BONUS_CHANGE_50 = -44
    const val HIT_BONUS_CHANGE_MISS = -100

    const val MIN_HEALTH = 0f
    const val MAX_HEALTH = 1f
    const val UNPAUSE_DELAY = 1500f
    const val BREAK_MIN_DURATION = 5000f

    fun getHitBonusValue(judgement: Int): Int = when(judgement) {
        320 -> HIT_BONUS_320; 300 -> HIT_BONUS_300; 200 -> HIT_BONUS_200
        100 -> HIT_BONUS_100; 50 -> HIT_BONUS_50; else -> HIT_BONUS_MISS
    }

    fun getHitBonusChange(judgement: Int): Int = when(judgement) {
        320 -> HIT_BONUS_CHANGE_320; 300 -> HIT_BONUS_CHANGE_300; 200 -> HIT_BONUS_CHANGE_200
        100 -> HIT_BONUS_CHANGE_100; 50 -> HIT_BONUS_CHANGE_50; else -> HIT_BONUS_CHANGE_MISS
    }

    enum class SkinStyle { BAR, CIRCLE, ARROW, DIAMOND }
}
