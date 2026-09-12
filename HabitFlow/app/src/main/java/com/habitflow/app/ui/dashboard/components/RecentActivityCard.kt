package com.habitflow.app.ui.dashboard.components

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitflow.app.domain.model.DayStatus
import com.habitflow.app.domain.model.HabitForDate
import com.habitflow.app.ui.theme.DashboardPalette

/**
 * "Recent Activity" (section under Today's Habits in the reference).
 * HabitFlow's data model doesn't record a completion *timestamp* (only a
 * date), so this shows today's habits in schedule order with their
 * completed/missed state rather than the clock times shown in the mockup --
 * an honest simplification rather than fabricating times.
 */
@Composable
fun RecentActivityCard(habits: List<HabitForDate>, palette: DashboardPalette, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(palette.cardBackground)
            .padding(18.dp),
    ) {
        Text(text = "Recent Activity", color = palette.textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)

        if (habits.isEmpty()) {
            Text(
                text = "Nothing scheduled yet today.",
                color = palette.textTertiary,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 12.dp),
            )
        } else {
            Column(modifier = Modifier.padding(top = 12.dp)) {
                habits.forEach { habitForDate ->
                    ActivityRow(habitForDate = habitForDate, palette = palette)
                }
            }
        }
    }
}

@Composable
private fun ActivityRow(habitForDate: HabitForDate, palette: DashboardPalette) {
    val isCompleted = habitForDate.status == DayStatus.COMPLETED
    val isMissed = habitForDate.status == DayStatus.MISSED

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(
                    when {
                        isCompleted -> palette.accentGreen
                        isMissed -> palette.cardBorder
                        else -> palette.cardBorder
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (isCompleted) {
                Icon(Icons.Filled.Check, contentDescription = null, tint = androidx.compose.ui.graphics.Color.White, modifier = Modifier.size(12.dp))
            } else if (isMissed) {
                Icon(Icons.Filled.Close, contentDescription = null, tint = palette.textTertiary, modifier = Modifier.size(12.dp))
            }
        }

        Text(
            text = habitForDate.habit.name,
            color = palette.textPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f),
        )

        Text(
            text = when {
                isCompleted -> "Completed"
                isMissed -> "Missed"
                else -> "Pending"
            },
            color = when {
                isCompleted -> palette.accentGreen
                isMissed -> palette.textTertiary
                else -> palette.textTertiary
            },
            fontSize = 12.sp,
        )
    }
}
