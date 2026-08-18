package com.leaf.osumania.objects

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.leaf.osumania.beatmap.HoldData
import com.leaf.osumania.beatmap.TapData
import com.leaf.osumania.engine.GameConstants
import com.leaf.osumania.engine.GameEngine
import com.leaf.osumania.engine.Playfield
import com.leaf.osumania.storage.SettingsStore

class NoteRenderer(
    private val engine: GameEngine,
    private val playfield: Playfield,
    private val settings: SettingsStore
) {
    private val pressedColumns = BooleanArray(18)

    private val laneColors = arrayOf(
        Color(1f, 0.4f, 0.6f, 1f),
        Color(0.4f, 0.8f, 1f, 1f),
        Color(1f, 0.85f, 0.4f, 1f),
        Color(0.4f, 1f, 0.45f, 1f),
        Color(0.53f, 0.4f, 0.93f, 1f),
        Color(1f, 0.6f, 0.3f, 1f),
        Color(0.3f, 1f, 0.8f, 1f),
        Color(0.9f, 0.3f, 0.5f, 1f),
        Color(0.6f, 0.9f, 0.3f, 1f)
    )

    fun setColumnPressed(column: Int, pressed: Boolean) {
        if (column in pressedColumns.indices) pressedColumns[column] = pressed
    }

    fun render(shapeRenderer: ShapeRenderer, batch: SpriteBatch, font: BitmapFont, layout: GlyphLayout) {
        renderStage(shapeRenderer)
        renderHoldBodies(shapeRenderer)
        renderTapNotes(shapeRenderer)
        renderHoldHeads(shapeRenderer)
        renderReceptors(shapeRenderer, batch, font, layout)
    }

    private fun renderStage(shapeRenderer: ShapeRenderer) {
        shapeRenderer.setColor(0.07f, 0.07f, 0.07f, settings.stageOpacity)
        shapeRenderer.rect(playfield.stageX, 0f, playfield.stageWidth, playfield.screenHeight)
        shapeRenderer.setColor(0.3f, 0.3f, 0.3f, 0.5f)
        shapeRenderer.rect(playfield.stageX - 2f, 0f, 2f, playfield.screenHeight)
        shapeRenderer.rect(playfield.stageX + playfield.stageWidth, 0f, 2f, playfield.screenHeight)
        shapeRenderer.setColor(1f, 1f, 1f, 0.2f)
        shapeRenderer.rect(playfield.stageX, playfield.hitPosition - 5f, playfield.stageWidth, 10f)
    }

    private fun renderTapNotes(shapeRenderer: ShapeRenderer) {
        val timeElapsed = engine.timeElapsed
        for (col in 0 until engine.keyCount) {
            val columnObjects = engine.columns.getOrNull(col) ?: continue
            for (obj in columnObjects) {
                if (obj !is TapData || obj.isHoldHead) continue
                val offset = engine.getObjectOffset(timeElapsed, obj.time)
                val y = playfield.hitPosition - offset
                if (y < -100f || y > playfield.screenHeight + 100f) continue
                val x = playfield.getColumnX(col)
                val w = playfield.columnWidth
                val color = getColorForColumn(col)
                renderNoteShape(shapeRenderer, x, y, w, color, settings.noteScale)
            }
        }
    }

    private fun renderHoldBodies(shapeRenderer: ShapeRenderer) {
        val timeElapsed = engine.timeElapsed
        for (col in 0 until engine.keyCount) {
            val columnObjects = engine.columns.getOrNull(col) ?: continue
            for (obj in columnObjects) {
                if (obj !is HoldData) continue
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
                val w = playfield.columnWidth * settings.noteScale * 0.6f
                val offsetX = (playfield.columnWidth - w) / 2f
                val color = getColorForColumn(col)
                val bodyColor = Color(color.r * 0.6f, color.g * 0.6f, color.b * 0.6f, 0.8f)
                shapeRenderer.setColor(bodyColor)
                shapeRenderer.rect(x + offsetX, topY, w, bodyHeight)
            }
        }
    }

    private fun renderHoldHeads(shapeRenderer: ShapeRenderer) {
        val timeElapsed = engine.timeElapsed
        for (col in 0 until engine.keyCount) {
            val columnObjects = engine.columns.getOrNull(col) ?: continue
            for (obj in columnObjects) {
                if (obj !is HoldData) continue
                val offset = engine.getObjectOffset(timeElapsed, obj.time)
                val y = playfield.hitPosition - offset
                if (y < -100f || y > playfield.screenHeight + 100f) continue
                val x = playfield.getColumnX(col)
                val w = playfield.columnWidth
                val color = getColorForColumn(col)
                renderNoteShape(shapeRenderer, x, y, w, color, settings.noteScale)
            }
        }
    }

    private fun renderReceptors(
        shapeRenderer: ShapeRenderer,
        batch: SpriteBatch,
        font: BitmapFont,
        layout: GlyphLayout
    ) {
        for (col in 0 until engine.keyCount) {
            val x = playfield.getColumnX(col)
            val y = playfield.hitPosition
            val w = playfield.columnWidth
            val pressed = pressedColumns[col]
            val color = getColorForColumn(col)

            shapeRenderer.setColor(color.r, color.g, color.b, if (pressed) 0.8f else 0.3f)
            shapeRenderer.rect(x, y - 5f, w, 10f)

            val receptorAlpha = settings.receptorOpacity
            shapeRenderer.setColor(0.5f, 0.5f, 0.5f, receptorAlpha)
            shapeRenderer.rect(x + 2f, y - w / 2f + 2f, w - 4f, w - 4f)

            if (pressed && settings.receptorLighting) {
                shapeRenderer.setColor(color.r, color.g, color.b, 0.4f)
                val lightHeight = playfield.screenHeight * 0.3f
                shapeRenderer.rect(x, y - lightHeight, w, lightHeight)
            }
        }
    }

    private fun renderNoteShape(
        shapeRenderer: ShapeRenderer,
        x: Float,
        y: Float,
        width: Float,
        color: Color,
        scale: Float
    ) {
        val w = width * scale
        val h = w * 0.4f
        val offsetX = (width - w) / 2f
        shapeRenderer.setColor(color)
        when (settings.skinStyle) {
            GameConstants.SkinStyle.ARROW -> {
                val cx = x + width / 2f
                val halfW = w / 2f
                shapeRenderer.triangle(cx, y + halfW, cx - halfW, y - halfW, cx + halfW, y - halfW)
            }
            GameConstants.SkinStyle.DIAMOND -> {
                val cx = x + width / 2f
                val halfW = w / 2f
                shapeRenderer.triangle(cx, y + halfW, cx - halfW, y, cx + halfW, y)
                shapeRenderer.triangle(cx, y - halfW, cx - halfW, y, cx + halfW, y)
            }
            else -> {
                shapeRenderer.rect(x + offsetX, y - h / 2f, w, h)
            }
        }
    }

    private fun getColorForColumn(col: Int): Color {
        return laneColors[col % laneColors.size]
    }

    fun dispose() {}
}
