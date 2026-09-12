package com.habitflow.app.ui.habits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habitflow.app.domain.model.Habit
import com.habitflow.app.domain.usecase.ArchiveHabitUseCase
import com.habitflow.app.domain.usecase.ObserveActiveHabitsUseCase
import com.habitflow.app.domain.usecase.SetHabitPinnedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HabitsViewModel @Inject constructor(
    observeActiveHabits: ObserveActiveHabitsUseCase,
    private val setHabitPinned: SetHabitPinnedUseCase,
    private val archiveHabit: ArchiveHabitUseCase,
) : ViewModel() {

    val habits: StateFlow<List<Habit>> = observeActiveHabits()
        .map { it.sortedWith(compareByDescending<Habit> { habit -> habit.isPinned }.thenBy { habit -> habit.name }) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun togglePinned(habit: Habit) {
        viewModelScope.launch { setHabitPinned(habit.id, !habit.isPinned) }
    }

    fun archive(habit: Habit) {
        viewModelScope.launch { archiveHabit(habit.id) }
    }
}
