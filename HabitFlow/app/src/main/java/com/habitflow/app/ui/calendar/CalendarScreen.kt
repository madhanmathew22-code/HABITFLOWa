package com.habitflow.app.ui.calendar

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.habitflow.app.domain.model.DayStatus
import com.habitflow.app.domain.model.DaySummary
import com.habitflow.app.domain.model.HabitForDate
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * Month view only for this phase (week/day views from section 12 are a
 * follow-up on top of the same [CalendarViewModel] -- both would reuse
 * [GetDayDetailUseCase], just with a different date range for the header).
 */
@Composable
fun CalendarScreen(viewModel: CalendarViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            MonthHeader(
                yearMonth = state.yearMonth,
                onPrevious = viewModel::onPreviousMonth,
                onNext = viewModel::onNextMonth,
            )
        }
        item { WeekdayHeaderRow() }
        item {
            AnimatedContent(
                targetState = state.yearMonth,
                transitionSpec = {
                    val forward = targetState.isAfter(initialState)
                    (if (forward) slideInHorizontally { it } else slideInHorizontally { -it }) togetherWith
                        (if (forward) slideOutHorizontally { -it } else slideOutHorizontally { it })
                },
                label = "calendar-month",
            ) { month ->
                MonthGrid(
                    yearMonth = month,
                    daySummaries = state.daySummaries,
                    selectedDate = state.selectedDate,
                    onSelectDate = viewModel::onSelectDate,
                )
            }
        }

        item {
            Text(
                text = state.selectedDate.format(DateTimeFormatter.ofPattern("EEEE, MMMM d")),
                style = MaterialTheme.typography.titleLarge,
            )
        }

        if (state.selectedDayHabits.isEmpty() && !state.isLoadingDay) {
            item {
                Text(
                    text = "Nothing scheduled this day.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            items(state.selectedDayHabits, key = { it.habit.id }) { habitForDate ->
                DayDetailHabitRow(habitForDate = habitForDate, onToggle = { viewModel.onToggleComplete(habitForDate) })
            }
        }
    }
}

@Composable
private fun MonthHeader(yearMonth: YearMonth, onPrevious: () -> Unit, onNext: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.Filled.ChevronLeft, contentDescription = "Previous month")
        }
        Text(
            text = yearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
            style = MaterialTheme.typography.titleLarge,
        )
        IconButton(onClick = onNext) {
            Icon(Icons.Filled.ChevronRight, contentDescription = "Next month")
        }
    }
}

@Composable
private fun WeekdayHeaderRow() {
    Row(modifier = Modifier.fillMaxWidth()) {
        val monday = LocalDate.now().with(java.time.DayOfWeek.MONDAY)
        repeat(7) { offset ->
            val day = monday.plusDays(offset.toLong())
            Text(
                text = day.dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.getDefault()),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun MonthGrid(
    yearMonth: YearMonth,
    daySummaries: Map<LocalDate, DaySummary>,
    selectedDate: LocalDate,
    onSelectDate: (LocalDate) -> Unit,
) {
    val firstOfMonth = yearMonth.atDay(1)
    // ISO: Monday=1 .. Sunday=7. Number of blank leading cells so the 1st
    // lands under its correct weekday column.
    val leadingBlanks = firstOfMonth.dayOfWeek.value - 1
    val totalDays = yearMonth.lengthOfMonth()
    val cells: List<LocalDate?> = List(leadingBlanks) { null } + (1..totalDays).map { yearMonth.atDay(it) }

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        // Row height is an estimate (44dp), not measured from the grid's
        // actual cell size -- LazyVerticalGrid needs a bounded height when
        // nested inside a LazyColumn item with scrolling disabled, and this
        // is the simplest way to provide one. Worth revisiting with a
        // measured/adaptive height if 44dp looks off on a real device.
        modifier = Modifier
            .fillMaxWidth()
            .height((((cells.size + 6) / 7) * 44).dp),
        userScrollEnabled = false,
    ) {
        items(cells) { date ->
            if (date == null) {
                Box(modifier = Modifier.aspectRatio(1f))
            } else {
                DayCell(
                    date = date,
                    summary = daySummaries[date] ?: DaySummary.empty(date),
                    isSelected = date == selectedDate,
                    isToday = date == LocalDate.now(),
                    onClick = { onSelectDate(date) },
                )
            }
        }
    }
}

@Composable
private fun DayCell(date: LocalDate, summary: DaySummary, isSelected: Boolean, isToday: Boolean, onClick: () -> Unit) {
    val dotColor = when (summary.overallStatus) {
        DayStatus.COMPLETED -> MaterialTheme.colorScheme.primary
        DayStatus.MISSED -> MaterialTheme.colorScheme.error
        DayStatus.PAUSED -> MaterialTheme.colorScheme.outline
        DayStatus.PENDING -> MaterialTheme.colorScheme.secondary
        DayStatus.NOT_SCHEDULED -> Color.Transparent
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clickable(onClick = onClick)
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
            )
            Box(
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(5.dp)
                    .background(dotColor, CircleShape),
            )
        }
    }
}

@Composable
private fun DayDetailHabitRow(habitForDate: HabitForDate, onToggle: () -> Unit) {
    val isCompleted = habitForDate.status == DayStatus.COMPLETED
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(habitForDate.habit.name, style = MaterialTheme.typography.titleMedium)
            Icon(
                imageVector = if (isCompleted) Icons.Filled.Check else Icons.Filled.RadioButtonUnchecked,
                contentDescription = if (isCompleted) "Completed" else "Not completed",
                tint = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
            )
        }
    }
}
