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
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.TextField
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.leaf.osumania.beatmap.BeatmapData
import com.leaf.osumania.ui.OsuColors
import com.leaf.osumania.ui.OsuFonts
import com.leaf.osumania.ui.OsuManiaGame
import com.leaf.osumania.engine.GameConstants

class SongSelectScreen(private val game: OsuManiaGame) : Screen {
    private lateinit var batch: SpriteBatch
    private lateinit var shapeRenderer: ShapeRenderer
    private lateinit var stage: Stage
    private lateinit var skin: Skin
    private var time = 0f
    private val beatmaps = mutableListOf<BeatmapData>()
    private var selectedBeatmap: BeatmapData? = null

    private fun makePanelDrawable(): Drawable {
        val pix = Pixmap(1, 1, Pixmap.Format.RGBA8888).apply {
            setColor(OsuColors.PANEL.r, OsuColors.PANEL.g, OsuColors.PANEL.b, 1f)
            fill()
        }
        val tex = com.badlogic.gdx.graphics.Texture(pix)
        pix.dispose()
        return TextureRegionDrawable(TextureRegion(tex))
    }

    override fun show() {
        batch = SpriteBatch()
        shapeRenderer = ShapeRenderer()
        stage = Stage(ScreenViewport())
        Gdx.input.inputProcessor = stage

        val panelDrawable = makePanelDrawable()

        skin = Skin()
        skin.add("default-font", OsuFonts.get(18))
        skin.add("default", Label.LabelStyle(OsuFonts.get(18), Color.WHITE))
        skin.add("small", Label.LabelStyle(OsuFonts.get(14), OsuColors.TEXT_SECONDARY))
        skin.add("title", Label.LabelStyle(OsuFonts.get(28), Color.WHITE))
        skin.add("button", TextButton.TextButtonStyle(
            panelDrawable, panelDrawable, panelDrawable,
            OsuFonts.get(18), Color.WHITE, Color.WHITE, Color.GRAY
        ))

        val root = Table()
        root.setFillParent(true)
        root.top()

        val header = Table()
        header.background = panelDrawable
        header.pad(8f)
        val titleLabel = Label("Song Select", skin, "title")
        header.add(titleLabel).left().expandX()

        val importBtn = TextButton("Import", skin, "button")
        importBtn.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                game.screen = BeatmapImportScreen(game)
            }
        })
        header.add(importBtn).width(100f).height(40f).right()
        root.add(header).fillX().row()

        val content = Table()
        content.pad(8f)

        val scrollPane = com.badlogic.gdx.scenes.scene2d.ui.ScrollPane(content, skin)
        scrollPane.setFadeScrollBars(false)
        scrollPane.setScrollingDisabled(true, false)

        val beatmapList = Table()
        if (beatmaps.isEmpty()) {
            val emptyLabel = Label("No beatmaps loaded.\nImport .osz files to get started.", skin, "small")
            beatmapList.add(emptyLabel).padTop(40f)
        } else {
            for (bm in beatmaps) {
                val card = createBeatmapCard(bm)
                beatmapList.add(card).fillX().padBottom(4f).row()
            }
        }
        content.add(beatmapList).fillX().expandX()

        root.add(scrollPane).expand().fill().row()

        stage.addActor(root)
    }

    private fun createBeatmapCard(beatmap: BeatmapData): Table {
        val card = Table()
        card.background = makePanelDrawable()
        card.pad(8f)

        val color = when (beatmap.difficulty.keyCount) {
            4 -> OsuColors.ACCENT_BLUE
            7 -> OsuColors.ACCENT_PURPLE
            else -> OsuColors.ACCENT_GREEN
        }

        val colorBar = Table()
        colorBar.color = color
        card.add(colorBar).width(4f).fillY().padRight(8f)

        val info = Table()
        val titleLabel = Label("${beatmap.metadata.artist} - ${beatmap.metadata.title}", skin)
        info.add(titleLabel).left().row()
        val versionLabel = Label(beatmap.metadata.version, skin, "small")
        info.add(versionLabel).left().row()

        val statsRow = Table()
        val keysLabel = Label("${beatmap.difficulty.keyCount}K", skin, "small")
        keysLabel.color = color
        statsRow.add(keysLabel).padRight(8f)
        val diffLabel = Label("OD ${beatmap.difficulty.od.toInt()} HP ${beatmap.difficulty.hp.toInt()}", skin, "small")
        statsRow.add(diffLabel)
        info.add(statsRow).left().padTop(4f).row()

        card.add(info).expandX().left()

        card.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                selectedBeatmap = beatmap
                game.screen = GameplayScreen(game, beatmap)
            }
        })

        return card
    }

    fun setBeatmaps(list: List<BeatmapData>) {
        beatmaps.clear()
        beatmaps.addAll(list)
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
        batch.dispose()
        shapeRenderer.dispose()
        stage.dispose()
        skin.dispose()
    }
}
