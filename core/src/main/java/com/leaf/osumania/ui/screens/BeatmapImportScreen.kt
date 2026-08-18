package com.leaf.osumania.ui.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.utils.viewport.FitViewport
import com.leaf.osumania.OsuManiaGame
import com.leaf.osumania.storage.BeatmapManager
import com.leaf.osumania.api.ApiBeatmapSet

class BeatmapImportScreen(
    private val game: OsuManiaGame,
    private val skin: Skin,
    private val beatmapManager: BeatmapManager,
    private val onDone: () -> Unit
) : ScreenAdapter() {

    private lateinit var stage: Stage
    private val importedBeatmaps = mutableListOf<ApiBeatmapSet>()

    override fun show() {
        stage = Stage(FitViewport(800f, 600f))
        Gdx.input.inputProcessor = stage

        val root = Table()
        root.setFillParent(true)
        root.pad(20f)

        val instructionLabel = Label(
            "Import .osz beatmap files from your device.\n" +
            "Tap the button below to open the file picker.",
            skin
        )
        instructionLabel.color = Color(0.7f, 0.7f, 0.7f, 1f)
        instructionLabel.setFontScale(0.8f)

        val importButton = TextButton("Import from Files", skin)
        importButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent, x: Float, y: Float) {
                triggerFileImport()
            }
        })

        val doneButton = TextButton("Done", skin)
        doneButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent, x: Float, y: Float) {
                onDone()
            }
        })

        val listLabel = Label("Imported Beatmaps:", skin)
        listLabel.color = Color.WHITE
        listLabel.setFontScale(0.75f)

        root.add(instructionLabel).colspan(2).padBottom(15f).row()
        root.add(importButton).width(200f).height(50f).padBottom(10f).row()
        root.add(listLabel).left().padBottom(8f).row()

        val listTable = Table()
        listTable.defaults().left().padBottom(4f)

        val importedInfo = Label("${importedBeatmaps.size} beatmap(s) imported", skin)
        importedInfo.color = Color(0.4f, 0.9f, 0.5f, 1f)
        importedInfo.setFontScale(0.7f)
        listTable.add(importedInfo).row()

        for (set in importedBeatmaps) {
            val name = "${set.artist} - ${set.title}"
            val entry = Label(name, skin)
            entry.color = Color(0.8f, 0.8f, 0.8f, 1f)
            entry.setFontScale(0.65f)
            listTable.add(entry).row()
        }

        root.add(listTable).colspan(2).expandX().fillX().padBottom(10f).row()
        root.add(doneButton).width(150f).height(45f)

        stage.addActor(root)
    }

    private fun triggerFileImport() {
        try {
            val intent = android.content.Intent(android.content.Intent.ACTION_GET_CONTENT).apply {
                type = "*/*"
                addCategory(android.content.Intent.CATEGORY_OPENABLE)
                putExtra(android.content.Intent.EXTRA_ALLOW_MULTIPLE, true)
            }
            val chooserIntent = android.content.Intent.createChooser(intent, "Select .osz files")
            Gdx.app.postRunnable {
                val activity = com.badlogic.gdx.Gdx.app as? com.badlogic.gdx.backends.android.AndroidApplication
                activity?.startActivityForResult(chooserIntent, 1001)
            }
        } catch (e: Exception) {
            Gdx.app.error("BeatmapImport", "Failed to open file picker", e)
        }
    }

    fun onFilesSelected(uris: List<String>) {
        for (uri in uris) {
            try {
                val inputStream = Gdx.app.context.contentResolver.openInputStream(android.net.Uri.parse(uri)) ?: continue
                val bytes = inputStream.readBytes()
                inputStream.close()
                val beatmap = beatmapManager.importOszFile(bytes)
                if (beatmap != null) {
                    val setInfo = ApiBeatmapSet(
                        title = beatmap.metadata.title,
                        artist = beatmap.metadata.artist,
                        creator = beatmap.metadata.creator
                    )
                    importedBeatmaps.add(setInfo)
                }
            } catch (e: Exception) {
                Gdx.app.error("BeatmapImport", "Failed to import file: $uri", e)
            }
        }
        refreshList()
    }

    private fun refreshList() {
        stage.clear()
        show()
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

    override fun dispose() {
        stage.dispose()
    }
}
