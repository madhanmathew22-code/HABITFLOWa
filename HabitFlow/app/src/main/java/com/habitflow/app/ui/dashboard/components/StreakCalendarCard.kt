package com.habitflow.app.ui.dashboard.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitflow.app.domain.model.DaySummary
import com.habitflow.app.domain.model.DayStatus
import com.habitflow.app.ui.theme.DashboardPalette
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

/**
 * The Streak Calendar (section 9). Changing month animates the whole grid
 * horizontally -- the direction is inferred from which button was pressed,
 * matching "previous month slides one direction, next month slides the
 * other". Days with a completed streak get a one-shot subtle scale pulse
 * the first time they're shown, not a continuous animation.
 */
@Composable
fun StreakCalendarCard(
    month: YearMonth,
    days: Map<LocalDate, DaySummary>,
    selectedDate: LocalDate,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    palette: DashboardPalette,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(palette.cardBackground)
            .padding(18.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = "Streak Calendar", color = palette.textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${month.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${month.year}",
                    color = palette.textSecondary,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(end = 4.dp),
                )
                IconButton(onClick = onPreviousMonth) {
                    Icon(Icons.Filled.ChevronLeft, contentDescription = "Previous month", tint = palette.textSecondary)
                }
                IconButton(onClick = onNextMonth) {
                    Icon(Icons.Filled.ChevronRight, contentDescription = "Next month", tint = palette.textSecondary)
                }
            }
        }

        AnimatedContent(
            targetState = month,
            transitionSpec = {
                val forward = targetState.isAfter(initialState)
                val enter = slideInHorizontally(animationSpec = tween(280)) { width -> if (forward) width else -width }
                val exit = slideOutHorizontally(animationSpec = tween(280)) { width -> if (forward) -width else width }
                enter togetherWith exit
            },
            label = "streakCalendarMonth",
        ) { animatedMonth ->
            CalendarGrid(
                month = animatedMonth,
                days = days,
                selectedDate = selectedDate,
                palette = palette,
            )
        }
    }
}

@Composable
private fun CalendarGrid(
    month: YearMonth,
    days: Map<LocalDate, DaySummary>,
    selectedDate: LocalDate,
    palette: DashboardPalette,
) {
    Column(modifier = Modifier.padding(top = 12.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            val orderedFromSunday = listOf(
                DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY,
            )
            orderedFromSunday.forEach { dow ->
                Text(
                    text = dow.getDisplayName(TextStyle.SHORT, Locale.getDefault()).take(3),
                    color = palette.textTertiary,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        val firstOfMonth = month.atDay(1)
        // Sunday-first leading blanks, to match the reference screenshot's
        // Sun..Sat header row.
        val leadingBlanks = firstOfMonth.dayOfWeek.value % 7
        val totalCells = leadingBlanks + month.lengthOfMonth()
        val rowCount = (totalCells + 6) / 7

        for (row in 0 until rowCount) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (col in 0 until 7) {
                    val cellIndex = row * 7 + col
                    val dayOfMonth = cellIndex - leadingBlanks + 1
                    Box(modifier = Modifier.weight(1f).aspectRatio(1f).padding(3.dp)) {
                        if (dayOfMonth in 1..month.lengthOfMonth()) {
                            val date = month.atDay(dayOfMonth)
                            CalendarDayCell(
                                date = date,
                                summary = days[date],
                                isSelected = date == selectedDate,
                                palette = palette,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    date: LocalDate,
    summary: DaySummary?,
    isSelected: Boolean,
    palette: DashboardPalette,
) {
    val isStreakDay = summary?.overallStatus == DayStatus.COMPLETED
    val isToday = date == LocalDate.now()

    // One-shot pulse the first time a streak day appears, per spec ("very
    // subtle pulse when first displayed", explicitly NOT a continuous loop).
    val pulseScale = remember(date, isStreakDay) { Animatable(if (isStreakDay) 0.85f else 1f) }
    LaunchedEffect(date, isStreakDay) {
        if (isStreakDay) {
            pulseScale.animateTo(1f, animationSpec = tween(280))
        }
    }

    val selectionScale by animateFloatAsState(
        targetValue = if (isSelected) 1.08f else 1f,
        animationSpec = tween(150),
        label = "calendarDaySelection",
    )

    val background = when {
        isStreakDay -> palette.accentGreen
        else -> Color.Transparent
    }

    Box(
        modifier = Modifier
            .scale(pulseScale.value * selectionScale)
            .clip(CircleShape)
            .background(background)
            .then(
                if (isToday) {
                    Modifier.border(width = 2.dp, color = palette.accentCyan, shape = CircleShape)
                } else {
                    Modifier
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            color = if (isStreakDay) Color.White else palette.textSecondary,
            fontSize = 13.sp,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
        )
    }
}
