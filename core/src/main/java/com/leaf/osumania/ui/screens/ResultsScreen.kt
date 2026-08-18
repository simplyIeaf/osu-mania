package com.leaf.osumania.ui.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.leaf.osumania.engine.GameConstants
import com.leaf.osumania.engine.GameEngine
import com.leaf.osumania.ui.OsuColors
import com.leaf.osumania.ui.OsuFonts
import com.leaf.osumania.ui.OsuManiaGame

class ResultsScreen(private val game: OsuManiaGame, private val engine: GameEngine) : Screen {
    private lateinit var batch: SpriteBatch
    private lateinit var shapeRenderer: ShapeRenderer
    private lateinit var stage: Stage
    private lateinit var skin: Skin

    override fun show() {
        batch = SpriteBatch()
        shapeRenderer = ShapeRenderer()
        stage = Stage(ScreenViewport())
        Gdx.input.inputProcessor = stage

        skin = Skin()
        skin.add("default", Label.LabelStyle(OsuFonts.get(18), Color.WHITE))
        skin.add("title", Label.LabelStyle(OsuFonts.get(28), Color.WHITE))
        skin.add("small", Label.LabelStyle(OsuFonts.get(14), OsuColors.TEXT_SECONDARY))

        val root = Table()
        root.setFillParent(true)
        root.pad(20f)
        root.top()

        val grade = engine.scoreSystem.getLetterGrade()
        val gradeColor = when (grade) {
            "SS", "X" -> OsuColors.ACCENT_YELLOW
            "S" -> OsuColors.ACCENT_YELLOW
            "A" -> OsuColors.ACCENT_PURPLE
            "B" -> OsuColors.ACCENT_BLUE
            "C" -> OsuColors.ACCENT_PINK
            "D" -> OsuColors.ACCENT_RED
            else -> OsuColors.TEXT_SECONDARY
        }

        val gradeLabel = Label(grade, skin, "title")
        gradeLabel.color = gradeColor
        root.add(gradeLabel).padBottom(20f).row()

        val scoreLabel = Label("Score: ${engine.scoreSystem.score}", skin, "title")
        root.add(scoreLabel).padBottom(8f).row()

        val accPct = "%.2f".format(engine.scoreSystem.accuracy * 100f)
        val accLabel = Label("Accuracy: $accPct%", skin, "title")
        root.add(accLabel).padBottom(8f).row()

        val comboLabel = Label("Max Combo: ${engine.scoreSystem.maxCombo}x", skin, "title")
        root.add(comboLabel).padBottom(20f).row()

        val judgementsTable = Table()
        for (j in intArrayOf(320, 300, 200, 100, 50, 0)) {
            val count = engine.scoreSystem.getJudgementCount(j)
            val name = when (j) { 320 -> "320g"; else -> j.toString() }
            val color = GameConstants.JUDGEMENT_COLORS[j] ?: Color.WHITE
            val nameLabel = Label(name, skin, "small")
            nameLabel.color = color
            judgementsTable.add(nameLabel).width(40f).left().padRight(8f)
            val countLabel = Label(count.toString(), skin, "default")
            judgementsTable.add(countLabel).width(60f).left().row()
        }
        root.add(judgementsTable).padBottom(20f).row()

        val pp = engine.scoreSystem.calculatePp(engine.beatmapData.difficulty.keyCount.toFloat())
        val ppLabel = Label("PP: ${pp.toInt()}", skin, "title")
        root.add(ppLabel).padBottom(30f).row()

        val btnTable = Table()

        val backBtn = TextButton("Back", skin, "default")
        backBtn.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                game.screen = SongSelectScreen(game)
            }
        })
        btnTable.add(backBtn).width(140f).height(50f).padRight(10f)

        val retryBtn = TextButton("Retry", skin, "default")
        retryBtn.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                game.screen = GameplayScreen(game, engine.beatmapData)
            }
        })
        btnTable.add(retryBtn).width(140f).height(50f)

        root.add(btnTable)

        stage.addActor(root)
    }

    override fun render(delta: Float) {
        Gdx.gl.glClearColor(OsuColors.BACKGROUND.r, OsuColors.BACKGROUND.g, OsuColors.BACKGROUND.b, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        stage.act(delta)
        stage.draw()
    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
    }

    override fun pause() {}
    override fun resume() {}

    override fun hide() {
        dispose()
    }

    override fun dispose() {
        batch.dispose()
        shapeRenderer.dispose()
        stage.dispose()
        skin.dispose()
    }
}
