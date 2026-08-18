package com.leaf.osumania.ui.screens

import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentActivity
import com.leaf.osumania.beatmap.BeatmapData
import com.leaf.osumania.engine.GameConstants
import com.leaf.osumania.engine.GameState
import com.leaf.osumania.storage.SettingsStore
import com.leaf.osumania.ui.GameplayStateHolder
import com.leaf.osumania.ui.theme.AccentGreen
import com.leaf.osumania.ui.theme.AccentPink
import com.leaf.osumania.ui.theme.AccentRed
import com.leaf.osumania.ui.theme.AccentYellow
import com.leaf.osumania.ui.theme.Background
import com.leaf.osumania.ui.theme.DarkOverlay
import com.leaf.osumania.ui.theme.Panel
import com.leaf.osumania.ui.theme.TextDim
import com.leaf.osumania.ui.theme.TextSecondary

@Composable
fun GameplayScreen(
    beatmap: BeatmapData,
    settings: SettingsStore,
    onGameEnd: (com.leaf.osumania.engine.GameEngine) -> Unit,
    onRetry: () -> Unit,
    onQuit: () -> Unit
) {
    val stateHolder = remember { GameplayStateHolder() }
    val context = LocalContext.current

    DisposableEffect(Unit) {
        stateHolder.onGameEnd = onGameEnd
        stateHolder.onRetry = onRetry
        stateHolder.onQuit = {
            (context as? FragmentActivity)?.supportFragmentManager
                ?.findFragmentByTag("gameplay_frag")
                ?.let { frag ->
                    (frag as? GameplayLibGdxFragment)?.let {
                        it.quitEngine()
                    }
                }
            onQuit()
        }
        GameplayLibGdxFragment.stateHolder = stateHolder
        GameplayLibGdxFragment.beatmapData = beatmap
        GameplayLibGdxFragment.settingsStore = settings
        onDispose {
            stateHolder.engine?.quit()
        }
    }

    BackHandler {
        if (stateHolder.paused) {
            stateHolder.engine?.unpause()
            stateHolder.paused = false
        } else {
            stateHolder.engine?.pause()
            stateHolder.paused = true
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            factory = { ctx ->
                val frameLayout = FrameLayout(ctx).apply {
                    id = android.view.View.generateViewId()
                }
                val activity = ctx as FragmentActivity
                val fragment = GameplayLibGdxFragment()
                activity.supportFragmentManager.beginTransaction()
                    .add(frameLayout.id, fragment, "gameplay_frag")
                    .commit()
                frameLayout
            },
            modifier = Modifier.fillMaxSize()
        )

        GameplayHUD(stateHolder, settings, onRetry, onQuit)
    }
}

@Composable
private fun GameplayHUD(
    state: GameplayStateHolder,
    settings: SettingsStore,
    onRetry: () -> Unit,
    onQuit: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        if (settings.showScore) {
            Text(
                text = "%08d".format(state.score),
                modifier = Modifier.align(Alignment.TopEnd),
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (settings.showAccuracy) {
            Text(
                text = "%.2f%%".format(state.accuracy),
                modifier = Modifier.align(Alignment.TopEnd).padding(top = 28.dp),
                color = Color(0xFFCCCCCC),
                fontSize = 16.sp
            )
        }

        if (settings.showCombo) {
            Text(
                text = "${state.combo}x",
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 40.dp),
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (settings.showHealthBar) {
            val healthColor = when {
                state.health > 0.5f -> AccentGreen
                state.health > 0.25f -> AccentYellow
                else -> AccentRed
            }
            LinearProgressIndicator(
                progress = { state.health.coerceIn(0f, 1f) },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 60.dp)
                    .width(200.dp)
                    .height(6.dp),
                color = healthColor,
                trackColor = Color(0xFF333333),
                strokeCap = StrokeCap.Round
            )
        }

        if (settings.showJudgement && state.judgementTimer < 0.8f && state.judgement >= 0) {
            val name = when (state.judgement) {
                320 -> "320"
                300 -> "300"
                200 -> "200"
                100 -> "100"
                50 -> "50"
                0 -> "Miss"
                else -> ""
            }
            val jColor = GameConstants.JUDGEMENT_COLORS[state.judgement]?.let { Color(it.r, it.g, it.b, it.a) } ?: Color.White
            val alpha = if (state.judgementTimer < 0.48f) 1f else {
                1f - ((state.judgementTimer - 0.48f) / 0.32f).coerceIn(0f, 1f)
            }
            val scale = if (state.judgementTimer < 0.24f) {
                1.2f - (state.judgementTimer / 0.24f) * 0.2f
            } else 1.0f

            val displayText = if (state.judgementEarlyLate.isNotEmpty()) "$name ${state.judgementEarlyLate}" else name
            Text(
                text = displayText,
                modifier = Modifier
                    .align(Alignment.Center)
                    .graphicsLayer(alpha = alpha, scaleX = scale, scaleY = scale),
                color = jColor,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (state.paused) {
            Box(
                modifier = Modifier.fillMaxSize().background(DarkOverlay),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(text = "Paused", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                    PauseButton("Continue") {
                        state.engine?.unpause()
                        state.paused = false
                    }
                    PauseButton("Retry", onRetry)
                    PauseButton("Quit", onQuit, AccentRed)
                }
            }
        }
    }
}

@Composable
private fun PauseButton(text: String, onClick: () -> Unit, color: Color = TextSecondary) {
    Button(
        onClick = onClick,
        modifier = Modifier.width(200.dp).height(48.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Panel)
    ) {
        Text(text = text, color = color, fontSize = 16.sp)
    }
}
