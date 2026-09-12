package com.habitflow.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habitflow.app.domain.model.DaySummary
import com.habitflow.app.domain.model.DayStatus
import com.habitflow.app.domain.model.HabitForDate
import com.habitflow.app.domain.usecase.GetCalendarMonthUseCase
import com.habitflow.app.domain.usecase.GetDateRangeSummaryUseCase
import com.habitflow.app.domain.usecase.GetHabitDayStateUseCase
import com.habitflow.app.domain.usecase.ObserveHabitsScheduledOnUseCase
import com.habitflow.app.domain.usecase.ToggleHabitCompletionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import javax.inject.Inject

/** One bar in the Weekly Progress chart. */
data class WeekDayProgress(
    val date: LocalDate,
    val label: String,
    val fraction: Float,
    val isToday: Boolean,
)

data class DashboardUiState(
    val date: LocalDate = LocalDate.now(),
    val habitsToday: List<HabitForDate> = emptyList(),
    val currentStreak: Int = 0,
    val totalCompletedRecent: Int = 0,
    val successRate: Float = 0f,
    val weeklyProgress: List<WeekDayProgress> = emptyList(),
    val calendarMonth: YearMonth = YearMonth.now(),
    val calendarDays: Map<LocalDate, DaySummary> = emptyMap(),
    val isLoading: Boolean = true,
) {
    val completedToday: Int get() = habitsToday.count { it.status == DayStatus.COMPLETED }
    val totalToday: Int get() = habitsToday.size
    val todayProgressFraction: Float
        get() = if (totalToday == 0) 0f else completedToday.toFloat() / totalToday
}

/**
 * Drives the Dashboard screen (the sidebar-based, screenshot-matching
 * layout). Reuses the same reactive habit/day-state plumbing as
 * [com.habitflow.app.ui.today.TodayViewModel] for "today's habits", and
 * layers on the wider aggregates the Dashboard needs: a trailing window for
 * the KPI row, the current Mon-Sun week for the bar chart, and one calendar
 * month for the streak calendar.
 *
 * "Total Completed" and "Success Rate" are computed over a trailing
 * [RECENT_WINDOW_DAYS] window rather than a habit's full history -- the
 * data layer has no cheap "since the beginning of time" aggregate (see
 * HabitRepository), so a bounded recent window keeps this a fixed number of
 * queries regardless of how long a habit has existed.
 */
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val observeHabitsScheduledOn: ObserveHabitsScheduledOnUseCase,
    private val getHabitDayState: GetHabitDayStateUseCase,
    private val toggleHabitCompletion: ToggleHabitCompletionUseCase,
    private val getDateRangeSummary: GetDateRangeSummaryUseCase,
    private val getCalendarMonth: GetCalendarMonthUseCase,
) : ViewModel() {

    private val today = LocalDate.now()
    private val refreshTrigger = MutableStateFlow(0)
    private val monthCursor = MutableStateFlow(YearMonth.from(today))

    private val todayState: StateFlow<Pair<List<HabitForDate>, Int>> = combine(
        observeHabitsScheduledOn(today),
        refreshTrigger,
    ) { habits, trigger -> habits to trigger }
        .flatMapLatest { (habits, trigger) ->
            flow {
                val rows = habits.map { habit -> getHabitDayState(habit, today) }
                emit(rows to trigger)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList<HabitForDate>() to 0)

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState

    init {
        viewModelScope.launch {
            todayState.collect { (habits, _) ->
                val streak = habits.maxOfOrNull { it.currentStreak } ?: 0
                _uiState.update {
                    it.copy(habitsToday = habits, currentStreak = streak, isLoading = false)
                }
                loadRecentAggregates()
            }
        }
        loadMonth(monthCursor.value)
    }

    fun onToggleComplete(habitForDate: HabitForDate) {
        viewModelScope.launch {
            toggleHabitCompletion(habitForDate)
            refreshTrigger.value += 1
            loadMonth(monthCursor.value)
        }
    }

    fun onPreviousMonth() {
        monthCursor.update { it.minusMonths(1) }
        loadMonth(monthCursor.value)
    }

    fun onNextMonth() {
        monthCursor.update { it.plusMonths(1) }
        loadMonth(monthCursor.value)
    }

    private fun loadMonth(month: YearMonth) {
        viewModelScope.launch {
            val days = getCalendarMonth(month)
            _uiState.update { it.copy(calendarMonth = month, calendarDays = days) }
        }
    }

    private suspend fun loadRecentAggregates() {
        val weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val weekDates = (0..6).map { weekStart.plusDays(it.toLong()) }
        val weekSummaries = getDateRangeSummary(weekDates)

        val weeklyProgress = weekDates.map { date ->
            val summary = weekSummaries[date] ?: DaySummary.empty(date)
            val fraction = if (summary.scheduledCount == 0) {
                0f
            } else {
                summary.completedCount.toFloat() / summary.scheduledCount
            }
            WeekDayProgress(
                date = date,
                label = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                fraction = fraction,
                isToday = date == today,
            )
        }

        val recentStart = today.minusDays((RECENT_WINDOW_DAYS - 1).toLong())
        val recentDates = (0 until RECENT_WINDOW_DAYS).map { recentStart.plusDays(it.toLong()) }
        val recentSummaries = getDateRangeSummary(recentDates)
        val totalScheduled = recentSummaries.values.sumOf { it.scheduledCount }
        val totalCompleted = recentSummaries.values.sumOf { it.completedCount }
        val successRate = if (totalScheduled == 0) 0f else totalCompleted.toFloat() / totalScheduled

        _uiState.update {
            it.copy(
                weeklyProgress = weeklyProgress,
                totalCompletedRecent = totalCompleted,
                successRate = successRate,
            )
        }
    }

    private companion object {
        const val RECENT_WINDOW_DAYS = 90
    }
}
