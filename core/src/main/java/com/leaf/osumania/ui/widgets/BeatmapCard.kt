package com.leaf.osumania.ui.widgets

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.leaf.osumania.api.ApiBeatmapSet

class BeatmapCard(skin: Skin, beatmapSet: ApiBeatmapSet) : Table() {

    companion object {
        private val PANEL_COLOR = Color(0.1f, 0.1f, 0.18f, 1f)
        private val ACCENT_COLOR = Color(1f, 0.4f, 0.6f, 1f)
        private val TEXT_WHITE = Color.WHITE
        private val TEXT_GRAY = Color(0.65f, 0.65f, 0.65f, 1f)
        private val RANKED_COLOR = Color(0.2f, 0.8f, 0.3f, 1f)
        private val LOVED_COLOR = Color(1f, 0.45f, 0.7f, 1f)
        private val GHOSTED_COLOR = Color(0.5f, 0.5f, 0.5f, 1f)
    }

    var onSelect: ((ApiBeatmapSet) -> Unit)? = null
    private var isHovered = false

    init {
        background = skin.newDrawable("white", PANEL_COLOR)
        pad(8f)

        val coverColor = when {
            beatmapSet.status == "ranked" -> Color(0.3f, 0.6f, 1f, 1f)
            beatmapSet.status == "loved" -> LOVED_COLOR
            else -> Color(0.5f, 0.5f, 0.5f, 1f)
        }
        val coverRect = Table()
        coverRect.background = skin.newDrawable("white", coverColor)
        coverRect.width = 80f
        coverRect.height = 80f

        val infoTable = Table()

        val titleLabel = Label(beatmapSet.title, skin)
        titleLabel.color = TEXT_WHITE
        titleLabel.setFontScale(0.9f)

        val artistLabel = Label(beatmapSet.artist, skin)
        artistLabel.color = TEXT_GRAY
        artistLabel.setFontScale(0.7f)

        val stars = buildString {
            val starCount = (beatmapSet.beatmaps.size.coerceAtMost(5))
            repeat(starCount) { append("*") }
        }
        val starLabel = Label(stars, skin)
        starLabel.color = ACCENT_COLOR
        starLabel.setFontScale(0.6f)

        val keyCount = beatmapSet.beatmaps.firstOrNull()?.let { bm ->
            when {
                bm.cs >= 7 -> "7K"
                bm.cs >= 6 -> "6K"
                bm.cs >= 5 -> "5K"
                bm.cs >= 4 -> "4K"
                else -> "4K"
            }
        } ?: "4K"

        val keyLabel = Label(keyCount, skin)
        keyLabel.color = ACCENT_COLOR
        keyLabel.setFontScale(0.55f)

        val statusColor = when (beatmapSet.status) {
            "ranked" -> RANKED_COLOR
            "loved" -> LOVED_COLOR
            "graveyard", "WIP" -> GHOSTED_COLOR
            else -> TEXT_GRAY
        }
        val statusLabel = Label(beatmapSet.status.uppercase(), skin)
        statusLabel.color = statusColor
        statusLabel.setFontScale(0.5f)

        val badgeTable = Table()
        badgeTable.add(keyLabel).padRight(6f)
        badgeTable.add(statusLabel)

        infoTable.add(titleLabel).left().row()
        infoTable.add(artistLabel).left().row()
        infoTable.add(starLabel).left().padTop(2f).row()
        infoTable.add(badgeTable).left().padTop(2f)

        add(coverRect).width(80f).height(80f).padRight(10f)
        add(infoTable).expandX().left().fillY()

        val card = this
        addListener(object : ClickListener() {
            override fun enter(event: InputEvent, x: Float, y: Float, pointer: Int, fromActor: com.badlogic.gdx.scenes.scene2d.Actor?) {
                isHovered = true
                card.background = skin.newDrawable("white", Color(0.15f, 0.15f, 0.25f, 1f))
            }

            override fun exit(event: InputEvent, x: Float, y: Float, pointer: Int, toActor: com.badlogic.gdx.scenes.scene2d.Actor?) {
                isHovered = false
                card.background = skin.newDrawable("white", PANEL_COLOR)
            }

            override fun clicked(event: InputEvent, x: Float, y: Float) {
                onSelect?.invoke(beatmapSet)
            }
        })
    }

    fun updateBorderStyle(skin: Skin) {
        if (isHovered) {
            background = skin.newDrawable("white", Color(1f, 0.4f, 0.6f, 0.3f))
        } else {
            background = skin.newDrawable("white", PANEL_COLOR)
        }
    }
}
