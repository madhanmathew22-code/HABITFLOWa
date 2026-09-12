package com.habitflow.app.ui.dashboard.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitflow.app.ui.theme.DashboardPalette
import kotlin.math.roundToInt

/**
 * The circular "Overall Progress" ring (section 4). The ring sweep and the
 * centered percentage animate together from 0 on entry; only the arc/value
 * animates, never the whole card (explicitly called out in the spec).
 */
@Composable
fun ProgressRing(
    progress: Float,
    palette: DashboardPalette,
    modifier: Modifier = Modifier,
    size: Dp = 180.dp,
    strokeWidth: Dp = 14.dp,
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(1100),
        label = "overallProgressRing",
    )

    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(size)) {
            val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            val inset = strokeWidth.toPx() / 2
            drawArc(
                color = palette.ringTrack,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = stroke,
                topLeft = Offset(inset, inset),
                size = Size(
                    this.size.width - strokeWidth.toPx(),
                    this.size.height - strokeWidth.toPx(),
                ),
            )
            drawArc(
                brush = Brush.sweepGradient(listOf(palette.accentCyan, palette.accentGreen, palette.accentCyan)),
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                style = stroke,
                topLeft = Offset(inset, inset),
                size = Size(
                    this.size.width - strokeWidth.toPx(),
                    this.size.height - strokeWidth.toPx(),
                ),
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${(animatedProgress * 100).roundToInt()}%",
                color = palette.textPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 34.sp,
            )
            Text(
                text = "Overall Progress",
                color = palette.textSecondary,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}
