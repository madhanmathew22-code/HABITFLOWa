package com.habitflow.app.ui.dashboard

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.habitflow.app.domain.model.HabitForDate
import com.habitflow.app.ui.dashboard.components.DashboardHabitRow
import com.habitflow.app.ui.dashboard.components.KpiCardRow
import com.habitflow.app.ui.dashboard.components.MotivationCard
import com.habitflow.app.ui.dashboard.components.ProgressRing
import com.habitflow.app.ui.dashboard.components.RecentActivityCard
import com.habitflow.app.ui.dashboard.components.StreakCalendarCard
import com.habitflow.app.ui.dashboard.components.WeeklyProgressChart
import com.habitflow.app.ui.dashboard.components.staggeredEntrance
import com.habitflow.app.ui.theme.DashboardPalette
import com.habitflow.app.ui.theme.DashboardPaletteDark
import com.habitflow.app.ui.theme.DashboardPaletteLight
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val motivationQuotes = listOf(
    "Discipline today, a better tomorrow.",
    "Small steps make big changes.",
    "Better habits. A brighter you.",
)

/**
 * The screenshot-matching dashboard content: header + KPI row + today's
 * habits / weekly chart / progress ring + recent activity / streak
 * calendar / motivation card. The permanent sidebar itself lives one level
 * up in [com.habitflow.app.ui.navigation.HabitFlowNavHost] so it persists
 * across every sidebar destination instead of being torn down and rebuilt
 * (and re-animated) on every screen change. Section 1's staggered entry
 * animation is driven by [staggeredEntrance] on each top-level block.
 */
@Composable
fun DashboardScreen(
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onOpenHabitCreation: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val palette = if (isDarkTheme) DashboardPaletteDark else DashboardPaletteLight

    Box(modifier = Modifier.fillMaxSize().background(palette.background)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(28.dp),
        ) {
            DashboardHeader(
                isDarkTheme = isDarkTheme,
                onToggleTheme = onToggleTheme,
                palette = palette,
                entranceIndex = 0,
            )

            Box(modifier = Modifier.padding(top = 24.dp).staggeredEntrance(1)) {
                KpiCardRow(state = state, palette = palette)
            }

            Row(
                modifier = Modifier
                    .padding(top = 20.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                Column(modifier = Modifier.weight(1.4f)) {
                    TodaysHabitsCard(
                        state = state,
                        onToggle = viewModel::onToggleComplete,
                        onAddHabit = onOpenHabitCreation,
                        palette = palette,
                        modifier = Modifier.staggeredEntrance(2),
                    )
                }
                Column(modifier = Modifier.weight(1.2f)) {
                    WeeklyProgressCard(state = state, palette = palette, modifier = Modifier.staggeredEntrance(3))
                }
                Column(modifier = Modifier.weight(0.9f)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(palette.cardBackground)
                            .padding(20.dp)
                            .staggeredEntrance(3),
                        contentAlignment = Alignment.Center,
                    ) {
                        ProgressRing(progress = state.successRate, palette = palette)
                    }
                }
            }

            Row(
                modifier = Modifier
                    .padding(top = 20.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                Column(modifier = Modifier.weight(1.2f)) {
                    RecentActivityCard(
                        habits = state.habitsToday,
                        palette = palette,
                        modifier = Modifier.staggeredEntrance(4),
                    )
                }
                Column(modifier = Modifier.weight(1.4f)) {
                    StreakCalendarCard(
                        month = state.calendarMonth,
                        days = state.calendarDays,
                        selectedDate = state.date,
                        onPreviousMonth = viewModel::onPreviousMonth,
                        onNextMonth = viewModel::onNextMonth,
                        palette = palette,
                        modifier = Modifier.staggeredEntrance(4),
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    val quote = remember { motivationQuotes.random() }
                    MotivationCard(quote = quote, palette = palette, modifier = Modifier.staggeredEntrance(4))
                }
            }
        }
    }
}

@Composable
private fun DashboardHeader(
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    palette: DashboardPalette,
    entranceIndex: Int,
) {
    val today = remember { LocalDate.now() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .staggeredEntrance(entranceIndex),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Column {
            Text(
                text = "Good ${greetingWord()} \uD83D\uDC4B",
                color = palette.textPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
            )
            Text(
                text = "Keep going! You're building a better you.",
                color = palette.textSecondary,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 2.dp),
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = today.format(DateTimeFormatter.ofPattern("EEE, d MMM yyyy")),
                color = palette.textSecondary,
                fontSize = 13.sp,
                modifier = Modifier.padding(end = 12.dp),
            )
            ThemeToggleButton(isDarkTheme = isDarkTheme, onToggle = onToggleTheme, palette = palette)
        }
    }
}

@Composable
private fun ThemeToggleButton(isDarkTheme: Boolean, onToggle: () -> Unit, palette: DashboardPalette) {
    val rotation by animateFloatAsState(
        targetValue = if (isDarkTheme) 0f else 180f,
        animationSpec = tween(350),
        label = "themeToggleRotation",
    )
    val background by animateColorAsState(
        targetValue = palette.cardBackgroundElevated,
        animationSpec = tween(350),
        label = "themeToggleBackground",
    )

    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(background)
            .clickable(onClick = onToggle),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = if (isDarkTheme) Icons.Filled.DarkMode else Icons.Filled.LightMode,
            contentDescription = "Toggle theme",
            tint = palette.textPrimary,
            modifier = Modifier.rotate(rotation),
        )
    }
}

@Composable
private fun TodaysHabitsCard(
    state: DashboardUiState,
    onToggle: (HabitForDate) -> Unit,
    onAddHabit: () -> Unit,
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
            Text(text = "Today's Habits", color = palette.textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Button(onClick = onAddHabit) {
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Text(text = "Add Habit", modifier = Modifier.padding(start = 6.dp))
            }
        }

        if (state.habitsToday.isEmpty() && !state.isLoading) {
            Text(
                text = "Build your first habit -- it'll show up here every day it's scheduled.",
                color = palette.textTertiary,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 16.dp),
            )
        } else {
            Column(modifier = Modifier.padding(top = 10.dp)) {
                state.habitsToday.forEach { habitForDate ->
                    DashboardHabitRow(
                        habitForDate = habitForDate,
                        onToggle = { onToggle(habitForDate) },
                        palette = palette,
                    )
                }
            }
        }
    }
}

@Composable
private fun WeeklyProgressCard(state: DashboardUiState, palette: DashboardPalette, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(palette.cardBackground)
            .padding(18.dp),
    ) {
        Text(text = "Weekly Progress", color = palette.textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        WeeklyProgressChart(
            days = state.weeklyProgress,
            palette = palette,
            modifier = Modifier.padding(top = 12.dp),
        )
    }
}

private fun greetingWord(): String {
    val hour = LocalTime.now().hour
    return when {
        hour < 12 -> "morning"
        hour < 17 -> "afternoon"
        else -> "evening"
    }
}
