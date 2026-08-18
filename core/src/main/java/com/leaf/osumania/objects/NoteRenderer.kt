package com.leaf.osumania.objects

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.leaf.osumania.beatmap.TapData
import com.leaf.osumania.beatmap.HoldData
import com.leaf.osumania.engine.GameConstants
import com.leaf.osumania.engine.GameEngine
import com.leaf.osumania.engine.Playfield

class NoteRenderer(private val engine: GameEngine, private val playfield: Playfield) {
    private val tapRectangles = mutableListOf<Rectangle>()
    private val holdBodies = mutableListOf<Triple<Float, Float, Int>>()
    private val holdHeads = mutableListOf<Triple<Float, Float, Int>>()
    private val holdTails = mutableListOf<Triple<Float, Float, Int>>()
    private val receptorRects = mutableListOf<Rectangle>()
    private val pressedColumns = BooleanArray(18)

    fun setColumnPressed(column: Int, pressed: Boolean) {
        if (column in pressedColumns.indices) pressedColumns[column] = pressed
    }

    fun render(shapeRenderer: ShapeRenderer, batch: SpriteBatch, font: BitmapFont, layout: GlyphLayout) {
        renderStage(shapeRenderer)
        renderHoldBodies(shapeRenderer)
        renderHoldHeads(shapeRenderer)
        renderHoldTails(shapeRenderer)
        renderTapNotes(shapeRenderer)
        renderReceptors(shapeRenderer, batch, font, layout)
    }

    private fun renderStage(shapeRenderer: ShapeRenderer) {
        shapeRenderer.rect(
            playfield.stageX, 0f, playfield.stageWidth, playfield.screenHeight,
            Color(0.07f, 0.07f, 0.07f, engine.settings.stageOpacity)
        )
        val sideWidth = 2f
        shapeRenderer.rect(playfield.stageX - sideWidth, 0f, sideWidth, playfield.screenHeight, Color(0.3f, 0.3f, 0.3f, 0.5f))
        shapeRenderer.rect(playfield.stageX + playfield.stageWidth, 0f, sideWidth, playfield.screenHeight, Color(0.3f, 0.3f, 0.3f, 0.5f))
        shapeRenderer.rect(playfield.stageX, playfield.hitPosition - 5f, playfield.stageWidth, 10f, Color(1f, 1f, 1f, 0.2f))
    }

    private fun renderTapNotes(shapeRenderer: ShapeRenderer) {
        val timeElapsed = engine.timeElapsed
        for (col in 0 until engine.keyCount) {
            val columnObjects = engine.columns.getOrNull(col) ?: continue
            for (obj in columnObjects) {
                if (obj !is TapData) continue
                val idx = engine.columnIndices[col]
                val objIdx = columnObjects.indexOf(obj)
                if (objIdx < idx) continue
                val offset = engine.getObjectOffset(timeElapsed, obj.time)
                val y = playfield.hitPosition - offset
                if (y < -100f || y > playfield.screenHeight + 100f) continue
                val x = playfield.getColumnX(col)
                val w = playfield.columnWidth
                val colors = engine.laneColors[col]
                val noteColor = if (obj.isHoldHead) colors.holdHeadColor else colors.tapColor
                renderNoteShape(shapeRenderer, x, y, w, noteColor, engine.settings.noteScale)
            }
        }
    }

    private fun renderHoldBodies(shapeRenderer: ShapeRenderer) {
        val timeElapsed = engine.timeElapsed
        for (col in 0 until engine.keyCount) {
            for (obj in engine.holdColumns.getOrNull(col) ?: emptyList()) {
                val headIdx = engine.holdColumnIndices[col]
                val objIdx = (engine.holdColumns.getOrNull(col) ?: emptyList()).indexOf(obj)
                if (objIdx < headIdx) continue
                val headOffset = engine.getObjectOffset(timeElapsed, obj.time)
                val tailOffset = engine.getObjectOffset(timeElapsed, obj.endTime)
                val headY = playfield.hitPosition - headOffset
                val tailY = playfield.hitPosition - tailOffset
                val topY = maxOf(headY, tailY)
                val bottomY = minOf(headY, playfield.hitPosition)
                if (bottomY < 0f || topY > playfield.screenHeight) continue
                val bodyHeight = bottomY - topY
                if (bodyHeight <= 0f) continue
                val x = playfield.getColumnX(col)
                val w = playfield.columnWidth * engine.settings.noteScale * 0.6f
                val offsetX = (playfield.columnWidth - w) / 2f
                val colors = engine.laneColors[col]
                shapeRenderer.rect(x + offsetX, topY, w, bodyHeight, colors.holdColor)
            }
        }
    }

