package com.leaf.osumania.ui.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.leaf.osumania.ui.OsuColors
import com.leaf.osumania.ui.OsuFonts
import com.leaf.osumania.ui.OsuManiaGame

class LoadingScreen(private val game: OsuManiaGame) : Screen {
    private lateinit var batch: SpriteBatch
    private lateinit var shapeRenderer: ShapeRenderer
    private lateinit var stage: Stage
    private lateinit var skin: Skin
    private var loadProgress = 0f
    private var loaded = false
    private var titleFont: BitmapFont? = null
    private var layout: GlyphLayout? = null

    override fun show() {
        batch = SpriteBatch()
        shapeRenderer = ShapeRenderer()
        stage = Stage(ScreenViewport())
        Gdx.input.inputProcessor = stage

        skin = Skin()
        skin.add("default", Label.LabelStyle(OsuFonts.get(22), Color.WHITE))
        skin.add("title", Label.LabelStyle(OsuFonts.get(48), OsuColors.ACCENT_PINK))

        val table = Table(skin)
        table.setFillParent(true)
        table.center()
        val titleLabel = Label("osu!mania", skin, "title")
        table.add(titleLabel).padBottom(40f).row()
        stage.addActor(table)

        titleFont = OsuFonts.get(48)
        layout = GlyphLayout()
    }

    override fun render(delta: Float) {
        if (!loaded) {
            loadProgress += delta * 0.8f
            if (loadProgress >= 1f) {
                loadProgress = 1f
                loaded = true
                game.screen = MainMenuScreen(game)
                return
            }
        }

        Gdx.gl.glClearColor(OsuColors.BACKGROUND.r, OsuColors.BACKGROUND.g, OsuColors.BACKGROUND.b, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        val w = Gdx.graphics.width.toFloat()
        val h = Gdx.graphics.height.toFloat()
        shapeRenderer.projectionMatrix.setToOrtho2D(0f, 0f, w, h)
        batch.projectionMatrix.setToOrtho2D(0f, 0f, w, h)

        val cx = w / 2f
        val cy = h / 2f

        batch.begin()
        titleFont?.let { font ->
            layout?.setText(it, "osu!mania")
            it.color = OsuColors.ACCENT_PINK
            it.draw(batch, layout!!, cx - layout!!.width / 2f, cy + 60f)
        }
        batch.end()

        val barW = 300f
        val barH = 6f
        val barX = cx - barW / 2f
        val barY = cy - 40f

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.setColor(OsuColors.BORDER)
        shapeRenderer.rect(barX, barY, barW, barH)
        shapeRenderer.setColor(OsuColors.PRIMARY)
        shapeRenderer.rect(barX, barY, barW * loadProgress, barH)
        shapeRenderer.end()

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
