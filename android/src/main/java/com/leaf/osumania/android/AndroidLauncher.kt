package com.leaf.osumania.android

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import com.badlogic.gdx.backends.android.AndroidApplication
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
import com.leaf.osumania.ui.OsuManiaGame

class AndroidLauncher : AndroidApplication() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_FULLSCREEN
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        )
        val config = AndroidApplicationConfiguration().apply {
            useImmersiveMode = true
            useAccelerometer = false
            useCompass = false
            numSamples = 0
        }
        val game = OsuManiaGame()
        game.externalFileLoader = { uri -> loadExternalFile(uri) }
        initialize(game, config)
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_VIEW) {
            intent.data?.let { uri ->
                contentResolver.openInputStream(uri)?.use { stream ->
                    val bytes = stream.readBytes()
                    (applicationListener as? OsuManiaGame)?.pendingOszImport = bytes
                }
            }
        }
    }

    private fun loadExternalFile(uri: String): ByteArray? {
        return try {
            val androidUri = android.net.Uri.parse(uri)
            contentResolver.openInputStream(androidUri)?.use { it.readBytes() }
        } catch (e: Exception) {
            null
        }
    }
}
