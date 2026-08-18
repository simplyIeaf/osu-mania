package com.leaf.osumania.ui

import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Preferences
import com.leaf.osumania.storage.SettingsStore

class OsuManiaGame : Game() {
    lateinit var settings: SettingsStore
    var pendingOszImport: ByteArray? = null
    var externalFileLoader: ((String) -> ByteArray?)? = null

    override fun create() {
        OsuFonts.init()
        settings = SettingsStore()
        settings.load()
        setScreen(LoadingScreen(this))
    }

    override fun dispose() {
        OsuFonts.dispose()
        screen?.dispose()
        super.dispose()
    }

    fun getPreferences(): Preferences = Gdx.app.getPreferences("OsuManiaSettings")
}
