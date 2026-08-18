package com.leaf.osumania.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import com.leaf.osumania.ui.theme.AccentPink
import com.leaf.osumania.ui.theme.AccentRed
import com.leaf.osumania.ui.theme.Background
import com.leaf.osumania.ui.theme.Panel
import com.leaf.osumania.ui.theme.TextPrimary

@Composable
fun MainMenuScreen(
    onPlay: () -> Unit,
    onSettings: () -> Unit,
    onImport: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize().background(Background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "osu!mania",
                color = AccentPink,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(40.dp))
            MenuButton("Play", onPlay)
            MenuButton("Settings", onSettings)
            MenuButton("Exit", onClick = {
                android.os.Process.killProcess(android.os.Process.myPid())
            }, color = AccentRed)
        }
    }
}

@Composable
private fun MenuButton(
    text: String,
    onClick: () -> Unit,
    color: androidx.compose.ui.graphics.Color = TextPrimary
) {
    Button(
        onClick = onClick,
        modifier = Modifier.width(280.dp).height(56.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Panel)
    ) {
        Text(text = text, color = color, fontSize = 18.sp)
    }
}
