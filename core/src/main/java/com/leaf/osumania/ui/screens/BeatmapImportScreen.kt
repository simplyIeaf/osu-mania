package com.leaf.osumania.ui.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.leaf.osumania.api.ApiBeatmapSet
import com.leaf.osumania.storage.BeatmapManager
import com.leaf.osumania.ui.OsuManiaGame

class BeatmapImportScreen(
    private val game: OsuManiaGame
) : Screen {
    private lateinit var stage: Stage
    private lateinit var skin: Skin

    override fun show() {
        stage = Stage(ScreenViewport())
        Gdx.input.inputProcessor = stage

        skin = Skin()
        skin.add("default", Label.LabelStyle(com.leaf.osumania.ui.OsuFonts.get(18), Color.WHITE))

        val root = Table()
        root.setFillParent(true)
        root.pad(20f)
        root.center()

        val instructionLabel = Label(
            "Import .osz beatmap files.\nPlace beatmaps in the game folder\nand restart to load them.",
            skin
        )
        instructionLabel.color = Color(0.7f, 0.7f, 0.7f, 1f)

        val backBtn = TextButton("Back", skin, "default")
        backBtn.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                game.screen = SongSelectScreen(game)
            }
        })

        root.add(instructionLabel).padBottom(20f).row()
        root.add(backBtn).width(150f).height(45f)

        stage.addActor(root)
    }

    override fun render(delta: Float) {
        Gdx.gl.glClearColor(0.05f, 0.05f, 0.1f, 1f)
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
        stage.dispose()
        skin.dispose()
    }
}
