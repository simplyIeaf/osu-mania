package com.leaf.osumania.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leaf.osumania.engine.GameConstants
import com.leaf.osumania.engine.GameEngine
import com.leaf.osumania.ui.theme.AccentBlue
import com.leaf.osumania.ui.theme.AccentGreen
import com.leaf.osumania.ui.theme.AccentPink
import com.leaf.osumania.ui.theme.AccentPurple
import com.leaf.osumania.ui.theme.AccentRed
import com.leaf.osumania.ui.theme.AccentYellow
import com.leaf.osumania.ui.theme.Background
import com.leaf.osumania.ui.theme.Panel
import com.leaf.osumania.ui.theme.TextDim
import com.leaf.osumania.ui.theme.TextSecondary

@Composable
fun ResultsScreen(
    engine: GameEngine,
    onBack: () -> Unit,
    onRetry: () -> Unit
) {
    val grade = engine.scoreSystem.getLetterGrade()
    val gradeColor = when (grade) {
        "SS", "X", "S" -> AccentYellow
        "A" -> AccentPurple
        "B" -> AccentBlue
        "C" -> AccentPink
        "D" -> AccentRed
        else -> TextSecondary
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = grade, color = gradeColor, fontSize = 48.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Score: ${engine.scoreSystem.score}", color = TextSecondary, fontSize = 22.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Accuracy: ${"%.2f".format(engine.scoreSystem.accuracy * 100f)}%",
            color = TextSecondary,
            fontSize = 22.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Max Combo: ${engine.scoreSystem.maxCombo}x", color = TextSecondary, fontSize = 22.sp)
        Spacer(modifier = Modifier.height(20.dp))

        Column(modifier = Modifier.background(Panel, RoundedCornerShape(8.dp)).padding(12.dp)) {
            for (j in intArrayOf(320, 300, 200, 100, 50, 0)) {
                val count = engine.scoreSystem.getJudgementCount(j)
                val name = if (j == 320) "320g" else j.toString()
                val color = GameConstants.JUDGEMENT_COLORS[j] ?: TextSecondary
                Row(modifier = Modifier.padding(vertical = 2.dp)) {
                    Text(text = name, color = color, fontSize = 14.sp, modifier = Modifier.width(50.dp))
                    Text(text = count.toString(), color = TextSecondary, fontSize = 14.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        val pp = engine.scoreSystem.calculatePp(engine.beatmapData.difficulty.keyCount.toFloat())
        Text(text = "PP: ${pp.toInt()}", color = TextSecondary, fontSize = 22.sp)
        Spacer(modifier = Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = onBack,
                modifier = Modifier.width(140.dp).height(50.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Panel)
            ) {
                Text("Back")
            }
            Button(
                onClick = onRetry,
                modifier = Modifier.width(140.dp).height(50.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Panel)
            ) {
                Text("Retry")
            }
        }
    }
}
