package com.leaf.osumania.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leaf.osumania.ui.theme.AccentPink
import com.leaf.osumania.ui.theme.Background
import com.leaf.osumania.ui.theme.Border
import com.leaf.osumania.ui.theme.Primary

@Composable
fun LoadingScreen(onFinished: () -> Unit) {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        progress.animateTo(1f, animationSpec = tween(durationMillis = 1200))
        onFinished()
    }

    Box(
        modifier = Modifier.fillMaxSize().background(Background),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "osu!mania",
                color = AccentPink,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold
            )
            Box(
                modifier = Modifier
                    .padding(top = 40.dp)
                    .width(300.dp)
                    .height(6.dp)
                    .clip(RectangleShape)
                    .background(Border)
            ) {
                Box(
                    modifier = Modifier
                        .height(6.dp)
                        .width(300.dp * progress.value)
                        .clip(RectangleShape)
                        .background(Primary)
                )
            }
        }
    }
}
