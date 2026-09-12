package com.habitflow.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.habitflow.app.data.local.entity.HabitEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface HabitDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(habit: HabitEntity): Long

    @Update
    suspend fun update(habit: HabitEntity)

    @Delete
    suspend fun delete(habit: HabitEntity)

    @Query("UPDATE habits SET isArchived = 1 WHERE id = :habitId")
    suspend fun archive(habitId: Long)

    @Query("UPDATE habits SET isArchived = 0 WHERE id = :habitId")
    suspend fun unarchive(habitId: Long)

    @Query("UPDATE habits SET isPinned = :pinned WHERE id = :habitId")
    suspend fun setPinned(habitId: Long, pinned: Boolean)

    @Query("UPDATE habits SET isFavorite = :favorite WHERE id = :habitId")
    suspend fun setFavorite(habitId: Long, favorite: Boolean)

    @Query("SELECT * FROM habits WHERE id = :habitId")
    fun observeById(habitId: Long): Flow<HabitEntity?>

    @Query("SELECT * FROM habits WHERE id = :habitId")
    suspend fun getById(habitId: Long): HabitEntity?

    @Query(
        """
        SELECT * FROM habits
        WHERE isArchived = 0
        ORDER BY isPinned DESC, sortOrder ASC, name ASC
        """
    )
    fun observeActiveHabits(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE isArchived = 1 ORDER BY name ASC")
    fun observeArchivedHabits(): Flow<List<HabitEntity>>

    @Query(
        """
        SELECT * FROM habits
        WHERE isArchived = 0
          AND startDate <= :date
          AND (endDate IS NULL OR endDate >= :date)
        ORDER BY isPinned DESC, sortOrder ASC, name ASC
        """
    )
    fun observeHabitsActiveOn(date: LocalDate): Flow<List<HabitEntity>>

    @Query(
        """
        SELECT * FROM habits
        WHERE isArchived = 0 AND (name LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%')
        """
    )
    fun search(query: String): Flow<List<HabitEntity>>
}
