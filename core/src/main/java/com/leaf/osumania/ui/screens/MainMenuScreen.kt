package com.leaf.osumania.ui.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.leaf.osumania.ui.OsuColors
import com.leaf.osumania.ui.OsuFonts
import com.leaf.osumania.ui.OsuManiaGame

class MainMenuScreen(private val game: OsuManiaGame) : Screen {
    private lateinit var batch: SpriteBatch
    private lateinit var stage: Stage
    private lateinit var skin: Skin
    private var titleFont: BitmapFont? = null
    private var layout: GlyphLayout? = null
    private var time = 0f

    private fun makePanelDrawable(): Drawable {
        val pix = Pixmap(1, 1, Pixmap.Format.RGBA8888).apply {
            setColor(OsuColors.PANEL.r, OsuColors.PANEL.g, OsuColors.PANEL.b, 1f)
            fill()
        }
        val tex = com.badlogic.gdx.graphics.Texture(pix)
        pix.dispose()
        return TextureRegionDrawable(com.badlogic.gdx.graphics.g2d.TextureRegion(tex))
    }

    override fun show() {
        batch = SpriteBatch()
        stage = Stage(ScreenViewport())
        Gdx.input.inputProcessor = stage

        val panelDrawable = makePanelDrawable()

        skin = Skin()
        skin.add("default-font", OsuFonts.get(22))
        skin.add("default", Label.LabelStyle(OsuFonts.get(22), Color.WHITE))
        skin.add("title", Label.LabelStyle(OsuFonts.get(42), OsuColors.ACCENT_PINK))

        val btnStyle = TextButton.TextButtonStyle()
        btnStyle.up = panelDrawable
        btnStyle.down = panelDrawable
        btnStyle.over = panelDrawable
        btnStyle.font = OsuFonts.get(24)
        btnStyle.fontColor = Color.WHITE
        skin.add("default", btnStyle)

        val root = Table()
        root.setFillParent(true)
        root.center()

        val title = Label("osu!mania", skin, "title")
        root.add(title).padBottom(80f).row()

        val playBtn = TextButton("  Play  ", skin, "default")
        playBtn.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                game.screen = SongSelectScreen(game)
            }
        })
        root.add(playBtn).width(280f).height(60f).padBottom(16f).row()

        val settingsBtn = TextButton("  Settings  ", skin, "default")
        settingsBtn.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                game.screen = SettingsScreen(game)
            }
        })
        root.add(settingsBtn).width(280f).height(60f).padBottom(16f).row()

        val exitBtn = TextButton("  Exit  ", skin, "default")
        exitBtn.color = OsuColors.ACCENT_RED
        exitBtn.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                Gdx.app.exit()
            }
        })
        root.add(exitBtn).width(280f).height(60f).row()

        stage.addActor(root)
        titleFont = OsuFonts.get(42)
        layout = GlyphLayout()
    }

    override fun render(delta: Float) {
        time += delta
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
        try { batch.dispose() } catch (_: Exception) {}
        try { stage.dispose() } catch (_: Exception) {}
        try { skin.dispose() } catch (_: Exception) {}
    }
}
