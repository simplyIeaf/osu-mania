package com.leaf.osumania.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leaf.osumania.beatmap.BeatmapData
import com.leaf.osumania.ui.theme.AccentBlue
import com.leaf.osumania.ui.theme.AccentGreen
import com.leaf.osumania.ui.theme.AccentPurple
import com.leaf.osumania.ui.theme.Background
import com.leaf.osumania.ui.theme.Panel
import com.leaf.osumania.ui.theme.TextDim
import com.leaf.osumania.ui.theme.TextSecondary

@Composable
fun SongSelectScreen(
    beatmaps: List<BeatmapData>,
    onSelectBeatmap: (BeatmapData) -> Unit,
    onImport: () -> Unit,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().background(Background)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Panel)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(containerColor = Background)
            ) {
                Text("<")
            }
            Text(
                text = "Song Select",
                modifier = Modifier.weight(1f).padding(start = 12.dp),
                color = TextSecondary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Button(
                onClick = onImport,
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
            ) {
                Text("Import", color = Background)
            }
        }

        if (beatmaps.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "No beatmaps loaded.\nImport .osz files to get started.",
                    color = TextDim,
                    fontSize = 16.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(8.dp),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(4.dp)
            ) {
                items(beatmaps) { beatmap ->
                    SongSelectCard(beatmap) { onSelectBeatmap(beatmap) }
                }
            }
        }
    }
}

@Composable
private fun SongSelectCard(beatmap: BeatmapData, onClick: () -> Unit) {
    val keyColor = when (beatmap.difficulty.keyCount) {
        4 -> AccentBlue
        7 -> AccentPurple
        else -> AccentGreen
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Panel, RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${beatmap.metadata.artist} - ${beatmap.metadata.title}",
                color = TextSecondary,
                fontSize = 16.sp
            )
            Text(
                text = beatmap.metadata.version,
                color = TextDim,
                fontSize = 13.sp
            )
            Row(modifier = Modifier.padding(top = 4.dp)) {
                Text(
                    text = "${beatmap.difficulty.keyCount}K",
                    color = keyColor,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = "OD ${beatmap.difficulty.od.toInt()} HP ${beatmap.difficulty.hp.toInt()}",
                    color = TextDim,
                    fontSize = 12.sp
                )
            }
        }
    }
}
