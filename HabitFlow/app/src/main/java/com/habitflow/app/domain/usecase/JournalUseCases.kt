package com.habitflow.app.domain.usecase

import com.habitflow.app.data.repository.JournalRepository
import com.habitflow.app.domain.model.JournalEntry
import com.habitflow.app.domain.model.MoodDay
import com.habitflow.app.domain.model.MoodLevel
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

class ObserveJournalTimelineUseCase @Inject constructor(
    private val repository: JournalRepository,
) {
    operator fun invoke(): Flow<List<JournalEntry>> = repository.observeTimeline()
}

class ObserveJournalForDateUseCase @Inject constructor(
    private val repository: JournalRepository,
) {
    operator fun invoke(date: LocalDate): Flow<List<JournalEntry>> = repository.observeForDate(date)
}

class CreateJournalEntryUseCase @Inject constructor(
    private val repository: JournalRepository,
) {
    sealed interface Result {
        data class Success(val entryId: Long) : Result
        data class Invalid(val reason: String) : Result
    }

    suspend operator fun invoke(
        date: LocalDate,
        title: String,
        body: String,
        moodLevel: MoodLevel?,
        photoUris: List<String> = emptyList(),
        linkedHabitIds: List<Long> = emptyList(),
    ): Result {
        if (title.isBlank() && body.isBlank()) {
            return Result.Invalid("Write something before saving.")
        }
        val id = repository.createEntry(date, title.trim(), body.trim(), moodLevel, photoUris, linkedHabitIds)
        return Result.Success(id)
    }
}

class DeleteJournalEntryUseCase @Inject constructor(
    private val repository: JournalRepository,
) {
    suspend operator fun invoke(entryId: Long) = repository.deleteEntry(entryId)
}

class AddJournalPhotoUseCase @Inject constructor(
    private val repository: JournalRepository,
) {
    suspend operator fun invoke(entryId: Long, uri: String) = repository.addPhoto(entryId, uri)
}

class SetMoodForDateUseCase @Inject constructor(
    private val repository: JournalRepository,
) {
    suspend operator fun invoke(date: LocalDate, moodLevel: MoodLevel) = repository.setMoodForDate(date, moodLevel)
}

class GetMoodForDateUseCase @Inject constructor(
    private val repository: JournalRepository,
) {
    suspend operator fun invoke(date: LocalDate): MoodLevel? = repository.getMoodForDate(date)
}

class ObserveMoodRangeUseCase @Inject constructor(
    private val repository: JournalRepository,
) {
    operator fun invoke(start: LocalDate, end: LocalDate): Flow<List<MoodDay>> = repository.observeMoodRange(start, end)
}

class SearchJournalUseCase @Inject constructor(
    private val repository: JournalRepository,
) {
    operator fun invoke(query: String): Flow<List<JournalEntry>> = repository.search(query)
}
