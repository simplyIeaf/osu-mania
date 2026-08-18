package com.leaf.osumania.engine

import com.leaf.osumania.beatmap.TimingPoint

object NotePositioning {
    fun getHitObjectOffset(timeElapsed: Float, noteTime: Float, timingPoints: List<TimingPoint>,
        startTime: Float, endTime: Float, hitPosition: Float, scrollSpeed: Float,
        constantSpeed: Boolean, playbackRate: Float): Float {
        if (constantSpeed) {
            val speed = hitPosition / (GameConstants.MAX_TIME_RANGE / scrollSpeed)
            return ((endTime - startTime) * speed) / playbackRate
        }
        var totalOffset = 0f
        var currentTimingPoint = timingPoints.firstOrNull() ?: return 0f
        var time = startTime
        for (tp in timingPoints) {
            if (tp.time > endTime) break
            if (tp.time > time) {
                val intervalStart = time
                val intervalEnd = minOf(tp.time, endTime)
                val duration = intervalEnd - intervalStart
                val speed = hitPosition / (GameConstants.MAX_TIME_RANGE / currentTimingPoint.scrollSpeed)
                totalOffset += (duration * speed) / playbackRate
            }
            currentTimingPoint = tp
            time = maxOf(time, tp.time)
        }
        if (time < endTime) {
            val duration = endTime - time
            val speed = hitPosition / (GameConstants.MAX_TIME_RANGE / currentTimingPoint.scrollSpeed)
            totalOffset += (duration * speed) / playbackRate
        }
        val noteOffset = hitPosition / (GameConstants.MAX_TIME_RANGE / scrollSpeed) / playbackRate
        return totalOffset - noteOffset
    }
}
