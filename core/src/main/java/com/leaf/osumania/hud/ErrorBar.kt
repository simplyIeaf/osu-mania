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

    private val color320g = Color(0f, 1f, 1f, 1f)
    private val color300 = Color(0.2f, 0.4f, 1f, 1f)
    private val color200 = Color(0f, 0.8f, 0.2f, 1f)
    private val color100 = Color(0.2f, 0.8f, 0f, 1f)
    private val color50 = Color(1f, 0.6f, 0f, 1f)
    private val colorMiss = Color(1f, 0.2f, 0.2f, 1f)

    private val section320gWidth = 0.15f
    private val section300Width = 0.25f
    private val section200Width = 0.2f
    private val section100Width = 0.2f
    private val section50Width = 0.2f

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

        shapeRenderer.rect(sx, sy, sw, sh, Color(0f, 0f, 0f, 0.6f))

        var offset = sx
        val s320w = sw * section320gWidth
        val s300w = sw * section300Width
        val s200w = sw * section200Width
        val s100w = sw * section100Width
        val s50w = sw * section50Width

        val sectionAlpha = 0.4f

        val c320 = Color(color320g); c320.a = sectionAlpha
        shapeRenderer.rect(offset, sy, s320w, sh, c320)
        offset += s320w

        val c300 = Color(color300); c300.a = sectionAlpha
        shapeRenderer.rect(offset, sy, s300w, sh, c300)
        offset += s300w

        val c200 = Color(color200); c200.a = sectionAlpha
        shapeRenderer.rect(offset, sy, s200w, sh, c200)
        offset += s200w

        val c100 = Color(color100); c100.a = sectionAlpha
        shapeRenderer.rect(offset, sy, s100w, sh, c100)
        offset += s100w

        val c50 = Color(color50); c50.a = sectionAlpha
        shapeRenderer.rect(offset, sy, s50w, sh, c50)
        offset += s50w

        val centerX = sx + sw / 2f
        val triangleSize = sh * 0.6f
        shapeRenderer.triangle(
            centerX, sy + sh + triangleSize,
            centerX - triangleSize / 2f, sy + sh,
            centerX + triangleSize / 2f, sy + sh,
            Color.WHITE
        )

        for (mark in marks) {
            val normalizedError = (mark.error / maxError).coerceIn(-1f, 1f)
            val markX = centerX + normalizedError * (sw / 2f)
            val markColor = getJudgementColor(mark.judgement)
            markColor.a = mark.alpha * 0.9f
            val markW = 2f * scale
            val markH = sh * 0.7f
            shapeRenderer.rect(markX - markW / 2f, sy + (sh - markH) / 2f, markW, markH, markColor)
        }
    }

    private fun getJudgementColor(judgement: Int): Color {
        return when (judgement) {
            GameConstants.Judgement.IDOL -> Color(color320g)
            GameConstants.Judgement.MISS -> Color(colorMiss)
            GameConstants.Judgement.BAD -> Color(color50)
            GameConstants.Judgement.GOOD -> Color(color100)
            GameConstants.Judgement.GREAT -> Color(color200)
            GameConstants.Judgement.PERFECT -> Color(color300)
            else -> Color.WHITE
        }
    }
}
