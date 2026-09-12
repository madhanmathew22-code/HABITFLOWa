package com.habitflow.app.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habitflow.app.domain.model.DaySummary
import com.habitflow.app.domain.model.HabitForDate
import com.habitflow.app.domain.usecase.GetCalendarMonthUseCase
import com.habitflow.app.domain.usecase.GetDayDetailUseCase
import com.habitflow.app.domain.usecase.ToggleHabitCompletionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

data class CalendarUiState(
    val yearMonth: YearMonth = YearMonth.now(),
    val daySummaries: Map<LocalDate, DaySummary> = emptyMap(),
    val selectedDate: LocalDate = LocalDate.now(),
    val selectedDayHabits: List<HabitForDate> = emptyList(),
    val isLoadingMonth: Boolean = true,
    val isLoadingDay: Boolean = true,
)

/**
 * Deliberately pull-based rather than Room-reactive (like TodayViewModel
 * and HabitDetailViewModel before it): a month view has no single Room
 * table to observe a Flow from -- it's an aggregate across every habit's
 * full history -- so it's loaded on demand (month change, date selection,
 * after a completion toggle) rather than kept continuously live. Same
 * documented tradeoff as the rest of the app: correct for in-app actions,
 * won't self-update from something external without reopening the screen.
 */
@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val getCalendarMonth: GetCalendarMonthUseCase,
    private val getDayDetail: GetDayDetailUseCase,
    private val toggleHabitCompletion: ToggleHabitCompletionUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        loadMonth(_uiState.value.yearMonth)
        loadDay(_uiState.value.selectedDate)
    }

    fun onPreviousMonth() = changeMonth(_uiState.value.yearMonth.minusMonths(1))

    fun onNextMonth() = changeMonth(_uiState.value.yearMonth.plusMonths(1))

    private fun changeMonth(newMonth: YearMonth) {
        _uiState.value = _uiState.value.copy(yearMonth = newMonth)
        loadMonth(newMonth)
    }

    fun onSelectDate(date: LocalDate) {
        _uiState.value = _uiState.value.copy(selectedDate = date)
        loadDay(date)
    }

    fun onToggleComplete(habitForDate: HabitForDate) {
        viewModelScope.launch {
            toggleHabitCompletion(habitForDate)
            // The completion may have changed this date's (and this month's)
            // aggregate status, so refresh both.
            loadDay(_uiState.value.selectedDate)
            loadMonth(_uiState.value.yearMonth)
        }
    }

    private fun loadMonth(yearMonth: YearMonth) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingMonth = true)
            val summaries = getCalendarMonth(yearMonth)
            _uiState.value = _uiState.value.copy(daySummaries = summaries, isLoadingMonth = false)
        }
    }

    private fun loadDay(date: LocalDate) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingDay = true)
            val habits = getDayDetail(date)
            _uiState.value = _uiState.value.copy(selectedDayHabits = habits, isLoadingDay = false)
        }
    }
}
