package com.leaf.osumania.ui.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.badlogic.gdx.ApplicationListener
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

class GameplayLibGdxFragment : AndroidFragmentApplication(), ApplicationListener {
    companion object {
        var stateHolder: GameplayStateHolder? = null
        var beatmapData: BeatmapData? = null
        var settingsStore: SettingsStore? = null
    }

    var engine: GameEngine? = null
    private var batch: SpriteBatch? = null
    private var shapeRenderer: ShapeRenderer? = null
    private var noteRenderer: NoteRenderer? = null
    private val font = BitmapFont()
    private val layout = GlyphLayout()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return initializeForView(this)
    }

    override fun create() {
        val bm = beatmapData ?: return
        val st = settingsStore ?: return
        val sh = stateHolder ?: return

        val e = GameEngine()
        e.init(bm, st)
        engine = e
        sh.engine = e

        batch = SpriteBatch()
        shapeRenderer = ShapeRenderer()
        noteRenderer = NoteRenderer(e, e.playfield, st)
    }

    override fun render() {
        val e = engine ?: return
        val sh = stateHolder ?: return
        val nr = noteRenderer ?: return
        val b = batch ?: return
        val sr = shapeRenderer ?: return

        if (!sh.paused) {
            e.update(Gdx.graphics.deltaTime)
        }

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        val w = Gdx.graphics.width.toFloat()
        val h = Gdx.graphics.height.toFloat()
        val viewportH = 480f
        val viewportW = viewportH * (w / h)

        sr.projectionMatrix.setToOrtho2D(0f, 0f, viewportW, viewportH)
        b.projectionMatrix.setToOrtho2D(0f, 0f, viewportW, viewportH)

        sr.begin(ShapeRenderer.ShapeType.Filled)
        nr.render(sr, b, font, layout)
        sr.end()

        if (!sh.paused) {
            handleInput()
        }

        sh.updateFromEngine(e)
        sh.updateJudgementTimer(Gdx.graphics.deltaTime)

        if (e.state == GameState.FINISH || e.state == GameState.FAIL) {
            Gdx.app.postRunnable {
                sh.onGameEnd?.invoke(e)
            }
        }
    }

    private fun handleInput() {
        val e = engine ?: return
        val cols = e.keyCount
        val sw = Gdx.graphics.width.toFloat()
        val sh = Gdx.graphics.height.toFloat()
        val viewportH = 480f
        val viewportW = viewportH * (sw / sh)
        val scaleX = viewportW / sw

        for (i in 0 until cols) {
            val colX = e.playfield.getColumnX(i)
            val colW = e.playfield.columnWidth
            val inputX = Gdx.input.x * scaleX
            val isTouched = Gdx.input.isTouched && inputX >= colX && inputX <= colX + colW

            if (isTouched && !e.inputSystem.isPressed(i)) {
                e.hit(i)
                noteRenderer?.setColumnPressed(i, true)
            } else if (!isTouched && e.inputSystem.isPressed(i)) {
                e.release(i)
                noteRenderer?.setColumnPressed(i, false)
            }
        }

        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ESCAPE)) {
            val sh = stateHolder
            if (sh != null && !sh.paused) {
                e.pause()
                sh.paused = true
            }
        }
    }

    override fun resize(width: Int, height: Int) {}
    override fun pause() {}
    override fun resume() {}
    override fun dispose() {
        try { batch?.dispose() } catch (_: Exception) {}
        try { shapeRenderer?.dispose() } catch (_: Exception) {}
        try { noteRenderer?.dispose() } catch (_: Exception) {}
        try { font.dispose() } catch (_: Exception) {}
    }

    fun pauseEngine() {
        engine?.pause()
        stateHolder?.paused = true
    }

    fun resumeEngine() {
        engine?.unpause()
        stateHolder?.paused = false
    }

    fun quitEngine() {
        engine?.quit()
    }
}
