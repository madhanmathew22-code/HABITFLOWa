package com.habitflow.app.ui.dashboard.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitflow.app.ui.dashboard.WeekDayProgress
import com.habitflow.app.ui.theme.DashboardPalette
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * The Weekly Progress bar chart (section 3). Bars grow from 0 to their
 * final height with a per-bar stagger, and each percentage label fades in
 * only once its bar has roughly finished growing, rather than all
 * appearing immediately.
 */
@Composable
fun WeeklyProgressChart(
    days: List<WeekDayProgress>,
    palette: DashboardPalette,
    modifier: Modifier = Modifier,
) {
    if (days.isEmpty()) return

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        days.forEachIndexed { index, day ->
            WeeklyBar(
                day = day,
                staggerIndex = index,
                palette = palette,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun WeeklyBar(
    day: WeekDayProgress,
    staggerIndex: Int,
    palette: DashboardPalette,
    modifier: Modifier = Modifier,
) {
    val heightFraction = remember { Animatable(0f) }
    var labelVisible by remember { mutableStateOf(false) }

    LaunchedEffect(day.date, day.fraction) {
        labelVisible = false
        heightFraction.snapTo(0f)
        delay(staggerIndex * 60L)
        val growth = launch {
            heightFraction.animateTo(day.fraction.coerceIn(0f, 1f), animationSpec = tween(650))
        }
        delay(500)
        labelVisible = true
        growth.join()
    }

    val labelAlpha by animateFloatAsState(
        targetValue = if (labelVisible) 1f else 0f,
        animationSpec = tween(300),
        label = "weeklyBarLabel",
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "${(day.fraction * 100).roundToInt()}%",
            color = palette.textSecondary.copy(alpha = labelAlpha),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 6.dp),
        )

        // The bar itself: an empty-height reserve up top plus a
        // fillMaxHeight(fraction) Canvas anchored to the bottom, so the
        // Canvas's own height IS the animated value -- no manual pixel math.
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Bottom,
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(fraction = heightFraction.value.coerceIn(0.02f, 1f)),
            ) {
                val barBrush = if (day.isToday) {
                    Brush.verticalGradient(listOf(palette.accentCyan, palette.accentGreen))
                } else {
                    Brush.verticalGradient(listOf(palette.chartBarTop, palette.chartBarBottom))
                }
                drawRoundRect(
                    brush = barBrush,
                    size = Size(size.width, size.height),
                    cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx()),
                )
            }
        }

        Text(
            text = day.label,
            color = if (day.isToday) palette.textPrimary else palette.textTertiary,
            fontWeight = if (day.isToday) FontWeight.SemiBold else FontWeight.Normal,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}
