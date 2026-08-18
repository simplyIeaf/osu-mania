package com.leaf.osumania.ui.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.leaf.osumania.beatmap.BeatmapData
import com.leaf.osumania.engine.GameEngine
import com.leaf.osumania.engine.GameState
import com.leaf.osumania.engine.Playfield
import com.leaf.osumania.objects.NoteRenderer
import com.leaf.osumania.hud.HudRenderer
import com.leaf.osumania.systems.InputSystem
import com.leaf.osumania.systems.AudioSystem
import com.leaf.osumania.systems.ScoreSystem
import com.leaf.osumania.systems.HealthSystem
import com.leaf.osumania.systems.ReplayRecorder
import com.leaf.osumania.mods.ModManager
import com.leaf.osumania.ui.OsuColors
import com.leaf.osumania.ui.OsuFonts
import com.leaf.osumania.ui.OsuManiaGame

class GameplayScreen(private val game: OsuManiaGame, private val beatmap: BeatmapData) : Screen {
    private lateinit var batch: SpriteBatch
    private lateinit var shapeRenderer: ShapeRenderer
    private lateinit var engine: GameEngine
    private lateinit var playfield: Playfield
    private lateinit var noteRenderer: NoteRenderer
    private lateinit var hudRenderer: HudRenderer
    private var paused = false
    private var pauseStage: Stage? = null
    private var pauseSkin: Skin? = null

    override fun show() {
        batch = SpriteBatch()
        shapeRenderer = ShapeRenderer()

        val sw = Gdx.graphics.width.toFloat()
        val sh = Gdx.graphics.height.toFloat()

        val settings = game.settings
        playfield = Playfield().apply {
            screenWidth = sw
            screenHeight = sh
            keyCount = beatmap.difficulty.keyCount
            hitPositionOffset = settings.hitPositionOffset
            stagePosition = settings.stagePosition
            noteOffset = settings.noteOffset
            noteScale = settings.noteScale
            laneWidthAdjustment = settings.laneWidthAdjustment
            laneSpacing = settings.laneSpacing
            upscroll = settings.upscroll
            skinStyle = settings.skinStyle
            recalculate()
        }

        engine = GameEngine()
        engine.init(beatmap, settings)

        noteRenderer = NoteRenderer(engine, playfield)
        hudRenderer = HudRenderer()
    }

    override fun render(delta: Float) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        if (paused) {
            renderPauseOverlay()
            return
        }

        engine.update(delta * 1000f)

        val w = Gdx.graphics.width.toFloat()
        val h = Gdx.graphics.height.toFloat()
        val viewportH = 480f
        val viewportW = viewportH * (w / h)

        shapeRenderer.projectionMatrix.setToOrtho2D(0f, 0f, viewportW, viewportH)
        batch.projectionMatrix.setToOrtho2D(0f, 0f, viewportW, viewportH)

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        noteRenderer.render(shapeRenderer, batch, OsuFonts.get(15), GlyphLayout())
        shapeRenderer.end()

        batch.begin()
        hudRenderer.render(shapeRenderer, batch, viewportW, viewportH)
        batch.end()

        handleInput()

        if (engine.state == GameState.FINISH || engine.state == GameState.FAIL) {
            game.screen = ResultsScreen(game, engine)
            return
        }
    }

    private fun handleInput() {
        val cols = engine.keyCount
        val sw = Gdx.graphics.width.toFloat()
        val sh = Gdx.graphics.height.toFloat()
        val viewportH = 480f
        val viewportW = viewportH * (sw / sh)
        val scaleX = viewportW / sw
        val scaleY = viewportH / sh

        for (i in 0 until cols) {
            val colX = playfield.getColumnX(i)
            val colW = playfield.columnWidth
            val inputX = Gdx.input.x * scaleX

            val isTouched = Gdx.input.isTouched &&
                inputX >= colX && inputX <= colX + colW

            if (isTouched && !engine.inputSystem.isPressed(i)) {
                engine.hit(i)
                noteRenderer.setColumnPressed(i, true)
            } else if (!isTouched && engine.inputSystem.isPressed(i)) {
                engine.release(i)
                noteRenderer.setColumnPressed(i, false)
            }
        }

        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ESCAPE)) {
            togglePause()
        }
    }

    private fun togglePause() {
        paused = !paused
        if (paused) {
            engine.pause()
            showPauseOverlay()
        } else {
            engine.unpause()
            pauseStage?.dispose()
            pauseStage = null
        }
    }

    private fun showPauseOverlay() {
        val screen = this
        pauseStage = Stage(ScreenViewport())
        pauseSkin = Skin()
        pauseSkin!!.add("default", Label.LabelStyle(OsuFonts.get(20), Color.WHITE))
        pauseSkin!!.add("title", Label.LabelStyle(OsuFonts.get(30), Color.WHITE))

        val root = Table()
        root.setFillParent(true)
        root.center()

        val overlay = Table()
        overlay.setColor(0f, 0f, 0f, 0.85f)

        val titleLabel = Label("Paused", pauseSkin, "title")
        overlay.add(titleLabel).padBottom(30f).row()

        val resumeBtn = TextButton("Continue", pauseSkin, "button")
        resumeBtn.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                togglePause()
            }
        })
        overlay.add(resumeBtn).width(200f).height(50f).padBottom(10f).row()

        val retryBtn = TextButton("Retry", pauseSkin, "button")
        retryBtn.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                pauseStage?.dispose()
                game.screen = GameplayScreen(game, beatmap)
            }
        })
        overlay.add(retryBtn).width(200f).height(50f).padBottom(10f).row()

        val quitBtn = TextButton("Quit", pauseSkin, "button")
        quitBtn.color = OsuColors.ACCENT_RED
        quitBtn.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                pauseStage?.dispose()
                game.screen = SongSelectScreen(game)
            }
        })
        overlay.add(quitBtn).width(200f).height(50f).row()

        root.add(overlay)
        pauseStage!!.addActor(root)
        Gdx.input.inputProcessor = pauseStage
    }

    private fun renderPauseOverlay() {
        Gdx.gl.glClearColor(OsuColors.BACKGROUND.r, OsuColors.BACKGROUND.g, OsuColors.BACKGROUND.b, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        pauseStage?.act()
        pauseStage?.draw()
    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
        pauseStage?.viewport?.update(width, height, true)
    }

    override fun pause() {}
    override fun resume() {}

    override fun hide() {
        engine.quit()
        dispose()
    }

    override fun dispose() {
        batch.dispose()
        shapeRenderer.dispose()
        noteRenderer.dispose()
        hudRenderer.dispose()
        pauseStage?.dispose()
        pauseSkin?.dispose()
    }
}
