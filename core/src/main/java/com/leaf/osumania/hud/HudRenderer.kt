package com.leaf.osumania.hud

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.Align
import com.leaf.osumania.engine.GameConstants
import com.leaf.osumania.engine.GameEngine

class HudRenderer(private val engine: GameEngine) {

    val scoreFont = BitmapFont()
    val comboFont = BitmapFont()
    val accuracyFont = BitmapFont()
    val judgementFont = BitmapFont()
    val smallFont = BitmapFont()
    private val layout = GlyphLayout()

    var judgementTimer = 0f
    var currentJudgement = -1
    var judgementScale = 1f
    var judgementAlpha = 1f
    var judgementEarlyLate = ""

    val errorBar = ErrorBar()

    private val judgementDuration = 0.8f
    private val judgementScaleDuration = 0.3f
    private val judgementStartScale = 1.2f
    private val judgementEndScale = 1.0f

    private data class TimingMark(
        var error: Float,
        var judgement: Int,
        var alpha: Float = 1f,
        var age: Float = 0f
    )

    private val timingMarks = mutableListOf<TimingMark>()
    private val markLifetime = 4f

    private var kpsCounter = 0
    private var totalHits = 0
    private var kpsTimer = 0f
    private var currentKps = 0

    private var healthFlashTimer = 0f
    private var previousHealth = 1f

    private val judgementColors = intArrayOf(
        0xFF00FFFF.toInt(),
        0xFF3366FF.toInt(),
        0xFF00CC33.toInt(),
        0xFF33CC00.toInt(),
        0xFFFF9900.toInt(),
        0xFFFF3333.toInt()
    )

    private val judgementNames = arrayOf("320g", "300", "200", "100", "50", "Miss")

    init {
        scoreFont.data.setScale(1.5f)
        comboFont.data.setScale(1.8f)
        accuracyFont.data.setScale(1.0f)
        judgementFont.data.setScale(1.5f)
        smallFont.data.setScale(0.8f)
    }

    fun showJudgement(judgement: Int, earlyOrLate: String?) {
        currentJudgement = judgement
        judgementTimer = 0f
        judgementScale = judgementStartScale
        judgementAlpha = 1f
        judgementEarlyLate = earlyOrLate ?: ""
        if (judgement != GameConstants.Judgement.MISS) {
            kpsCounter++
            totalHits++
        } else {
            totalHits++
        }
    }

    fun addTimingMark(error: Float, judgement: Int) {
        if (timingMarks.size >= 150) {
            timingMarks.removeAt(0)
        }
        timingMarks.add(TimingMark(error, judgement))
        errorBar.addMark(error, judgement)
    }

    fun update(deltaTime: Float) {
        if (judgementTimer < judgementDuration) {
            judgementTimer += deltaTime
            if (judgementTimer < judgementScaleDuration) {
                val t = judgementTimer / judgementScaleDuration
                judgementScale = judgementStartScale + (judgementEndScale - judgementStartScale) * t
            } else {
                judgementScale = judgementEndScale
            }
            if (judgementTimer > judgementDuration * 0.6f) {
                val fadeStart = judgementDuration * 0.6f
                judgementAlpha = 1f - ((judgementTimer - fadeStart) / (judgementDuration - fadeStart)).coerceIn(0f, 1f)
            }
        }

        healthFlashTimer = (healthFlashTimer - deltaTime).coerceAtLeast(0f)
        if (engine.health < previousHealth) {
            healthFlashTimer = 0.15f
        }
        previousHealth = engine.health

        kpsTimer += deltaTime
        if (kpsTimer >= 1f) {
            currentKps = kpsCounter
            kpsCounter = 0
            kpsTimer -= 1f
        }

        val iterator = timingMarks.iterator()
        while (iterator.hasNext()) {
            val mark = iterator.next()
            mark.age += deltaTime
            mark.alpha = (1f - mark.age / markLifetime).coerceIn(0f, 1f)
            if (mark.age >= markLifetime) {
                iterator.remove()
            }
        }

        errorBar.update(deltaTime)
    }

