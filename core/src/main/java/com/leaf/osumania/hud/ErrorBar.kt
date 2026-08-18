package com.leaf.osumania.hud

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.leaf.osumania.engine.GameConstants

class ErrorBar {
    var x: Float = 0f
    var y: Float = 0f
    var width: Float = 300f
    var height: Float = 20f
    var scale: Float = 1f

    private data class Mark(
        var error: Float,
        var judgement: Int,
        var alpha: Float = 1f,
        var age: Float = 0f
    )

    private val marks = mutableListOf<Mark>()
    private val maxMarks = 150
    private val markLifetime = 4f
    private val maxError = 80f

    private val color320 = Color(0f, 1f, 1f, 1f)
    private val color300 = Color(0.2f, 0.4f, 1f, 1f)
    private val color200 = Color(0f, 0.8f, 0.2f, 1f)
    private val color100 = Color(0.2f, 0.8f, 0f, 1f)
    private val color50 = Color(1f, 0.6f, 0f, 1f)
    private val colorMiss = Color(1f, 0.2f, 0.2f, 1f)

    fun addMark(error: Float, judgement: Int) {
        if (marks.size >= maxMarks) {
            marks.removeAt(0)
        }
        marks.add(Mark(error, judgement))
    }

    fun update(deltaTime: Float) {
        val iterator = marks.iterator()
        while (iterator.hasNext()) {
            val mark = iterator.next()
            mark.age += deltaTime
            mark.alpha = (1f - mark.age / markLifetime).coerceIn(0f, 1f)
            if (mark.age >= markLifetime) {
                iterator.remove()
            }
        }
    }

    fun render(shapeRenderer: ShapeRenderer) {
        val sw = width * scale
        val sh = height * scale
        val sx = x - sw / 2f
        val sy = y

        shapeRenderer.setColor(0f, 0f, 0f, 0.6f)
        shapeRenderer.rect(sx, sy, sw, sh)

        val sectionAlpha = 0.4f
        var offset = sx
        val sections = listOf(
            color320 to 0.15f,
            color300 to 0.25f,
            color200 to 0.2f,
            color100 to 0.2f,
            color50 to 0.2f
        )
        for ((color, widthFrac) in sections) {
            val secW = sw * widthFrac
            shapeRenderer.setColor(color.r, color.g, color.b, sectionAlpha)
            shapeRenderer.rect(offset, sy, secW, sh)
            offset += secW
        }

        val centerX = sx + sw / 2f
        val triangleSize = sh * 0.6f
        shapeRenderer.setColor(Color.WHITE)
        shapeRenderer.triangle(
            centerX, sy + sh + triangleSize,
            centerX - triangleSize / 2f, sy + sh,
            centerX + triangleSize / 2f, sy + sh
        )

        for (mark in marks) {
            val normalizedError = (mark.error / maxError).coerceIn(-1f, 1f)
            val markX = centerX + normalizedError * (sw / 2f)
            val markColor = getJudgementColor(mark.judgement)
            markColor.a = mark.alpha * 0.9f
            val markW = 2f * scale
            val markH = sh * 0.7f
            shapeRenderer.setColor(markColor)
            shapeRenderer.rect(markX - markW / 2f, sy + (sh - markH) / 2f, markW, markH)
        }
    }

    private fun getJudgementColor(judgement: Int): Color {
        return when (judgement) {
            GameConstants.JUDGEMENT_IDOL -> Color(color320)
            GameConstants.JUDGEMENT_MISS -> Color(colorMiss)
            GameConstants.JUDGEMENT_BAD -> Color(color50)
            GameConstants.JUDGEMENT_GOOD -> Color(color100)
            GameConstants.JUDGEMENT_GREAT -> Color(color200)
            GameConstants.JUDGEMENT_PERFECT -> Color(color300)
            else -> Color.WHITE
        }
    }
}
