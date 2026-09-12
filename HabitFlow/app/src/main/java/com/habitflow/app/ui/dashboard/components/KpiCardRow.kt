package com.habitflow.app.ui.dashboard.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitflow.app.ui.dashboard.DashboardUiState
import com.habitflow.app.ui.theme.DashboardPalette
import kotlin.math.roundToInt

private data class KpiSpec(
    val title: String,
    val value: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val iconColor: Color,
    val iconBackground: Color,
    val progress: Float,
)

/**
 * The 4-card KPI row (section 2). Each card counts its number up from zero
 * on first appearance and gets a small elevation/scale/glow response to
 * press or hover -- there's no real "hover" concept on touch, but
 * [Modifier.hoverable] still fires for stylus/mouse-capable devices
 * (Chromebooks, tablets with a pointer), matching the spec item without
 * hurting phone/touch use.
 */
@Composable
fun KpiCardRow(state: DashboardUiState, palette: DashboardPalette, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        val cards = listOf(
            KpiSpec(
                title = "Completed Today",
                value = "${state.completedToday} / ${state.totalToday} habits",
                icon = Icons.Filled.CheckCircle,
                iconColor = Color.White,
                iconBackground = palette.accentGreen,
                progress = state.todayProgressFraction,
            ),
            KpiSpec(
                title = "Current Streak",
                value = "${state.currentStreak} days",
                icon = Icons.Filled.LocalFireDepartment,
                iconColor = Color.White,
                iconBackground = palette.accentBlue,
                progress = 1f,
            ),
            KpiSpec(
                title = "Total Completed",
                value = "${state.totalCompletedRecent} habits",
                icon = Icons.Filled.EmojiEvents,
                iconColor = Color.White,
                iconBackground = palette.accentPurple,
                progress = 1f,
            ),
            KpiSpec(
                title = "Success Rate",
                value = "${(state.successRate * 100).roundToInt()}%",
                icon = Icons.Filled.TrackChanges,
                iconColor = Color.White,
                iconBackground = palette.accentPink,
                progress = state.successRate,
            ),
        )

        cards.forEach { spec ->
            KpiCard(spec = spec, palette = palette, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun KpiCard(spec: KpiSpec, palette: DashboardPalette, modifier: Modifier = Modifier) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()
    val emphasized = isPressed || isHovered

    val scale by animateFloatAsState(
        targetValue = if (emphasized) 1.02f else 1f,
        animationSpec = tween(150),
        label = "kpiCardScale",
    )
    val borderColor by animateFloatAsState(
        targetValue = if (emphasized) 1f else 0f,
        animationSpec = tween(150),
        label = "kpiCardBorder",
    )

    // Numeric count-up: parses the leading integer out of the display
    // value, animates a matching integer up from zero, then re-inserts it
    // into the original string so the unit/suffix ("habits", "days", "%")
    // is preserved without each KPI needing its own bespoke formatter.
    val targetNumber = remember(spec.value) { leadingNumber(spec.value) }
    val animatedNumber = remember(spec.value) { Animatable(0f) }
    LaunchedEffect(spec.value) {
        animatedNumber.snapTo(0f)
        animatedNumber.animateTo(targetNumber, animationSpec = tween(900))
    }
    val animatedFraction by animateFloatAsState(
        targetValue = spec.progress,
        animationSpec = tween(900),
        label = "kpiCardFraction",
    )

    Column(
        modifier = modifier
            .scale(scale)
            .hoverable(interactionSource)
            .clip(RoundedCornerShape(18.dp))
            .background(palette.cardBackground)
            .border(
                width = 1.dp,
                color = androidx.compose.ui.graphics.lerp(palette.cardBorder, spec.iconBackground, borderColor * 0.6f),
                shape = RoundedCornerShape(18.dp),
            )
            .padding(18.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(spec.iconBackground),
                contentAlignment = Alignment.Center,
            ) {
                Icon(spec.icon, contentDescription = null, tint = spec.iconColor, modifier = Modifier.size(22.dp))
            }
            Text(text = spec.title, color = palette.textSecondary, fontSize = 13.sp)
        }

        Text(
            text = formatWithAnimatedNumber(spec.value, animatedNumber.value.roundToInt()),
            color = palette.textPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 26.sp,
            modifier = Modifier.padding(top = 12.dp),
        )

        Box(
            modifier = Modifier
                .padding(top = 10.dp)
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(palette.cardBorder),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedFraction.coerceIn(0f, 1f))
                    .clip(RoundedCornerShape(2.dp))
                    .background(spec.iconBackground),
            )
        }
    }
}

/** Extracts the leading run of digits from a KPI display string, e.g. "78%" -> 78f. */
private fun leadingNumber(value: String): Float =
    value.takeWhile { it.isDigit() }.ifEmpty { "0" }.toFloat()

/** Re-inserts an animated integer in place of the leading digits of [original]. */
private fun formatWithAnimatedNumber(original: String, animated: Int): String {
    val remainder = original.dropWhile { it.isDigit() }
    return "$animated$remainder"
}
