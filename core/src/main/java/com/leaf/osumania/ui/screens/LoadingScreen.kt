package com.leaf.osumania.ui.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.leaf.osumania.ui.OsuColors
import com.leaf.osumania.ui.OsuFonts
import com.leaf.osumania.ui.OsuManiaGame

class LoadingScreen(private val game: OsuManiaGame) : Screen {
    private var batch: SpriteBatch? = null
    private var shapeRenderer: ShapeRenderer? = null
    private var stage: Stage? = null
    private var skin: Skin? = null
    private var loadProgress = 0f
    private var loaded = false
    private var titleFont: BitmapFont? = null
    private var layout: GlyphLayout? = null
    private var disposables = mutableListOf<com.badlogic.gdx.utils.Disposable>()

    override fun show() {
        try {
            batch = SpriteBatch()
            shapeRenderer = ShapeRenderer()
            stage = Stage(ScreenViewport())
            Gdx.input.inputProcessor = stage

            val pix = Pixmap(1, 1, Pixmap.Format.RGBA8888).apply {
                setColor(Color.WHITE)
                fill()
            }
            val tex = Texture(pix)
            pix.dispose()

            skin = Skin()
            skin!!.add("white", tex)

            val lblStyle = Label.LabelStyle(OsuFonts.get(22), Color.WHITE)
            val titleStyle = Label.LabelStyle(OsuFonts.get(48), OsuColors.ACCENT_PINK)
            skin!!.add("default", lblStyle)
            skin!!.add("title", titleStyle)

            val table = Table()
            table.setFillParent(true)
            table.center()
            val titleLabel = Label("osu!mania", skin, "title")
            table.add(titleLabel).padBottom(40f).row()
            stage!!.addActor(table)

            titleFont = OsuFonts.get(48)
            layout = GlyphLayout()
        } catch (e: Exception) {
            Gdx.app.error("OsuMania", "LoadingScreen.show() failed", e)
        }
    }

    override fun render(delta: Float) {
        if (!loaded) {
            loadProgress += delta * 0.8f
            if (loadProgress >= 1f) {
                loadProgress = 1f
                loaded = true
                try {
                    game.screen = MainMenuScreen(game)
                } catch (e: Exception) {
                    Gdx.app.error("OsuMania", "Failed to transition to MainMenu", e)
                }
                return
            }
        }

        Gdx.gl.glClearColor(OsuColors.BACKGROUND.r, OsuColors.BACKGROUND.g, OsuColors.BACKGROUND.b, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        val w = Gdx.graphics.width.toFloat()
        val h = Gdx.graphics.height.toFloat()
        val sr = shapeRenderer ?: return
        val b = batch ?: return

        sr.projectionMatrix.setToOrtho2D(0f, 0f, w, h)
        b.projectionMatrix.setToOrtho2D(0f, 0f, w, h)

        val cx = w / 2f
        val cy = h / 2f

        b.begin()
        val f = titleFont
        val l = layout
        if (f != null && l != null) {
            l.setText(f, "osu!mania")
            f.color = OsuColors.ACCENT_PINK
            f.draw(b, l, cx - l.width / 2f, cy + 60f)
        }
        b.end()

        val barW = 300f
        val barH = 6f
        val barX = cx - barW / 2f
        val barY = cy - 40f

        sr.begin(ShapeRenderer.ShapeType.Filled)
        sr.setColor(OsuColors.BORDER)
        sr.rect(barX, barY, barW, barH)
        sr.setColor(OsuColors.PRIMARY)
        sr.rect(barX, barY, barW * loadProgress, barH)
        sr.end()

        stage?.act(delta)
        stage?.draw()
    }

    override fun resize(width: Int, height: Int) {
        stage?.viewport?.update(width, height, true)
    }

    override fun pause() {}
    override fun resume() {}

    override fun hide() {
        dispose()
    }

    override fun dispose() {
        try { batch?.dispose() } catch (_: Exception) {}
        try { shapeRenderer?.dispose() } catch (_: Exception) {}
        try { stage?.dispose() } catch (_: Exception) {}
        try { skin?.dispose() } catch (_: Exception) {}
        disposables.forEach { try { it.dispose() } catch (_: Exception) {} }
    }
}