    fun render(shapeRenderer: ShapeRenderer, batch: SpriteBatch, viewportWidth: Float, viewportHeight: Float) {
        batch.begin()

        if (engine.settings.showScore) {
            renderScore(batch, viewportWidth, viewportHeight)
        }

        if (engine.settings.showCombo) {
            renderCombo(batch, viewportWidth, viewportHeight)
        }

        if (engine.settings.showAccuracy) {
            renderAccuracy(batch, viewportWidth, viewportHeight)
        }

        if (engine.settings.showHealthBar) {
            batch.end()
            renderHealthBar(shapeRenderer, viewportWidth, viewportHeight)
            renderProgressBar(shapeRenderer, viewportWidth, viewportHeight)
            batch.begin()
        }

        if (engine.settings.showJudgement && judgementTimer < judgementDuration) {
            renderJudgement(batch, viewportWidth, viewportHeight)
        }

        if (engine.settings.showErrorBar) {
            batch.end()
            errorBar.x = viewportWidth / 2f
            errorBar.y = 20f
            errorBar.scale = engine.settings.errorBarScale
            errorBar.render(shapeRenderer)
            batch.begin()
        }

        renderKpsCounter(batch, viewportWidth, viewportHeight)
        renderJudgementCounter(batch, viewportWidth, viewportHeight)

        if (engine.settings.showFps) {
            renderFps(batch, viewportWidth, viewportHeight)
        }

        if (engine.countdownActive) {
            renderCountdown(batch, viewportWidth, viewportHeight)
        }

        batch.end()
    }

    private fun renderScore(batch: SpriteBatch, viewportWidth: Float, viewportHeight: Float) {
        val scoreText = String.format("%08d", engine.score)
        layout.setText(scoreFont, scoreText)
        scoreFont.color = Color.WHITE
        scoreFont.draw(batch, scoreText, viewportWidth - layout.width - 20f, viewportHeight - 20f)
    }

    private fun renderCombo(batch: SpriteBatch, viewportWidth: Float, viewportHeight: Float) {
        val comboText = "${engine.combo}x"
        layout.setText(comboFont, comboText)
        comboFont.color = Color.WHITE
        comboFont.draw(batch, comboText, (viewportWidth - layout.width) / 2f, viewportHeight - 60f)
    }

    private fun renderAccuracy(batch: SpriteBatch, viewportWidth: Float, viewportHeight: Float) {
        val accText = String.format("%.2f%%", engine.accuracy * 100f)
        layout.setText(accuracyFont, accText)
        accuracyFont.color = Color(0.8f, 0.8f, 0.8f, 1f)
        accuracyFont.draw(batch, accText, viewportWidth - layout.width - 20f, viewportHeight - 50f)
    }

    private fun renderHealthBar(shapeRenderer: ShapeRenderer, viewportWidth: Float, viewportHeight: Float) {
        val barWidth = 400f
        val barHeight = 5f
        val barX = viewportWidth - barWidth - 20f
        val barY = viewportHeight - 80f

        shapeRenderer.rect(barX, barY, barWidth, barHeight, Color(0.2f, 0.2f, 0.2f, 0.8f))

        val healthWidth = barWidth * engine.health.coerceIn(0f, 1f)
        val healthColor = when {
            engine.health > 0.5f -> Color(0.2f, 0.8f, 0.2f, 1f)
            engine.health > 0.25f -> Color(1f, 0.8f, 0f, 1f)
            else -> Color(1f, 0.2f, 0.2f, 1f)
        }
        shapeRenderer.rect(barX, barY, healthWidth, barHeight, healthColor)

        if (healthFlashTimer > 0f) {
            val flashAlpha = (healthFlashTimer / 0.15f) * 0.5f
            shapeRenderer.rect(barX, barY, barWidth, barHeight, Color(1f, 0f, 0f, flashAlpha))
        }
    }

