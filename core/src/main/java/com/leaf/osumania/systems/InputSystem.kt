package com.leaf.osumania.systems

class InputSystem(private val keyCount: Int) {
    var tappedColumns = BooleanArray(keyCount)
        private set
    var pressedColumns = BooleanArray(keyCount)
        private set
    var releasedColumns = BooleanArray(keyCount)
        private set
    var pauseTapped: Boolean = false
        private set

    fun hit(column: Int) {
        if (column < 0 || column >= keyCount) return
        tappedColumns[column] = true
        pressedColumns[column] = true
        releasedColumns[column] = false
    }

    fun release(column: Int) {
        if (column < 0 || column >= keyCount) return
        pressedColumns[column] = false
        releasedColumns[column] = true
    }

    fun isTapped(column: Int): Boolean {
        if (column < 0 || column >= keyCount) return false
        return tappedColumns[column]
    }

    fun isPressed(column: Int): Boolean {
        if (column < 0 || column >= keyCount) return false
        return pressedColumns[column]
    }

    fun isReleased(column: Int): Boolean {
        if (column < 0 || column >= keyCount) return false
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

    fun reset() {
        tappedColumns = BooleanArray(keyCount)
        pressedColumns = BooleanArray(keyCount)
        releasedColumns = BooleanArray(keyCount)
        pauseTapped = false
    }
}
