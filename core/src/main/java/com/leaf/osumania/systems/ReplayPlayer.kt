package com.leaf.osumania.systems

import com.leaf.osumania.engine.GameEngine

class ReplayPlayer(private val engine: GameEngine) {
    private var events = listOf<ReplayEvent>()
    private var currentIndex = 0
    var isPlaying: Boolean = false; private set

    fun load(replayData: ReplayRecorder.ReplayData) {
        events = replayData.inputs
            .map { ReplayEvent(it.first, it.second, it.third) }
            .sortedBy { it.time }
        currentIndex = 0
        isPlaying = true
    }

    fun update(timeElapsed: Float) {
        if (!isPlaying) return

        while (currentIndex < events.size) {
            val event = events[currentIndex]
            if (event.time > timeElapsed) break

            if (event.isDown) {
                engine.inputSystem.hit(event.column, event.time)
            } else {
                engine.inputSystem.release(event.column, event.time)
            }
            currentIndex++
        }

        if (currentIndex >= events.size) {
            isPlaying = false
        }
    }

    fun stop() {
        isPlaying = false
        currentIndex = 0
    }

    fun reset() {
        events = emptyList()
        currentIndex = 0
        isPlaying = false
    }

    data class ReplayEvent(val column: Int, val time: Float, val isDown: Boolean)
}