    private fun renderProgressBar(shapeRenderer: ShapeRenderer, viewportWidth: Float, viewportHeight: Float) {
        val barWidth = 400f
        val barHeight = 5f
        val barX = viewportWidth - barWidth - 20f
        val barY = viewportHeight - 90f

        shapeRenderer.rect(barX, barY, barWidth, barHeight, Color(0.2f, 0.2f, 0.2f, 0.8f))

        val progress = engine.progress.coerceIn(0f, 1f)
        shapeRenderer.rect(barX, barY, barWidth * progress, barHeight, Color(0.3f, 0.5f, 1f, 1f))
    }

    private fun renderJudgement(batch: SpriteBatch, viewportWidth: Float, viewportHeight: Float) {
        val name = when (currentJudgement) {
            GameConstants.Judgement.IDOL -> "320g"
            GameConstants.Judgement.PERFECT -> "300"
            GameConstants.Judgement.GREAT -> "200"
            GameConstants.Judgement.GOOD -> "100"
            GameConstants.Judgement.BAD -> "50"
            GameConstants.Judgement.MISS -> "Miss"
            else -> ""
        }

        judgementFont.color.a = judgementAlpha
        judgementFont.data.setScale(1.5f * judgementScale)

        var displayText = name
        if (judgementEarlyLate.isNotEmpty()) {
            displayText = "$name $judgementEarlyLate"
        }

        layout.setText(judgementFont, displayText)
        judgementFont.draw(
            batch, displayText,
            (viewportWidth - layout.width) / 2f,
            viewportHeight / 2f + layout.height / 2f
        )

        judgementFont.data.setScale(1.5f)
        judgementFont.color.a = 1f
    }

    private fun renderKpsCounter(batch: SpriteBatch, viewportWidth: Float, viewportHeight: Float) {
        val kpsText = "KPS: $currentKps"
        val totalText = "Total: $totalHits"
        smallFont.color = Color(0.7f, 0.7f, 0.7f, 0.8f)
        smallFont.draw(batch, kpsText, 10f, viewportHeight - 20f)
        smallFont.draw(batch, totalText, 10f, viewportHeight - 36f)
    }

    private fun renderJudgementCounter(batch: SpriteBatch, viewportWidth: Float, viewportHeight: Float) {
        val counts = engine.judgementCounts
        smallFont.color = Color(0.7f, 0.7f, 0.7f, 0.8f)
        var yOffset = viewportHeight - 60f
        for (i in judgementNames.indices) {
            val colour = Color(judgementColors[i])
            smallFont.color = colour
            val text = "${judgementNames[i]}: ${counts.getOrElse(i) { 0 }}"
            smallFont.draw(batch, text, 10f, yOffset)
            yOffset -= 16f
        }
    }

    private fun renderFps(batch: SpriteBatch, viewportWidth: Float, viewportHeight: Float) {
        val fpsText = "FPS: ${Gdx.graphics.framesPerSecond}"
        smallFont.color = Color(0.5f, 1f, 0.5f, 0.7f)
        smallFont.draw(batch, fpsText, 10f, viewportHeight - 4f)
    }

    private fun renderCountdown(batch: SpriteBatch, viewportWidth: Float, viewportHeight: Float) {
        val seconds = ((engine.countdownValue + 0.99f).toInt()).coerceAtLeast(0)
        val text = if (seconds > 0) seconds.toString() else "Go!"
        judgementFont.data.setScale(3f)
        judgementFont.color = Color.WHITE
        layout.setText(judgementFont, text)
        judgementFont.draw(
            batch, text,
            (viewportWidth - layout.width) / 2f,
            viewportHeight / 2f + layout.height / 2f
        )
        judgementFont.data.setScale(1.5f)
    }

    fun dispose() {
        scoreFont.dispose()
        comboFont.dispose()
        accuracyFont.dispose()
        judgementFont.dispose()
        smallFont.dispose()
    }
}
