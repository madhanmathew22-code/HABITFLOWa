package com.habitflow.app.ui.journal.compose

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habitflow.app.domain.model.MoodLevel
import com.habitflow.app.domain.usecase.CreateJournalEntryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class JournalComposeUiState(
    val title: String = "",
    val body: String = "",
    val mood: MoodLevel? = null,
    /**
     * URI strings from the system Photo Picker
     * ([androidx.activity.result.contract.ActivityResultContracts.PickMultipleVisualMedia]).
     * These are stored as-is: unlike `ACTION_OPEN_DOCUMENT` URIs, Photo
     * Picker URIs don't support (and don't need) `takePersistableUriPermission`
     * -- Android grants read access to picked media across app restarts
     * automatically, and calling that API on a Photo Picker URI can throw.
     */
    val photoUris: List<String> = emptyList(),
    val errorMessage: String? = null,
    val saved: Boolean = false,
)

@HiltViewModel
class JournalComposeViewModel @Inject constructor(
    private val createJournalEntry: CreateJournalEntryUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(JournalComposeUiState())
    val uiState: StateFlow<JournalComposeUiState> = _uiState.asStateFlow()

    fun onTitleChange(value: String) {
        _uiState.value = _uiState.value.copy(title = value)
    }

    fun onBodyChange(value: String) {
        _uiState.value = _uiState.value.copy(body = value)
    }

    fun onMoodSelected(mood: MoodLevel) {
        _uiState.value = _uiState.value.copy(mood = if (_uiState.value.mood == mood) null else mood)
    }

    fun onPhotosPicked(uris: List<String>) {
        _uiState.value = _uiState.value.copy(photoUris = _uiState.value.photoUris + uris)
    }

    fun onRemovePhoto(uri: String) {
        _uiState.value = _uiState.value.copy(photoUris = _uiState.value.photoUris - uri)
    }

    fun save() {
        val state = _uiState.value
        viewModelScope.launch {
            when (
                val result = createJournalEntry(
                    date = LocalDate.now(),
                    title = state.title,
                    body = state.body,
                    moodLevel = state.mood,
                    photoUris = state.photoUris,
                )
            ) {
                is CreateJournalEntryUseCase.Result.Success -> _uiState.value = state.copy(saved = true)
                is CreateJournalEntryUseCase.Result.Invalid -> _uiState.value = state.copy(errorMessage = result.reason)
            }
        }
    }
}
