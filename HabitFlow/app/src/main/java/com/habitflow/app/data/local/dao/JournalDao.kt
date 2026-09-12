package com.habitflow.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.habitflow.app.data.local.entity.JournalEntryEntity
import com.habitflow.app.data.local.entity.JournalHabitCrossRef
import com.habitflow.app.data.local.entity.JournalMediaEntity
import com.habitflow.app.data.local.entity.MoodEntryEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface JournalDao {

    @Insert
    suspend fun insertEntry(entry: JournalEntryEntity): Long

    @Update
    suspend fun updateEntry(entry: JournalEntryEntity)

    @Delete
    suspend fun deleteEntry(entry: JournalEntryEntity)

    @Query("SELECT * FROM journal_entries WHERE id = :entryId")
    suspend fun getById(entryId: Long): JournalEntryEntity?

    @Query("DELETE FROM journal_entries WHERE id = :entryId")
    suspend fun deleteById(entryId: Long)

    @Insert
    suspend fun insertMedia(media: JournalMediaEntity): Long

    @Query("DELETE FROM journal_media WHERE id = :mediaId")
    suspend fun deleteMedia(mediaId: Long)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun linkHabit(crossRef: JournalHabitCrossRef)

    @Query("SELECT * FROM journal_entries ORDER BY date DESC, id DESC")
    fun observeAll(): Flow<List<JournalEntryEntity>>

    @Query("SELECT * FROM journal_entries WHERE date = :date ORDER BY id DESC")
    fun observeForDate(date: LocalDate): Flow<List<JournalEntryEntity>>

    @Query("SELECT * FROM journal_media WHERE journalEntryId = :entryId ORDER BY sortOrder ASC")
    fun observeMediaForEntry(entryId: Long): Flow<List<JournalMediaEntity>>

    @Query(
        """
        SELECT * FROM journal_entries
        WHERE title LIKE '%' || :query || '%' OR body LIKE '%' || :query || '%'
        ORDER BY date DESC
        """
    )
    fun search(query: String): Flow<List<JournalEntryEntity>>
}

@Dao
interface MoodDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(mood: MoodEntryEntity)

    @Query("SELECT * FROM mood_entries WHERE date = :date")
    suspend fun getForDate(date: LocalDate): MoodEntryEntity?

    @Query("SELECT * FROM mood_entries WHERE date BETWEEN :start AND :end ORDER BY date ASC")
    fun observeInRange(start: LocalDate, end: LocalDate): Flow<List<MoodEntryEntity>>
}
