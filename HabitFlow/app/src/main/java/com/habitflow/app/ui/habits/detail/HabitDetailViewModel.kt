package com.habitflow.app.ui.habits.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habitflow.app.domain.model.HabitForDate
import com.habitflow.app.domain.usecase.ArchiveHabitUseCase
import com.habitflow.app.domain.usecase.CreateHabitUseCase
import com.habitflow.app.domain.usecase.DuplicateHabitUseCase
import com.habitflow.app.domain.usecase.GetHabitDayStateUseCase
import com.habitflow.app.domain.usecase.ObserveHabitUseCase
import com.habitflow.app.domain.usecase.PauseHabitUseCase
import com.habitflow.app.domain.usecase.ResumeHabitUseCase
import com.habitflow.app.domain.usecase.SetHabitPinnedUseCase
import com.habitflow.app.domain.usecase.SkipHabitTodayUseCase
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

data class HabitDetailUiState(
    val today: HabitForDate? = null,
    val isLoading: Boolean = true,
    val isArchived: Boolean = false,
)

@HiltViewModel
class HabitDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeHabit: ObserveHabitUseCase,
    private val getHabitDayState: GetHabitDayStateUseCase,
    private val toggleHabitCompletion: ToggleHabitCompletionUseCase,
    private val archiveHabit: ArchiveHabitUseCase,
    private val setHabitPinned: SetHabitPinnedUseCase,
    private val skipHabitToday: SkipHabitTodayUseCase,
    private val pauseHabit: PauseHabitUseCase,
    private val resumeHabit: ResumeHabitUseCase,
    private val duplicateHabit: DuplicateHabitUseCase,
) : ViewModel() {

    val habitId: Long = checkNotNull(savedStateHandle.get<Long>("habitId")) {
        "HabitDetailScreen requires a habitId nav argument"
    }
    private val today = LocalDate.now()
    private val refreshTrigger = MutableStateFlow(0)

    val uiState: StateFlow<HabitDetailUiState> = combine(
        observeHabit(habitId),
        refreshTrigger,
    ) { habit, _ -> habit }
        .flatMapLatest { habit ->
            flow {
                if (habit == null) {
                    emit(HabitDetailUiState(today = null, isLoading = false))
                } else {
                    val dayState = getHabitDayState(habit, today)
                    emit(HabitDetailUiState(today = dayState, isLoading = false, isArchived = habit.isArchived))
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HabitDetailUiState())

    private fun refresh() {
        refreshTrigger.value += 1
    }

    fun onToggleComplete() {
        val current = uiState.value.today ?: return
        viewModelScope.launch {
            toggleHabitCompletion(current)
            refresh()
        }
    }

    fun onTogglePinned() {
        val current = uiState.value.today?.habit ?: return
        viewModelScope.launch { setHabitPinned(current.id, !current.isPinned) }
    }

    fun onArchive(onDone: () -> Unit) {
        val current = uiState.value.today?.habit ?: return
        viewModelScope.launch {
            archiveHabit(current.id)
            onDone()
        }
    }

    /** Section 8: skip today without damaging the streak -- reuses the pause mechanism for a single day. */
    fun onSkipToday() {
        viewModelScope.launch {
            skipHabitToday(habitId)
            refresh()
        }
    }

    /** Pauses indefinitely (endDate = null) until [onResume] is called. */
    fun onPauseIndefinitely() {
        viewModelScope.launch {
            pauseHabit(habitId, today, null, reason = "paused")
            refresh()
        }
    }

    fun onResume() {
        viewModelScope.launch {
            resumeHabit(habitId)
            refresh()
        }
    }

    fun onDuplicate(onDuplicated: (Long) -> Unit) {
        viewModelScope.launch {
            when (val result = duplicateHabit(habitId)) {
                is CreateHabitUseCase.Result.Success -> onDuplicated(result.habitId)
                is CreateHabitUseCase.Result.Invalid -> Unit // nothing sensible to show here yet; Phase J adds a snackbar host
            }
        }
    }
}
