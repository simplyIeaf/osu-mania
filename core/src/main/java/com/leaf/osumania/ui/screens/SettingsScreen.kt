package com.leaf.osumania.ui.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Slider
import com.badlogic.gdx.scenes.scene2d.ui.Slider.SliderStyle
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.leaf.osumania.engine.GameConstants
import com.leaf.osumania.ui.OsuColors
import com.leaf.osumania.ui.OsuFonts
import com.leaf.osumania.ui.OsuManiaGame

class SettingsScreen(private val game: OsuManiaGame) : Screen {
    private lateinit var batch: SpriteBatch
    private lateinit var stage: Stage
    private lateinit var skin: Skin

    private fun makePanelDrawable(): Drawable {
        val pix = Pixmap(1, 1, Pixmap.Format.RGBA8888).apply {
            setColor(OsuColors.PANEL.r, OsuColors.PANEL.g, OsuColors.PANEL.b, 1f)
            fill()
        }
        val tex = com.badlogic.gdx.graphics.Texture(pix)
        pix.dispose()
        return TextureRegionDrawable(TextureRegion(tex))
    }

    private fun makeSliderDrawable(color: Color): Drawable {
        val pix = Pixmap(1, 1, Pixmap.Format.RGBA8888).apply {
            setColor(color.r, color.g, color.b, color.a)
            fill()
        }
        val tex = com.badlogic.gdx.graphics.Texture(pix)
        pix.dispose()
        return TextureRegionDrawable(TextureRegion(tex))
    }

