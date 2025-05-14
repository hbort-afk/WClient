package com.retrivedmods.wclient.overlay

import android.util.Base64
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI

@Composable
fun Watermark() {
    val transition = rememberInfiniteTransition()
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(animation = tween(3000, easing = LinearEasing))
    )

    val rainbowColors = List(7) { i ->
        val hue = ((i * 360f / 7) + (phase * 180f / PI).toFloat()) % 360
        Color.hsv(hue, 1f, 1f)
    }

    val gradient = Brush.horizontalGradient(rainbowColors)

    // Base64 strings
    val base64Client = "V0NsaWVudA=="
    val base64Version = "djYuMA=="

    val clientName = String(Base64.decode(base64Client, Base64.DEFAULT))
    val versionText = String(Base64.decode(base64Version, Base64.DEFAULT))

    val richText = buildAnnotatedString {
        withStyle(style = SpanStyle(fontSize = 18.sp, fontWeight = FontWeight.SemiBold)) {
            append(clientName)
        }
        withStyle(
            style = SpanStyle(
                fontSize = 10.sp,
                baselineShift = BaselineShift.Superscript
            )
        ) {
            append(" $versionText")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        contentAlignment = Alignment.TopStart
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0x00000000)) // translucent dark background
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = richText,
                style = androidx.compose.ui.text.TextStyle(
                    brush = gradient,
                    fontSize = TextUnit.Unspecified,
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}
