package com.habitflow.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.habitflow.app.data.local.entity.CategoryEntity
import com.habitflow.app.data.local.entity.HabitPauseEntity
import com.habitflow.app.data.local.entity.HabitReminderEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface HabitPauseDao {
    @Insert
    suspend fun insert(pause: HabitPauseEntity): Long

    @Update
    suspend fun update(pause: HabitPauseEntity)

    @Query("SELECT * FROM habit_pauses WHERE habitId = :habitId ORDER BY startDate ASC")
    fun observeForHabit(habitId: Long): Flow<List<HabitPauseEntity>>

    @Query("SELECT * FROM habit_pauses WHERE habitId = :habitId ORDER BY startDate ASC")
    suspend fun getAllForHabit(habitId: Long): List<HabitPauseEntity>

    @Query(
        """
        SELECT * FROM habit_pauses
        WHERE habitId = :habitId AND startDate <= :date AND (endDate IS NULL OR endDate >= :date)
        LIMIT 1
        """
    )
    suspend fun activePauseOn(habitId: Long, date: LocalDate): HabitPauseEntity?
}

@Dao
interface HabitReminderDao {
    @Insert
    suspend fun insert(reminder: HabitReminderEntity): Long

    @Update
    suspend fun update(reminder: HabitReminderEntity)

    @Query("DELETE FROM habit_reminders WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM habit_reminders WHERE habitId = :habitId")
    fun observeForHabit(habitId: Long): Flow<List<HabitReminderEntity>>

    @Query("SELECT * FROM habit_reminders WHERE isEnabled = 1")
    suspend fun getAllEnabled(): List<HabitReminderEntity>
}

@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(categories: List<CategoryEntity>)

    @Insert
    suspend fun insert(category: CategoryEntity): Long

    @Update
    suspend fun update(category: CategoryEntity)

    @Query("SELECT * FROM categories ORDER BY sortOrder ASC, name ASC")
    fun observeAll(): Flow<List<CategoryEntity>>

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun count(): Int
}
