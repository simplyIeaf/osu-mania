package com.leaf.osumania.beatmap

import kotlin.math.abs

data class HitSound(
    val normal: Boolean = true,
    val whistle: Boolean = false,
    val finish: Boolean = false,
    val clap: Boolean = false
)

data class HitSample(
    val normalSet: Int = 0,
    val additionSet: Int = 0,
    val index: Int = 0,
    val volume: Float = 0.5f,
    val filename: String = ""
)

data class TapData(
    val column: Int,
    val time: Float,
    val endTime: Float = time,
    val hitSound: HitSound = HitSound(),
    val hitSample: HitSample = HitSample(),
    val isHoldHead: Boolean = false
)

data class HoldData(
    val column: Int,
    val time: Float,
    val endTime: Float
)

typealias HitObject = Any

data class TimingPoint(
    val time: Float,
    val beatLength: Float,
    val meter: Int = 4,
    val sampleSet: String = "normal",
    val sampleIndex: Int = 0,
    val volume: Float = 0.5f,
    val uninherited: Boolean = true,
    val effects: Int = 0,
    val scrollSpeed: Float = 1f
)

data class Break(
    val startTime: Float,
    val endTime: Float
)

data class HitWindows(
    val w320: Float,
    val w300: Float,
    val w200: Float,
    val w100: Float,
    val w50: Float,
    val w0: Float
) {
    fun getJudgement(deltaMs: Float): Int {
        val d = abs(deltaMs)
        return when {
            d <= w320 -> 320
            d <= w300 -> 300
            d <= w200 -> 200
            d <= w100 -> 100
            d <= w50 -> 50
            d <= w0 -> 0
            else -> -1
        }
    }
}

data class Metadata(
    val title: String = "",
    val titleUnicode: String = "",
    val artist: String = "",
    val artistUnicode: String = "",
    val version: String = "",
    val creator: String = ""
)

data class Difficulty(
    val keyCount: Int = 4,
    val od: Float = 5f,
    val hp: Float = 5f
)

data class BeatmapData(
    val beatmapId: Int = 0,
    val beatmapSetId: Int = 0,
    val beatmapHash: String = "",
    val version: String = "",
    val timingPoints: List<TimingPoint> = emptyList(),
    val hitObjects: List<Any> = emptyList(),
    val breaks: List<Break> = emptyList(),
    val startTime: Float = 0f,
    val endTime: Float = 0f,
    val hitWindows: HitWindows = HitWindows(19.4f, 49f, 82f, 112f, 136f, 173f),
    val delay: Float = 0f,
    val metadata: Metadata = Metadata(),
    val difficulty: Difficulty = Difficulty(),
    val audioOffset: Float = 0f,
    val backgroundUrl: String? = null,
    val videoUrl: String? = null
)
