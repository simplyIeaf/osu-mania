package com.leaf.osumania.ui.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.backends.android.AndroidFragmentApplication
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.leaf.osumania.beatmap.BeatmapData
import com.leaf.osumania.engine.GameEngine
import com.leaf.osumania.engine.GameState
import com.leaf.osumania.objects.NoteRenderer
import com.leaf.osumania.storage.SettingsStore
import com.leaf.osumania.ui.GameplayStateHolder

class GameplayLibGdxFragment : AndroidFragmentApplication() {
    companion object {
        var stateHolder: GameplayStateHolder? = null
        var beatmapData: BeatmapData? = null
        var settingsStore: SettingsStore? = null
    }

    var engine: GameEngine? = null

    override fun createScreen() {
        val bm = beatmapData ?: return
        val st = settingsStore ?: return
        val sh = stateHolder ?: return

        val e = GameEngine()
        e.init(bm, st)
        engine = e
        sh.engine = e
        setScreen(InnerGameplayScreen(e, st, sh))
    }
}

class InnerGameplayScreen(
    private val engine: GameEngine,
    private val settings: SettingsStore,
    private val stateHolder: GameplayStateHolder
) : com.badlogic.gdx.Screen {
    private lateinit var batch: SpriteBatch
    private lateinit var shapeRenderer: ShapeRenderer
    private lateinit var noteRenderer: NoteRenderer
    private val font = BitmapFont()
    private val layout = GlyphLayout()
    private var lastDelta = 0f

    override fun show() {
        batch = SpriteBatch()
        shapeRenderer = ShapeRenderer()
        noteRenderer = NoteRenderer(engine, engine.playfield, settings)
    }

    override fun render(delta: Float) {
        lastDelta = delta

        if (!stateHolder.paused) {
            engine.update(delta)
        }

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        val w = Gdx.graphics.width.toFloat()
        val h = Gdx.graphics.height.toFloat()
        val viewportH = 480f
        val viewportW = viewportH * (w / h)

        shapeRenderer.projectionMatrix.setToOrtho2D(0f, 0f, viewportW, viewportH)
        batch.projectionMatrix.setToOrtho2D(0f, 0f, viewportW, viewportH)

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        noteRenderer.render(shapeRenderer, batch, font, layout)
        shapeRenderer.end()

        if (!stateHolder.paused) {
            handleInput()
        }

        stateHolder.updateFromEngine(engine)
        stateHolder.updateJudgementTimer(delta)

        if (engine.state == GameState.FINISH || engine.state == GameState.FAIL) {
            Gdx.app.postRunnable {
                stateHolder.onGameEnd?.invoke(engine)
            }
        }
    }

    private fun handleInput() {
        val cols = engine.keyCount
        val sw = Gdx.graphics.width.toFloat()
        val sh = Gdx.graphics.height.toFloat()
        val viewportH = 480f
        val viewportW = viewportH * (sw / sh)
        val scaleX = viewportW / sw

        for (i in 0 until cols) {
            val colX = engine.playfield.getColumnX(i)
            val colW = engine.playfield.columnWidth
            val inputX = Gdx.input.x * scaleX
            val isTouched = Gdx.input.isTouched && inputX >= colX && inputX <= colX + colW

            if (isTouched && !engine.inputSystem.isPressed(i)) {
                engine.hit(i)
                noteRenderer.setColumnPressed(i, true)
            } else if (!isTouched && engine.inputSystem.isPressed(i)) {
                engine.release(i)
                noteRenderer.setColumnPressed(i, false)
            }
        }

        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ESCAPE)) {
            if (!stateHolder.paused) {
                engine.pause()
                stateHolder.paused = true
            }
        }
    }

    override fun resize(width: Int, height: Int) {}
    override fun pause() {}
    override fun resume() {}

    override fun hide() {
        engine.quit()
        dispose()
    }

    override fun dispose() {
        try { batch.dispose() } catch (_: Exception) {}
        try { shapeRenderer.dispose() } catch (_: Exception) {}
        try { noteRenderer.dispose() } catch (_: Exception) {}
        try { font.dispose() } catch (_: Exception) {}
    }
}
