package com.leaf.osumania.ui

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator

object OsuFonts {
    private var generator: FreeTypeFontGenerator? = null
    private val fonts = mutableMapOf<Int, BitmapFont>()

    fun init() {
        generator = FreeTypeFontGenerator(Gdx.files.internal("fonts/VarelaRound.ttf"))
    }

    fun get(size: Int): BitmapFont {
        return fonts.getOrPut(size) {
            val params = FreeTypeFontGenerator.FreeTypeFontParameter()
            params.size = size
            params.minFilter = com.badlogic.gdx.graphics.Texture.TextureFilter.Linear
            params.magFilter = com.badlogic.gdx.graphics.Texture.TextureFilter.Linear
            generator?.generateFont(params) ?: BitmapFont()
        }
    }

    fun getMono(size: Int): BitmapFont {
        return fonts.getOrPut(size + 10000) {
            val gen = FreeTypeFontGenerator(Gdx.files.internal("fonts/RobotoMono.ttf"))
            val params = FreeTypeFontGenerator.FreeTypeFontParameter()
            params.size = size
            params.minFilter = com.badlogic.gdx.graphics.Texture.TextureFilter.Linear
            params.magFilter = com.badlogic.gdx.graphics.Texture.TextureFilter.Linear
            gen.generateFont(params)
        }
    }

    fun dispose() {
        fonts.values.forEach { it.dispose() }
        fonts.clear()
        generator?.dispose()
        generator = null
    }
}
