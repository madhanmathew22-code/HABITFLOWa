package com.habitflow.app.data.repository

import com.habitflow.app.data.local.dao.JournalDao
import com.habitflow.app.data.local.dao.MoodDao
import com.habitflow.app.data.local.entity.JournalEntryEntity
import com.habitflow.app.data.local.entity.JournalHabitCrossRef
import com.habitflow.app.data.local.entity.JournalMediaEntity
import com.habitflow.app.data.local.entity.MoodEntryEntity
import com.habitflow.app.domain.model.JournalEntry
import com.habitflow.app.domain.model.MoodDay
import com.habitflow.app.domain.model.MoodLevel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JournalRepositoryImpl @Inject constructor(
    private val journalDao: JournalDao,
    private val moodDao: MoodDao,
) : JournalRepository {

    override fun observeTimeline(): Flow<List<JournalEntry>> =
        journalDao.observeAll().map { entities -> entities.map { toDomain(it) } }

    override fun observeForDate(date: LocalDate): Flow<List<JournalEntry>> =
        journalDao.observeForDate(date).map { entities -> entities.map { toDomain(it) } }

    override suspend fun createEntry(
        date: LocalDate,
        title: String,
        body: String,
        moodLevel: MoodLevel?,
        photoUris: List<String>,
        linkedHabitIds: List<Long>,
    ): Long {
        val entryId = journalDao.insertEntry(
            JournalEntryEntity(
                date = date,
                title = title,
                body = body,
                moodLevelOrdinal = moodLevel?.ordinal,
            ),
        )
        photoUris.forEachIndexed { index, uri ->
            journalDao.insertMedia(JournalMediaEntity(journalEntryId = entryId, uri = uri, sortOrder = index))
        }
        linkedHabitIds.forEach { habitId ->
            journalDao.linkHabit(JournalHabitCrossRef(journalEntryId = entryId, habitId = habitId))
        }
        return entryId
    }

    override suspend fun deleteEntry(entryId: Long) {
        // JournalMediaEntity/JournalHabitCrossRef both cascade-delete via
        // their foreign keys, so deleting just the entry row is sufficient.
        journalDao.deleteById(entryId)
    }

    override suspend fun addPhoto(entryId: Long, uri: String) {
        journalDao.insertMedia(JournalMediaEntity(journalEntryId = entryId, uri = uri))
    }

    override suspend fun setMoodForDate(date: LocalDate, moodLevel: MoodLevel) {
        moodDao.upsert(MoodEntryEntity(date = date, moodLevelOrdinal = moodLevel.ordinal))
    }

    override suspend fun getMoodForDate(date: LocalDate): MoodLevel? =
        moodDao.getForDate(date)?.let { MoodLevel.entries[it.moodLevelOrdinal] }

    override fun observeMoodRange(start: LocalDate, end: LocalDate): Flow<List<MoodDay>> =
        moodDao.observeInRange(start, end).map { entities ->
            entities.map { MoodDay(it.date, MoodLevel.entries[it.moodLevelOrdinal]) }
        }

    override fun search(query: String): Flow<List<JournalEntry>> =
        journalDao.search(query).map { entities -> entities.map { toDomain(it) } }

    private suspend fun toDomain(entity: JournalEntryEntity): JournalEntry {
        val media = journalDao.observeMediaForEntry(entity.id).first()
        return JournalEntry(
            id = entity.id,
            date = entity.date,
            title = entity.title,
            body = entity.body,
            moodLevel = entity.moodLevelOrdinal?.let { MoodLevel.entries[it] },
            photoUris = media.sortedBy { it.sortOrder }.map { it.uri },
            createdAtEpochMillis = entity.createdAtEpochMillis,
        )
    }
}
