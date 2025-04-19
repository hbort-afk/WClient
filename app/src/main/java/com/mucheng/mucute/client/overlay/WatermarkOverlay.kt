package com.mucheng.mucute.client.overlay

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.sin

class WatermarkOverlay : OverlayWindow() {

    private var hue by mutableStateOf(0f)

    private fun getRGBColor(): Color {
        val red = (sin(hue) * 127 + 128).toInt().coerceIn(0, 255)
        val green = (sin(hue + 2f) * 127 + 128).toInt().coerceIn(0, 255)
        val blue = (sin(hue + 4f) * 127 + 128).toInt().coerceIn(0, 255)
        return Color(red, green, blue)
    }

    @Composable
    override fun Content() {
        LaunchedEffect(Unit) {
            while (true) {
                hue += 0.1f
                kotlinx.coroutines.delay(50)
            }
        }

        Box(modifier = Modifier.offset(12.dp, 12.dp)) {
            BasicText(
                text = "WClient Premium",
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = getRGBColor()
                )
            )
        }
    }
}
