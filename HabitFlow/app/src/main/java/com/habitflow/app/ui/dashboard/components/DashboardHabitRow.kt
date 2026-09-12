package com.habitflow.app.ui.dashboard.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitflow.app.domain.model.DayStatus
import com.habitflow.app.domain.model.HabitForDate
import com.habitflow.app.ui.common.IconRegistry
import com.habitflow.app.ui.theme.DashboardPalette

/**
 * One row in "Today's Habits" (sections 5 and 6). Toggling completion
 * animates the checkbox fill/checkmark, gives the row a brief highlight
 * tint, and applies a tiny scale "pop" -- deliberately no confetti, per
 * spec ("satisfying but professional").
 */
@Composable
fun DashboardHabitRow(
    habitForDate: HabitForDate,
    onToggle: () -> Unit,
    palette: DashboardPalette,
    modifier: Modifier = Modifier,
) {
    val isCompleted = habitForDate.status == DayStatus.COMPLETED
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()

    val rowScale by animateFloatAsState(
        targetValue = if (isPressed) 0.99f else 1f,
        animationSpec = tween(120),
        label = "habitRowScale",
    )
    val rowBackground by animateColorAsState(
        targetValue = when {
            isCompleted -> palette.accentGreenSoft
            isHovered -> palette.cardBackgroundElevated
            else -> Color.Transparent
        },
        animationSpec = tween(220),
        label = "habitRowBackground",
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        modifier = modifier
            .fillMaxWidth()
            .scale(rowScale)
            .hoverable(interactionSource)
            .clip(RoundedCornerShape(12.dp))
            .background(rowBackground)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onToggle)
            .padding(horizontal = 10.dp, vertical = 10.dp),
    ) {
        AnimatedCheckbox(isCompleted = isCompleted, palette = palette)

        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color(habitForDate.habit.colorArgb).copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = IconRegistry.iconFor(habitForDate.habit.iconKey),
                contentDescription = null,
                tint = Color(habitForDate.habit.colorArgb),
                modifier = Modifier.size(18.dp),
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(text = habitForDate.habit.name, color = palette.textPrimary, fontWeight = FontWeight.Medium, fontSize = 15.sp)
            if (habitForDate.habit.notes.isNotBlank()) {
                Text(text = habitForDate.habit.notes, color = palette.textTertiary, fontSize = 12.sp)
            }
        }

        Text(
            text = if (isCompleted) "Completed" else "Pending",
            color = if (isCompleted) palette.accentGreen else palette.textTertiary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun AnimatedCheckbox(isCompleted: Boolean, palette: DashboardPalette) {
    val checkScale by animateFloatAsState(
        targetValue = if (isCompleted) 1f else 0.85f,
        animationSpec = spring(dampingRatio = 0.55f),
        label = "checkboxPop",
    )
    val fillColor by animateColorAsState(
        targetValue = if (isCompleted) palette.accentGreen else Color.Transparent,
        animationSpec = tween(200),
        label = "checkboxFill",
    )
    val borderColor by animateColorAsState(
        targetValue = if (isCompleted) palette.accentGreen else palette.textTertiary,
        animationSpec = tween(200),
        label = "checkboxBorder",
    )

    Box(
        modifier = Modifier
            .size(24.dp)
            .scale(checkScale)
            .clip(CircleShape)
            .background(fillColor)
            .border(width = 2.dp, color = borderColor, shape = CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedVisibility(
            visible = isCompleted,
            enter = scaleIn(animationSpec = spring(dampingRatio = 0.5f)) + fadeIn(),
            exit = scaleOut(animationSpec = tween(120)) + fadeOut(),
        ) {
            Icon(Icons.Filled.Check, contentDescription = "Completed", tint = Color.White, modifier = Modifier.size(14.dp))
        }
    }
}
