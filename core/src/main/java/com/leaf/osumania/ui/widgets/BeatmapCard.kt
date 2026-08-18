package com.leaf.osumania.ui.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leaf.osumania.api.ApiBeatmapSet
import com.leaf.osumania.ui.theme.AccentPink
import com.leaf.osumania.ui.theme.Panel
import com.leaf.osumania.ui.theme.TextDim
import com.leaf.osumania.ui.theme.TextSecondary

@Composable
fun BeatmapCard(
    beatmapSet: ApiBeatmapSet,
    onSelect: (ApiBeatmapSet) -> Unit
) {
    val coverColor = when (beatmapSet.status) {
        "ranked" -> Color(0xFF4D99FF)
        "loved" -> AccentPink
        else -> Color(0xFF808080)
    }
    val statusColor = when (beatmapSet.status) {
        "ranked" -> Color(0xFF33CC55)
        "loved" -> AccentPink
        "graveyard", "WIP" -> Color(0xFF808080)
        else -> TextDim
    }
    val keyCount = beatmapSet.beatmaps.firstOrNull()?.let { bm ->
        when {
            bm.cs >= 7 -> "7K"
            bm.cs >= 6 -> "6K"
            bm.cs >= 5 -> "5K"
            else -> "4K"
        }
    } ?: "4K"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(Panel)
            .clickable { onSelect(beatmapSet) }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(coverColor)
        )

        Column(modifier = Modifier.weight(1f).padding(start = 10.dp)) {
            Text(text = beatmapSet.title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Text(text = beatmapSet.artist, color = TextDim, fontSize = 12.sp)
            Row(modifier = Modifier.padding(top = 4.dp)) {
                Text(text = keyCount, color = AccentPink, fontSize = 11.sp, modifier = Modifier.padding(end = 8.dp))
                Text(text = beatmapSet.status.uppercase(), color = statusColor, fontSize = 10.sp)
            }
        }
    }
}
