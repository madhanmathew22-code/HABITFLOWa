package com.habitflow.app.ui.dashboard.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitflow.app.ui.theme.DashboardPalette

/**
 * The Motivation card (section 10). Motion here is deliberately almost
 * imperceptible: a very slow gradient drift and a barely-there vertical
 * float on the leaf icon, both continuous but small enough to read as
 * "alive" rather than "animated". Nothing here should draw the eye away
 * from the KPI/chart/ring entry animations.
 */
@Composable
fun MotivationCard(quote: String, palette: DashboardPalette, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "motivationCard")

    val gradientShift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 14000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "motivationGradientShift",
    )
    val leafFloat by transition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "motivationLeafFloat",
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        palette.accentPurpleSoft,
                        palette.accentBlueSoft,
                        palette.accentPurpleSoft,
                    ),
                    start = Offset(gradientShift * 200f, 0f),
                    end = Offset(400f + gradientShift * 200f, 400f),
                ),
            )
            .padding(20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Motivation",
                color = Color.White.copy(alpha = 0.7f),
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                modifier = Modifier.padding(bottom = 20.dp),
            )
            Text(
                text = "\u201C$quote\u201D",
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                fontStyle = FontStyle.Italic,
                textAlign = TextAlign.Center,
            )
            Icon(
                imageVector = Icons.Filled.Spa,
                contentDescription = null,
                tint = palette.accentGreen,
                modifier = Modifier
                    .padding(top = 16.dp)
                    .graphicsLayer { translationY = leafFloat },
            )
        }
    }
}