    private fun renderHoldHeads(shapeRenderer: ShapeRenderer) {
        val timeElapsed = engine.timeElapsed
        for (col in 0 until engine.keyCount) {
            for (obj in engine.holdColumns.getOrNull(col) ?: emptyList()) {
                val headIdx = engine.holdColumnIndices[col]
                val objIdx = (engine.holdColumns.getOrNull(col) ?: emptyList()).indexOf(obj)
                if (objIdx < headIdx) continue
                val offset = engine.getObjectOffset(timeElapsed, obj.time)
                val y = playfield.hitPosition - offset
                if (y < -100f || y > playfield.screenHeight + 100f) continue
                val x = playfield.getColumnX(col)
                val w = playfield.columnWidth
                renderNoteShape(shapeRenderer, x, y, w, engine.laneColors[col].holdHeadColor, engine.settings.noteScale)
            }
        }
    }

    private fun renderHoldTails(shapeRenderer: ShapeRenderer) {
        val timeElapsed = engine.timeElapsed
        for (col in 0 until engine.keyCount) {
            for (obj in engine.holdColumns.getOrNull(col) ?: emptyList()) {
                val headIdx = engine.holdColumnIndices[col]
                val objIdx = (engine.holdColumns.getOrNull(col) ?: emptyList()).indexOf(obj)
                if (objIdx < headIdx) continue
                val offset = engine.getObjectOffset(timeElapsed, obj.endTime)
                val y = playfield.hitPosition - offset
                if (y < -100f || y > playfield.screenHeight + 100f) continue
                val x = playfield.getColumnX(col)
                val w = playfield.columnWidth * engine.settings.noteScale * 0.6f
                val offsetX = (playfield.columnWidth - w) / 2f
                renderNoteShape(shapeRenderer, x + offsetX, y, w, engine.laneColors[col].holdColor, engine.settings.noteScale)
            }
        }
    }

    private fun renderReceptors(shapeRenderer: ShapeRenderer, batch: SpriteBatch, font: BitmapFont, layout: GlyphLayout) {
        for (col in 0 until engine.keyCount) {
            val x = playfield.getColumnX(col)
            val y = playfield.hitPosition
            val w = playfield.columnWidth
            val pressed = pressedColumns[col]
            val colors = engine.laneColors[col]

            shapeRenderer.rect(x, y - 5f, w, 10f, Color(colors.tapColor.r, colors.tapColor.g, colors.tapColor.b, if (pressed) 0.8f else 0.3f))

            val receptorAlpha = engine.settings.receptorOpacity
            val borderColor = Color(0.5f, 0.5f, 0.5f, receptorAlpha)
            shapeRenderer.rect(x + 2f, y - w / 2f + 2f, w - 4f, w - 4f, borderColor)

            if (pressed && engine.settings.receptorLighting) {
                val lightColor = Color(colors.tapColor.r, colors.tapColor.g, colors.tapColor.b, 0.4f)
                val lightHeight = playfield.screenHeight * 0.3f
                shapeRenderer.rect(x, y - lightHeight, w, lightHeight, lightColor)
            }
        }
    }

    private fun renderNoteShape(shapeRenderer: ShapeRenderer, x: Float, y: Float, width: Float, color: Color, scale: Float) {
        val w = width * scale
        val h = w * 0.4f
        val offsetX = (width - w) / 2f
        when (engine.settings.skinStyle) {
            GameConstants.SkinStyle.BAR -> {
                shapeRenderer.rect(x + offsetX, y - h / 2f, w, h, color)
            }
            GameConstants.SkinStyle.CIRCLE -> {
                shapeRenderer.rect(x + offsetX, y - h / 2f, w, h, color)
            }
            GameConstants.SkinStyle.ARROW -> {
                val cx = x + width / 2f
                val halfW = w / 2f
                shapeRenderer.triangle(cx, y + halfW, cx - halfW, y - halfW, cx + halfW, y - halfW, color)
            }
            GameConstants.SkinStyle.DIAMOND -> {
                val cx = x + width / 2f
                val halfW = w / 2f
                shapeRenderer.triangle(cx, y + halfW, cx - halfW, y, cx + halfW, y, color)
                shapeRenderer.triangle(cx, y - halfW, cx - halfW, y, cx + halfW, y, color)
            }
        }
    }

    fun dispose() {}
}
