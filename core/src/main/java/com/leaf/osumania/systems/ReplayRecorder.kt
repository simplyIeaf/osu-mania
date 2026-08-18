package com.leaf.osumania.systems

import com.leaf.osumania.mods.Mods

class ReplayRecorder {
    val inputs = mutableListOf<Triple<Int, Float, Boolean>>()
    var beatmapHash: String = ""
    var mods: Mods = Mods()
    var columnMap: IntArray? = null

    fun record(column: Int, timeElapsed: Float, isDown: Boolean) {
        inputs.add(Triple(column, timeElapsed, isDown))
    }

    fun getResult(): ReplayData {
        return ReplayData(
            version = 2,
            beatmapHash = beatmapHash,
            mods = mods,
            inputs = inputs.toList(),
            columnMap = columnMap
        )
    }

    fun reset() {
        inputs.clear()
        beatmapHash = ""
        mods = Mods()
        columnMap = null
    }

    data class ReplayData(
        val version: Int = 2,
        val beatmapHash: String,
        val mods: Mods,
        val inputs: List<Triple<Int, Float, Boolean>>,
        val columnMap: IntArray? = null
    )
}
