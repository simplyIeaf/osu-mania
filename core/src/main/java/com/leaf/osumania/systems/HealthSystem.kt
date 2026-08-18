package com.leaf.osumania.systems

import com.leaf.osumania.beatmap.BeatmapData
import com.leaf.osumania.beatmap.TimingPoint
import com.leaf.osumania.mods.Mods

class HealthSystem {
    var health: Float = 1f; private set
    var hpMultiplierNormal: Float = 1f; private set

    private var hpDrainRate: Float = 0f

    fun reset() {
        health = 1f
        hpDrainRate = 0f
    }

    fun hit(judgement: Int) {
        val increase = getHealthIncrease(judgement)
        health = (health + increase).coerceIn(0f, 1f)
    }

    fun miss() {
        health = (health - 0.0075f * (health + 1)).coerceIn(0f, 1f)
    }

    fun passiveDrain(deltaTime: Float, timingPoint: TimingPoint) {
        val drainAmount = hpDrainRate * deltaTime
        health = (health - drainAmount).coerceIn(0f, 1f)
    }

    fun drain(deltaTime: Float) {
        health = (health - hpMultiplierNormal * deltaTime * 0.001f).coerceIn(0f, 1f)
    }

    private fun getHealthIncrease(judgement: Int): Float {
        return when (judgement) {
            320 -> hpMultiplierNormal * (0.0055f - health * 0.0005f)
            300 -> hpMultiplierNormal * (0.005f - health * 0.0005f)
            200 -> hpMultiplierNormal * (0.004f - health * 0.0004f)
            100 -> 0f
            50 -> -(health + 1) * 0.0016f
            else -> -(health + 1) * 0.0075f
        }
    }
}
