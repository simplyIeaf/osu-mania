package com.leaf.osumania.systems

import com.leaf.osumania.engine.GameEngine

class InputSystem(private val engine: GameEngine) {
    lateinit var tappedColumns: BooleanArray
        private set
    lateinit var pressedColumns: BooleanArray
        private set
    lateinit var releasedColumns: BooleanArray
        private set
    lateinit var columnPressedKeybinds: Array<MutableSet<Int>>
        private set
    var pauseTapped: Boolean = false
        private set

    fun reset(keyCount: Int) {
        tappedColumns = BooleanArray(keyCount)
        pressedColumns = BooleanArray(keyCount)
        releasedColumns = BooleanArray(keyCount)
        columnPressedKeybinds = Array(keyCount) { mutableSetOf() }
        pauseTapped = false
    }

    fun hit(column: Int, timeElapsed: Float) {
        if (column < 0 || column >= pressedColumns.size) return
        tappedColumns[column] = true
        pressedColumns[column] = true
        releasedColumns[column] = false
        engine.audioSystem.playHitsound("normal", "hitnormal", 0.8f)
        engine.noteHit(column, timeElapsed)
    }

    fun release(column: Int, timeElapsed: Float) {
        if (column < 0 || column >= pressedColumns.size) return
        pressedColumns[column] = false
        releasedColumns[column] = true
        engine.noteRelease(column, timeElapsed)
    }

    fun isTapped(column: Int): Boolean {
        if (column < 0 || column >= tappedColumns.size) return false
        return tappedColumns[column]
    }

    fun isPressed(column: Int): Boolean {
        if (column < 0 || column >= pressedColumns.size) return false
        return pressedColumns[column]
    }

    fun isReleased(column: Int): Boolean {
        if (column < 0 || column >= releasedColumns.size) return false
        return releasedColumns[column]
    }

    fun clearInputs() {
        for (i in tappedColumns.indices) {
            tappedColumns[i] = false
        }
        for (i in releasedColumns.indices) {
            releasedColumns[i] = false
        }
    }

    fun checkLateMisses(timeElapsed: Float) {
        for (column in pressedColumns.indices) {
            if (pressedColumns[column] || releasedColumns[column]) continue
            for (note in engine.currentNotes) {
                if (note.column != column) continue
                if (note.hit || note.missed) continue
                if (note is com.leaf.osumania.beatmap.TapData) {
                    val missWindow = engine.getMissWindow()
                    if (timeElapsed - note.time > missWindow) {
                        note.missed = true
                        engine.scoreSystem.hit(0, null)
                    }
                }
            }
        }
    }
}
