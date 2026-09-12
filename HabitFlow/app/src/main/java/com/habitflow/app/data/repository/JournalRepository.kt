package com.habitflow.app.data.repository

import com.habitflow.app.domain.model.JournalEntry
import com.habitflow.app.domain.model.MoodDay
import com.habitflow.app.domain.model.MoodLevel
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface JournalRepository {

    /**
     * The full timeline, newest first. Each entry's [JournalEntry.photoUris]
     * is resolved eagerly (one extra query per entry) rather than lazily per
     * row composable -- simpler to reason about for a personal journal's
     * realistic data volume, and avoids a Flow-of-Flows shape in the UI layer.
     */
    fun observeTimeline(): Flow<List<JournalEntry>>

    fun observeForDate(date: LocalDate): Flow<List<JournalEntry>>

    suspend fun createEntry(
        date: LocalDate,
        title: String,
        body: String,
        moodLevel: MoodLevel?,
        photoUris: List<String>,
        linkedHabitIds: List<Long>,
    ): Long

    suspend fun deleteEntry(entryId: Long)

    suspend fun addPhoto(entryId: Long, uri: String)

    suspend fun setMoodForDate(date: LocalDate, moodLevel: MoodLevel)

    suspend fun getMoodForDate(date: LocalDate): MoodLevel?

    fun observeMoodRange(start: LocalDate, end: LocalDate): Flow<List<MoodDay>>

    fun search(query: String): Flow<List<JournalEntry>>
}
