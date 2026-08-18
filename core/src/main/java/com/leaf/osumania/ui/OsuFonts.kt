package com.leaf.osumania.ui

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator

object OsuFonts {
    private var generator: FreeTypeFontGenerator? = null
    private val fonts = mutableMapOf<Int, BitmapFont>()

    fun init() {
        try {
            val handle = Gdx.files.internal("fonts/VarelaRound.ttf")
            if (handle.exists()) {
                generator = FreeTypeFontGenerator(handle)
            }
        } catch (_: Exception) {
        }
    }

    fun get(size: Int): BitmapFont {
        return fonts.getOrPut(size) {
            if (generator != null) {
                val params = FreeTypeFontGenerator.FreeTypeFontParameter()
                params.size = size
                params.minFilter = com.badlogic.gdx.graphics.Texture.TextureFilter.Linear
                params.magFilter = com.badlogic.gdx.graphics.Texture.TextureFilter.Linear
                generator!!.generateFont(params) ?: BitmapFont()
            } else {
                BitmapFont()
            }
        }
    }

    fun dispose() {
        fonts.values.forEach { it.dispose() }
        fonts.clear()
        generator?.dispose()
        generator = null
    }
}
