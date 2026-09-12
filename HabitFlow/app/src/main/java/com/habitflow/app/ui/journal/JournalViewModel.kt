package com.habitflow.app.ui.journal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habitflow.app.domain.model.JournalEntry
import com.habitflow.app.domain.model.MoodLevel
import com.habitflow.app.domain.usecase.DeleteJournalEntryUseCase
import com.habitflow.app.domain.usecase.ObserveJournalTimelineUseCase
import com.habitflow.app.domain.usecase.ObserveMoodRangeUseCase
import com.habitflow.app.domain.usecase.SetMoodForDateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class JournalUiState(
    val entries: List<JournalEntry> = emptyList(),
    val last7DaysMood: Map<LocalDate, MoodLevel> = emptyMap(),
    val isLoading: Boolean = true,
) {
    val todayMood: MoodLevel? get() = last7DaysMood[LocalDate.now()]
}

@HiltViewModel
class JournalViewModel @Inject constructor(
    observeJournalTimeline: ObserveJournalTimelineUseCase,
    observeMoodRange: ObserveMoodRangeUseCase,
    private val setMoodForDate: SetMoodForDateUseCase,
    private val deleteJournalEntry: DeleteJournalEntryUseCase,
) : ViewModel() {

    private val today = LocalDate.now()

    val uiState: StateFlow<JournalUiState> = combine(
        observeJournalTimeline(),
        observeMoodRange(today.minusDays(6), today),
    ) { entries, moodDays ->
        JournalUiState(
            entries = entries,
            last7DaysMood = moodDays.associate { it.date to it.moodLevel },
            isLoading = false,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), JournalUiState())

    fun onSetTodayMood(mood: MoodLevel) {
        viewModelScope.launch { setMoodForDate(today, mood) }
    }

    fun onDeleteEntry(entryId: Long) {
        viewModelScope.launch { deleteJournalEntry(entryId) }
    }
}
