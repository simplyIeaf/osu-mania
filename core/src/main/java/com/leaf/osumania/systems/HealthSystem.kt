package com.leaf.osumania.systems

import com.leaf.osumania.beatmap.BeatmapData
import com.leaf.osumania.beatmap.TimingPoint
import com.leaf.osumania.beatmap.TapData
import com.leaf.osumania.beatmap.HoldData
import com.leaf.osumania.mods.Mods
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class HealthSystem {
    var health: Float = 1f; private set
    var hpMultiplierNormal: Float = 1f; private set

    private var hpDrainRate: Float = 0f

    fun reset(mods: Mods? = null) {
        health = 1f
        hpDrainRate = 0f
    }

    fun computeDrainRate(beatmap: BeatmapData, mods: Mods) {
        val hpRaw = beatmap.hp
        val hp = getHpAfterMods(hpRaw, mods)
        val odRaw = beatmap.od
        val od = getOdAfterMods(odRaw, mods)

        val lowestHpEver = 0.975f - hp * 0.0725f
        val lowestHpEnd = 0.99f - hp * 0.059f
        val hpRecoveryAvailable = 0.04f * max(0f, 1f - hp / 10f)

        val hitWindows = computeHitWindows(od)

        val allObjects = mutableListOf<Pair<Float, Float>>()
        for (tap in beatmap.taps) {
            allObjects.add(Pair(tap.time, tap.time))
        }
        for (hold in beatmap.holds) {
            allObjects.add(Pair(hold.time, hold.endTime))
        }
        allObjects.sortBy { it.first }

        val hpMultiplierDrain = hpMultiplierFromHp(hp)
        val hpMultiplierBonus = 0.5f

        var testDrop = 0.00025f
        var found = false

        for (attempt in 0..100) {
            var currentHealth = 1f
            var hpMultiplier = hpMultiplierNormal
            var recoveredTotal = 0f
            var lowestSeen = 1f

            var lastTime = -1f
            var lastEndTime = -1f

            for ((index, obj) in allObjects.withIndex()) {
                val startTime = obj.first
                val endTime = obj.second

                if (lastEndTime > 0f && lastTime > 0f) {
                    val gap = startTime - lastEndTime
                    if (gap > 0f) {
                        currentHealth -= hpDrainRate * gap / 1000f
                        currentHealth = currentHealth.coerceIn(0f, 1f)
                    }
                } else if (index > 0 && lastTime > 0f) {
                    val gap = startTime - lastTime
                    if (gap > 0f) {
                        currentHealth -= hpDrainRate * gap / 1000f
                        currentHealth = currentHealth.coerceIn(0f, 1f)
                    }
                }

                val isHold = endTime > startTime + 1f

                if (!isHold) {
                    val increase = hpMultPerHit(300, currentHealth, hp) * hpMultiplier
                    val decrease = hpMultPerMiss(currentHealth, hp) * hpMultiplierDrain
                    val mid = increase * 0.5f
                    currentHealth = (currentHealth + mid).coerceIn(0f, 1f)
                    recoveredTotal += max(0f, mid)
                } else {
                    val increase = hpMultPerHit(300, currentHealth, hp) * hpMultiplier * 0.5f
                    currentHealth = (currentHealth + increase).coerceIn(0f, 1f)
                    recoveredTotal += max(0f, increase)
                }

                if (currentHealth < lowestSeen) lowestSeen = currentHealth

                lastTime = startTime
                lastEndTime = endTime
            }

            val dropAmount = testDrop * allObjects.size.toFloat()

            if (lowestSeen < lowestHpEver || currentHealth < lowestHpEnd) {
                hpMultiplierNormal += 0.1f
                testDrop *= 0.9f
            } else {
                hpDrainRate = hpMultiplierNormal * testDrop * 1000f
                found = true
                break
            }
        }

        if (!found) {
            hpDrainRate = hpMultiplierNormal * testDrop * 1000f
        }
    }

    fun hit(judgement: Int, isForHold: Boolean = false) {
        val increase = getHealthIncrease(judgement, isForHold)
        health = (health + increase).coerceIn(0f, 1f)
    }

    fun getHealthIncrease(judgement: Int, isForHold: Boolean = false): Float {
        val hpMult = hpMultiplierNormal
        return when (judgement) {
            0 -> -(health + 1) * if (isHold) 0.00375f else 0.0075f
            50 -> -(health + 1) * 0.0016f
            100 -> 0f
            200 -> hpMult * (0.004f - health * 0.0004f)
            300 -> hpMult * (0.005f - health * 0.0005f)
            320 -> hpMult * (0.0055f - health * 0.0005f)
            else -> 0f
        }
    }

    fun drain(deltaTime: Float) {
        health = (health - hpMultiplierNormal * deltaTime).coerceIn(0f, 1f)
    }

    private fun computeHitWindows(od: Float): FloatArray {
        val odMs = 300f - 3f * od
        val goodMs = odMs + 20f
        val mehMs = odMs + 50f
        val missMs = odMs + 100f
        return floatArrayOf(odMs, goodMs, mehMs, missMs)
    }

    private fun getHpAfterMods(hp: Float, mods: Mods): Float = when {
        mods.easy -> hp / 2f
        mods.hardRock -> (hp * 1.4f).coerceAtMost(10f)
        mods.hpOverride != null -> mods.hpOverride!!
        else -> hp
    }

    private fun getOdAfterMods(od: Float, mods: Mods): Float = when {
        mods.easy -> od / 2f
        mods.hardRock -> (od * 1.4f).coerceAtMost(10f)
        mods.odOverride != null -> mods.odOverride!!
        else -> od
    }

    private fun hpMultiplierFromHp(hp: Float): Float {
        return 0.06f * max(1f, hp)
    }

    private fun hpMultPerHit(judgement: Int, currentHealth: Float, hp: Float): Float {
        return when (judgement) {
            320 -> 0.0055f - currentHealth * 0.0005f
            300 -> 0.005f - currentHealth * 0.0005f
            200 -> 0.004f - currentHealth * 0.0004f
            100 -> 0f
            50 -> -(currentHealth + 1) * 0.0016f
            else -> -(currentHealth + 1) * 0.0075f
        }
    }

    private fun hpMultPerMiss(currentHealth: Float, hp: Float): Float {
        return -(currentHealth + 1) * 0.0075f
    }
}
