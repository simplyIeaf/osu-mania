package com.leaf.osumania.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.leaf.osumania.beatmap.BeatmapData
import com.leaf.osumania.storage.SettingsStore
import com.leaf.osumania.ui.screens.BeatmapImportScreen
import com.leaf.osumania.ui.screens.GameplayScreen
import com.leaf.osumania.ui.screens.LoadingScreen
import com.leaf.osumania.ui.screens.MainMenuScreen
import com.leaf.osumania.ui.screens.ResultsScreen
import com.leaf.osumania.ui.screens.SettingsScreen
import com.leaf.osumania.ui.screens.SongSelectScreen

sealed class Screen {
    data object Loading : Screen()
    data object MainMenu : Screen()
    data object SongSelect : Screen()
    data object Settings : Screen()
    data object BeatmapImport : Screen()
    data class Gameplay(val beatmap: BeatmapData) : Screen()
    data class Results(val engine: com.leaf.osumania.engine.GameEngine) : Screen()
}

@Composable
fun AppNavigation(
    settings: SettingsStore,
    onImportFile: () -> Unit
) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Loading) }
    var beatmaps by remember { mutableStateOf(listOf<BeatmapData>()) }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        when (val s = currentScreen) {
            is Screen.Loading -> LoadingScreen(
                onFinished = { currentScreen = Screen.MainMenu }
            )
            is Screen.MainMenu -> MainMenuScreen(
                onPlay = { currentScreen = Screen.SongSelect },
                onSettings = { currentScreen = Screen.Settings },
                onImport = { onImportFile() }
            )
            is Screen.SongSelect -> SongSelectScreen(
                beatmaps = beatmaps,
                onSelectBeatmap = { currentScreen = Screen.Gameplay(it) },
                onImport = { currentScreen = Screen.BeatmapImport },
                onBack = { currentScreen = Screen.MainMenu }
            )
            is Screen.Settings -> SettingsScreen(
                settings = settings,
                onBack = {
                    settings.save()
                    currentScreen = Screen.MainMenu
                }
            )
            is Screen.BeatmapImport -> BeatmapImportScreen(
                onBack = { currentScreen = Screen.SongSelect }
            )
            is Screen.Gameplay -> GameplayScreen(
                beatmap = s.beatmap,
                settings = settings,
                onGameEnd = { engine -> currentScreen = Screen.Results(engine) },
                onRetry = { currentScreen = Screen.Gameplay(s.beatmap) },
                onQuit = { currentScreen = Screen.SongSelect }
            )
            is Screen.Results -> ResultsScreen(
                engine = s.engine,
                onBack = { currentScreen = Screen.SongSelect },
                onRetry = { currentScreen = Screen.Gameplay(s.engine.beatmapData) }
            )
        }
    }
}
