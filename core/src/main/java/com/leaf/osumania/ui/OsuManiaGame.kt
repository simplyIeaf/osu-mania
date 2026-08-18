package com.leaf.osumania.ui

import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Preferences
import com.leaf.osumania.storage.SettingsStore
import com.leaf.osumania.ui.screens.LoadingScreen

class OsuManiaGame : Game() {
    lateinit var settings: SettingsStore
    var pendingOszImport: ByteArray? = null
    var externalFileLoader: ((String) -> ByteArray?)? = null

    override fun create() {
        try {
            OsuFonts.init()
        } catch (e: Exception) {
            Gdx.app.error("OsuMania", "OsuFonts.init failed", e)
        }
        settings = SettingsStore()
        try {
            settings.load()
        } catch (e: Exception) {
            Gdx.app.error("OsuMania", "SettingsStore.load failed", e)
        }
        setScreen(LoadingScreen(this))
    }

    override fun dispose() {
        try { OsuFonts.dispose() } catch (_: Exception) {}
        try { screen?.dispose() } catch (_: Exception) {}
        super.dispose()
    }

    fun getPreferences(): Preferences = Gdx.app.getPreferences("OsuManiaSettings")
}
