package com.leaf.osumania.mods

data class Mods(
    var autoplay: Boolean = false,
    var easy: Boolean = false,
    var hardRock: Boolean = false,
    var noFail: Boolean = false,
    var suddenDeath: Boolean = false,
    var perfect: Boolean = false,
    var perfectSs: Boolean = false,
    var playbackRate: Float = 1f,
    var mirror: Boolean = false,
    var random: Boolean = false,
    var constantSpeed: Boolean = false,
    var holdOff: Boolean = false,
    var accuracyChallengeThreshold: Float? = null,
    var accuracyChallengeMode: String = "standard",
    var hpOverride: Float? = null,
    var odOverride: Float? = null,
    var coverType: String? = null,
    var coverFadeTime: Float = 0f,
    var percyCutoffDuration: Float? = null
) {
    val effectivePlaybackRate: Float get() = playbackRate
    val isSpeedMod: Boolean get() = playbackRate != 1f
}

class ModManager(initialMods: Mods = Mods()) {
    var mods = Mods()
        private set

    init {
        mods = initialMods.copy()
    }

    fun reset() {
        mods = Mods()
    }

    val isNoFail: Boolean get() = mods.noFail
    val isAutoplay: Boolean get() = mods.autoplay
    val isMirror: Boolean get() = mods.mirror
    val isRandom: Boolean get() = mods.random

    fun getScoreMultiplier(): Float {
        var m = 1f
        if (mods.easy) m *= 0.5f
        if (mods.noFail) m *= 0.5f
        if (mods.playbackRate < 1f) m *= 0.3f
        if (mods.constantSpeed) m *= 0.9f
        if (mods.holdOff) m *= 0.9f
        if (mods.hpOverride != null) m *= 0.5f
        if (mods.odOverride != null) m *= 0.5f
        return m
    }

    fun getHpAfterMods(hp: Float): Float = when {
        mods.easy -> hp / 2f
        mods.hardRock -> (hp * 1.4f).coerceAtMost(10f)
        mods.hpOverride != null -> mods.hpOverride!!
        else -> hp
    }

    fun getOdAfterMods(od: Float): Float = when {
        mods.easy -> od / 2f
        mods.hardRock -> (od * 1.4f).coerceAtMost(10f)
        mods.odOverride != null -> mods.odOverride!!
        else -> od
    }

    fun getModStrings(): List<String> {
        val list = mutableListOf<String>()
        if (mods.easy) list.add("EZ")
        if (mods.noFail) list.add("NF")
        if (mods.hardRock) list.add("HR")
        if (mods.suddenDeath) list.add("SD")
        if (mods.perfect) list.add("PF")
        if (mods.autoplay) list.add("AP")
        if (mods.playbackRate == 1.5f) list.add("DT")
        if (mods.playbackRate == 0.75f) list.add("HT")
        if (mods.playbackRate != 1f && mods.playbackRate != 1.5f && mods.playbackRate != 0.75f) {
            list.add("%.2fx".format(mods.playbackRate))
        }
        if (mods.mirror) list.add("MR")
        if (mods.random) list.add("RD")
        if (mods.constantSpeed) list.add("CS")
        if (mods.holdOff) list.add("HO")
        return list
    }

    fun encode(): Int {
        var bits = 0
        if (mods.autoplay) bits = bits or (1 shl 0)
        if (mods.easy) bits = bits or (1 shl 1)
        if (mods.hardRock) bits = bits or (1 shl 2)
        if (mods.mirror) bits = bits or (1 shl 3)
        if (mods.random) bits = bits or (1 shl 4)
        if (mods.constantSpeed) bits = bits or (1 shl 5)
        if (mods.holdOff) bits = bits or (1 shl 6)
        if (mods.noFail) bits = bits or (1 shl 7)
        if (mods.suddenDeath) bits = bits or (1 shl 8)
        if (mods.perfect) bits = bits or (1 shl 9)
        if (mods.perfectSs) bits = bits or (1 shl 10)
        return bits
    }
}