    override fun show() {
        batch = SpriteBatch()
        stage = Stage(ScreenViewport())
        Gdx.input.inputProcessor = stage

        val panelDrawable = makePanelDrawable()
        val knobDrawable = makeSliderDrawable(OsuColors.ACCENT_PINK)

        skin = Skin()
        skin.add("default", Label.LabelStyle(OsuFonts.get(16), Color.WHITE))
        skin.add("small", Label.LabelStyle(OsuFonts.get(13), OsuColors.TEXT_SECONDARY))
        skin.add("section", Label.LabelStyle(OsuFonts.get(20), OsuColors.ACCENT_PINK))

        val btnStyle = TextButton.TextButtonStyle()
        btnStyle.up = panelDrawable
        btnStyle.down = panelDrawable
        btnStyle.over = panelDrawable
        btnStyle.font = OsuFonts.get(18)
        btnStyle.fontColor = Color.WHITE
        skin.add("default", btnStyle)

        val sliderStyle = SliderStyle(
            makeSliderDrawable(OsuColors.BORDER),
            knobDrawable
        )
        skin.add("default-horizontal", sliderStyle)

        val root = Table()
        root.setFillParent(true)
        root.top()
        root.pad(10f)

        val header = Table()
        header.background = panelDrawable
        header.pad(8f)
        val backBtn = TextButton("< Back", skin, "default")
        backBtn.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                game.settings.save()
                game.screen = MainMenuScreen(game)
            }
        })
        header.add(backBtn).left()
        val title = Label("Settings", skin, "section")
        header.add(title).expandX().center()
        root.add(header).fillX().padBottom(10f).row()

        val scrollContent = Table()
        scrollContent.pad(4f)

        addSection(scrollContent, "Gameplay")
        addSlider(scrollContent, "Scroll Speed", 1f, 40f, game.settings.scrollSpeed) { game.settings.scrollSpeed = it }
        addSlider(scrollContent, "Hit Position", 0f, 200f, game.settings.hitPositionOffset) { game.settings.hitPositionOffset = it }
        addSlider(scrollContent, "Background Dim", 0f, 1f, game.settings.backgroundDim) { game.settings.backgroundDim = it }
        addToggle(scrollContent, "Upscroll", game.settings.upscroll) { game.settings.upscroll = it }
        addToggle(scrollContent, "Performance Mode", game.settings.performanceMode) { game.settings.performanceMode = it }

        addSection(scrollContent, "Display")
        addSlider(scrollContent, "Playfield Size", 0.3f, 1.0f, game.settings.stageWidth) { game.settings.stageWidth = it }
        addSlider(scrollContent, "Playfield X Position", -0.5f, 0.5f, game.settings.stageXOffset) { game.settings.stageXOffset = it }
        addSlider(scrollContent, "Stage Opacity", 0f, 1f, game.settings.stageOpacity) { game.settings.stageOpacity = it }
        addSlider(scrollContent, "Receptor Opacity", 0f, 1f, game.settings.receptorOpacity) { game.settings.receptorOpacity = it }
        addSlider(scrollContent, "Note Scale", 0.5f, 1f, game.settings.noteScale) { game.settings.noteScale = it }
        addSlider(scrollContent, "Note Offset", -100f, 100f, game.settings.noteOffset) { game.settings.noteOffset = it }

        addSection(scrollContent, "Skin")
        addSlider(scrollContent, "Hue", 0f, 360f, game.settings.hue) { game.settings.hue = it }
        addToggle(scrollContent, "Darker Hold Notes", game.settings.darkerHoldNotes) { game.settings.darkerHoldNotes = it }

        val skinStyleRow = Table()
        skinStyleRow.add(Label("Note Style:", skin, "default")).left().padRight(10f)
        for (style in GameConstants.SkinStyle.entries) {
            val btn = TextButton(style.name, skin, "default")
            if (style == game.settings.skinStyle) btn.color = OsuColors.ACCENT_PINK
            btn.addListener(object : ClickListener() {
                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                    game.settings.skinStyle = style
                }
            })
            skinStyleRow.add(btn).width(80f).height(35f).padRight(4f)
        }
        scrollContent.add(skinStyleRow).fillX().padTop(6f).row()

        addSection(scrollContent, "Volume")
        addSlider(scrollContent, "Music Volume", 0f, 1f, game.settings.musicVolume) { game.settings.musicVolume = it }
        addSlider(scrollContent, "SFX Volume", 0f, 1f, game.settings.sfxVolume) { game.settings.sfxVolume = it }
        addSlider(scrollContent, "Audio Offset", -300f, 300f, game.settings.audioOffset) { game.settings.audioOffset = it }

        addSection(scrollContent, "HUD")
        addToggle(scrollContent, "Show Score", game.settings.showScore) { game.settings.showScore = it }
        addToggle(scrollContent, "Show Combo", game.settings.showCombo) { game.settings.showCombo = it }
        addToggle(scrollContent, "Show Accuracy", game.settings.showAccuracy) { game.settings.showAccuracy = it }
        addToggle(scrollContent, "Show Health Bar", game.settings.showHealthBar) { game.settings.showHealthBar = it }
        addToggle(scrollContent, "Show Error Bar", game.settings.showErrorBar) { game.settings.showErrorBar = it }
        addToggle(scrollContent, "Show FPS", game.settings.showFps) { game.settings.showFps = it }

        val scrollPane = com.badlogic.gdx.scenes.scene2d.ui.ScrollPane(scrollContent, skin)
        scrollPane.setFadeScrollBars(false)
        root.add(scrollPane).expand().fill()

        stage.addActor(root)
    }

    private fun addSection(table: Table, title: String) {
        table.padTop(8f)
        val label = Label(title, skin, "section")
        table.add(label).left().padTop(12f).padBottom(4f).row()
    }

    private fun addSlider(table: Table, name: String, min: Float, max: Float, current: Float, onChange: (Float) -> Unit) {
        val row = Table()
        row.add(Label("$name:", skin, "default")).left().width(160f)
        val slider = Slider(min, max, (max - min) / 100f, false, skin)
        slider.value = current
        slider.addListener(object : com.badlogic.gdx.scenes.scene2d.utils.ChangeListener() {
            override fun changed(event: ChangeEvent?, actor: com.badlogic.gdx.scenes.scene2d.Actor?) {
                onChange(slider.value)
            }
        })
        row.add(slider).expandX().fillX().padLeft(8f).padRight(8f)
        val valueLabel = Label("%.1f".format(current), skin, "small")
        slider.addListener(object : com.badlogic.gdx.scenes.scene2d.utils.ChangeListener() {
            override fun changed(event: ChangeEvent?, actor: com.badlogic.gdx.scenes.scene2d.Actor?) {
                valueLabel.setText("%.1f".format(slider.value))
            }
        })
        row.add(valueLabel).width(50f)
        table.add(row).fillX().padTop(4f).row()
    }

    private fun addToggle(table: Table, name: String, current: Boolean, onChange: (Boolean) -> Unit) {
        val row = Table()
        row.add(Label(name, skin, "default")).left().expandX()
        val btn = TextButton(if (current) "ON" else "OFF", skin, "default")
        btn.color = if (current) OsuColors.ACCENT_GREEN else OsuColors.TEXT_SECONDARY
        btn.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                val newVal = !current
                onChange(newVal)
                btn.setText(if (newVal) "ON" else "OFF")
                btn.color = if (newVal) OsuColors.ACCENT_GREEN else OsuColors.TEXT_SECONDARY
            }
        })
        row.add(btn).width(70f).height(35f)
        table.add(row).fillX().padTop(4f).row()
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
        stage.dispose()
        skin.dispose()
    }
}
