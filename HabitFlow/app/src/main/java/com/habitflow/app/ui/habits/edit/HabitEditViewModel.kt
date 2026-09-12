package com.habitflow.app.ui.habits.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habitflow.app.domain.model.Habit
import com.habitflow.app.domain.usecase.CreateHabitUseCase
import com.habitflow.app.domain.usecase.GetHabitOnceUseCase
import com.habitflow.app.domain.usecase.UpdateHabitUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HabitEditUiState(
    val habit: Habit? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val saved: Boolean = false,
)

@HiltViewModel
class HabitEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getHabitOnce: GetHabitOnceUseCase,
    private val updateHabit: UpdateHabitUseCase,
) : ViewModel() {

    private val habitId: Long = checkNotNull(savedStateHandle.get<Long>("habitId")) {
        "HabitEditScreen requires a habitId nav argument"
    }

    private val _uiState = MutableStateFlow(HabitEditUiState())
    val uiState: StateFlow<HabitEditUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val habit = getHabitOnce(habitId)
            _uiState.value = HabitEditUiState(habit = habit, isLoading = false)
        }
    }

    fun save(updated: Habit) {
        viewModelScope.launch {
            when (val result = updateHabit(updated)) {
                is CreateHabitUseCase.Result.Success -> _uiState.value = _uiState.value.copy(saved = true, errorMessage = null)
                is CreateHabitUseCase.Result.Invalid -> _uiState.value = _uiState.value.copy(errorMessage = result.reason)
            }
        }
    }
}
