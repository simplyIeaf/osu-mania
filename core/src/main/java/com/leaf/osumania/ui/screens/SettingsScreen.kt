package com.leaf.osumania.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leaf.osumania.engine.GameConstants
import com.leaf.osumania.storage.SettingsStore
import com.leaf.osumania.ui.theme.AccentGreen
import com.leaf.osumania.ui.theme.AccentPink
import com.leaf.osumania.ui.theme.Background
import com.leaf.osumania.ui.theme.Border
import com.leaf.osumania.ui.theme.Panel
import com.leaf.osumania.ui.theme.TextSecondary

@Composable
fun SettingsScreen(
    settings: SettingsStore,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(containerColor = Panel)
            ) {
                Text("< Back")
            }
            Text(
                text = "Settings",
                modifier = Modifier.weight(1f).padding(start = 16.dp),
                color = AccentPink,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            SectionHeader("Gameplay")
            SliderSetting("Scroll Speed", 1f, 40f, settings.scrollSpeed) { settings.scrollSpeed = it }
            SliderSetting("Hit Position", 0f, 200f, settings.hitPositionOffset) { settings.hitPositionOffset = it }
            SliderSetting("Background Dim", 0f, 1f, settings.backgroundDim) { settings.backgroundDim = it }
            ToggleSetting("Upscroll", settings.upscroll) { settings.upscroll = it }
            ToggleSetting("Performance Mode", settings.performanceMode) { settings.performanceMode = it }

            SectionHeader("Display")
            SliderSetting("Playfield Size", 0.3f, 1.0f, settings.stageWidth) { settings.stageWidth = it }
            SliderSetting("Playfield X Position", -0.5f, 0.5f, settings.stageXOffset) { settings.stageXOffset = it }
            SliderSetting("Stage Opacity", 0f, 1f, settings.stageOpacity) { settings.stageOpacity = it }
            SliderSetting("Receptor Opacity", 0f, 1f, settings.receptorOpacity) { settings.receptorOpacity = it }
            SliderSetting("Note Scale", 0.5f, 1f, settings.noteScale) { settings.noteScale = it }
            SliderSetting("Note Offset", -100f, 100f, settings.noteOffset) { settings.noteOffset = it }

            SectionHeader("Skin")
            SliderSetting("Hue", 0f, 360f, settings.hue) { settings.hue = it }
            ToggleSetting("Darker Hold Notes", settings.darkerHoldNotes) { settings.darkerHoldNotes = it }

            SectionHeader("Note Style")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (style in GameConstants.SkinStyle.entries) {
                    val isSelected = style == settings.skinStyle
                    Box(
                        modifier = Modifier
                            .background(if (isSelected) AccentPink else Panel, RoundedCornerShape(4.dp))
                            .clickable { settings.skinStyle = style }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(style.name, color = if (isSelected) Background else TextSecondary, fontSize = 12.sp)
                    }
                }
            }

            SectionHeader("Volume")
            SliderSetting("Music Volume", 0f, 1f, settings.musicVolume) { settings.musicVolume = it }
            SliderSetting("SFX Volume", 0f, 1f, settings.sfxVolume) { settings.sfxVolume = it }
            SliderSetting("Audio Offset", -300f, 300f, settings.audioOffset) { settings.audioOffset = it }

            SectionHeader("HUD")
            ToggleSetting("Show Score", settings.showScore) { settings.showScore = it }
            ToggleSetting("Show Combo", settings.showCombo) { settings.showCombo = it }
            ToggleSetting("Show Accuracy", settings.showAccuracy) { settings.showAccuracy = it }
            ToggleSetting("Show Health Bar", settings.showHealthBar) { settings.showHealthBar = it }
            ToggleSetting("Show Error Bar", settings.showErrorBar) { settings.showErrorBar = it }
            ToggleSetting("Show FPS", settings.showFps) { settings.showFps = it }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
        color = AccentPink,
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun SliderSetting(name: String, min: Float, max: Float, current: Float, onChange: (Float) -> Unit) {
    var value by remember { mutableFloatStateOf(current) }
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            modifier = Modifier.width(140.dp),
            color = TextSecondary,
            fontSize = 14.sp
        )
        Slider(
            value = value,
            onValueChange = { value = it },
            onValueChangeFinished = { onChange(value) },
            valueRange = min..max,
            modifier = Modifier.weight(1f),
            colors = SliderDefaults.colors(
                thumbColor = AccentPink,
                activeTrackColor = AccentPink,
                inactiveTrackColor = Border
            )
        )
        Text(
            text = "%.1f".format(value),
            modifier = Modifier.width(48.dp),
            color = TextSecondary,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun ToggleSetting(name: String, current: Boolean, onChange: (Boolean) -> Unit) {
    var enabled by remember { mutableStateOf(current) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable {
                enabled = !enabled
                onChange(enabled)
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            modifier = Modifier.weight(1f),
            color = TextSecondary,
            fontSize = 14.sp
        )
        Box(
            modifier = Modifier
                .background(if (enabled) AccentGreen else Panel, RoundedCornerShape(4.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = if (enabled) "ON" else "OFF",
                color = if (enabled) Background else TextSecondary,
                fontSize = 12.sp
            )
        }
    }
}
