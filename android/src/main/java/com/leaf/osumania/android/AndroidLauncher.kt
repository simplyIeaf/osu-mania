package com.leaf.osumania.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.leaf.osumania.storage.SettingsStore
import com.leaf.osumania.ui.AppNavigation
import com.leaf.osumania.ui.theme.OsuManiaTheme

class AndroidLauncher : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val settings = SettingsStore()
        settings.load()
        setContent {
            OsuManiaTheme {
                AppNavigation(
                    settings = settings,
                    onImportFile = { }
                )
            }
        }
    }
}
