package com.leaf.osumania.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.leaf.osumania.engine.GameEngine
import com.leaf.osumania.engine.GameState

class GameplayStateHolder {
    var score by mutableIntStateOf(0)
    var combo by mutableIntStateOf(0)
    var maxCombo by mutableIntStateOf(0)
    var accuracy by mutableFloatStateOf(100f)
    var health by mutableFloatStateOf(1f)
    var judgement by mutableIntStateOf(-1)
    var judgementTimer by mutableFloatStateOf(0f)
    var judgementEarlyLate by mutableStateOf("")
    var progress by mutableFloatStateOf(0f)
    var gameState by mutableStateOf(GameState.LOADING)
    var judgementCounts by mutableStateOf(mapOf<Int, Int>())
    var paused by mutableStateOf(false)

    var onGameEnd: ((GameEngine) -> Unit)? = null
    var onRetry: (() -> Unit)? = null
    var onQuit: (() -> Unit)? = null

    var engine: GameEngine? = null

    private val judgementDuration = 0.8f

    fun showJudgement(j: Int, earlyLate: String?) {
        judgement = j
        judgementTimer = 0f
        judgementEarlyLate = earlyLate ?: ""
    }

    fun updateJudgementTimer(delta: Float) {
        if (judgementTimer < judgementDuration) {
            judgementTimer += delta
        }
    }

    fun updateFromEngine(e: GameEngine) {
        score = e.score
        combo = e.combo
        maxCombo = e.maxCombo
        accuracy = e.accuracy
        health = e.health
        progress = e.progress
        gameState = e.state
        judgementCounts = e.judgementCounts
    }
}
