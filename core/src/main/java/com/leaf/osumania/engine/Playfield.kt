package com.leaf.osumania.engine

class Playfield(
    var screenWidth: Float = GameConstants.OSU_WIDTH,
    var screenHeight: Float = GameConstants.OSU_HEIGHT,
    var keyCount: Int = 4,
    var hitPositionOffset: Float = 50f,
    var stagePosition: Float = 0.5f,
    var noteOffset: Float = 0f,
    var noteScale: Float = 1f,
    var laneWidthAdjustment: Float = 1f,
    var laneSpacing: Float = 0f,
    var upscroll: Boolean = false,
    var skinStyle: GameConstants.SkinStyle = GameConstants.SkinStyle.BAR
) {
    var columnWidth: Float = 0f
        private set
    var stageWidth: Float = 0f
        private set
    var hitPosition: Float = 0f
        private set
    var notesContainerWidth: Float = 0f
        private set
    var stageX: Float = 0f
        private set

    fun recalculate() {
        val baseLaneWidth = GameConstants.LANE_WIDTHS[GameConstants.LANE_WIDTHS.size.coerceAtMost(keyCount) - 1]
        val scaleFactor = screenWidth / GameConstants.OSU_WIDTH
        columnWidth = (baseLaneWidth * scaleFactor * laneWidthAdjustment).coerceAtMost(screenWidth / keyCount * 0.95f)

        val columnRatio = when (skinStyle) {
            GameConstants.SkinStyle.CIRCLE -> GameConstants.CIRCLE_COLUMN_RATIO
            GameConstants.SkinStyle.ARROW -> GameConstants.ARROW_COLUMN_RATIO
            GameConstants.SkinStyle.DIAMOND -> GameConstants.DIAMOND_COLUMN_RATIO
            GameConstants.SkinStyle.BAR -> 1f
        }
        columnWidth *= columnRatio

        val totalSpacing = laneSpacing * (keyCount - 1) * scaleFactor
        stageWidth = columnWidth * keyCount + totalSpacing
        notesContainerWidth = stageWidth

        hitPosition = screenHeight - hitPositionOffset

        val maxX = screenWidth - stageWidth
        stageX = (maxX * stagePosition).coerceIn(0f, maxX)
    }

    fun getColumnX(column: Int): Float {
        val scaleFactor = screenWidth / GameConstants.OSU_WIDTH
        val spacing = laneSpacing * scaleFactor
        return stageX + column * (columnWidth + spacing)
    }
}
