package com.habitflow.app.ui.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habitflow.app.domain.model.DayStatus
import com.habitflow.app.domain.model.HabitForDate
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
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class TodayUiState(
    val date: LocalDate = LocalDate.now(),
    val habits: List<HabitForDate> = emptyList(),
    val isLoading: Boolean = true,
) {
    val completedCount: Int get() = habits.count { it.status == DayStatus.COMPLETED }
    val totalCount: Int get() = habits.size
    val progressFraction: Float
        get() = if (totalCount == 0) 0f else completedCount.toFloat() / totalCount
    val isPerfectDay: Boolean get() = totalCount > 0 && completedCount == totalCount
}

/**
 * Drives the Today dashboard (section 5).
 *
 * Habit *definitions* are reactive to Room via [ObserveHabitsScheduledOnUseCase].
 * Per-day *completion state* is recomputed from [GetHabitDayStateUseCase] each
 * time [refreshTrigger] ticks -- see the README's "known limitations" section
 * for why this isn't a fully Room-reactive join yet (Phase A did not change
 * that; it's a data-layer addition planned for later, tracked there).
 */
@HiltViewModel
class TodayViewModel @Inject constructor(
    private val observeHabitsScheduledOn: ObserveHabitsScheduledOnUseCase,
    private val getHabitDayState: GetHabitDayStateUseCase,
    private val toggleHabitCompletion: ToggleHabitCompletionUseCase,
) : ViewModel() {

    private val today = LocalDate.now()
    private val refreshTrigger = MutableStateFlow(0)

    val uiState: StateFlow<TodayUiState> = combine(
        observeHabitsScheduledOn(today),
        refreshTrigger,
    ) { habits, _ -> habits }
        .flatMapLatest { habits ->
            flow {
                val rows = habits.map { habit -> getHabitDayState(habit, today) }
                emit(TodayUiState(date = today, habits = rows, isLoading = false))
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TodayUiState())

    fun onToggleComplete(habitForDate: HabitForDate) {
        viewModelScope.launch {
            toggleHabitCompletion(habitForDate)
            refreshTrigger.value += 1
        }
    }
}
