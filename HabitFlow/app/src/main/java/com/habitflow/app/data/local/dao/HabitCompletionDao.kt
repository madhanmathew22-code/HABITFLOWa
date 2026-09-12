package com.habitflow.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.habitflow.app.data.local.entity.HabitCompletionEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface HabitCompletionDao {

    @Insert
    suspend fun insert(completion: HabitCompletionEntity): Long

    @Delete
    suspend fun delete(completion: HabitCompletionEntity)

    @Query("DELETE FROM habit_completions WHERE habitId = :habitId AND date = :date")
    suspend fun deleteAllForHabitOnDate(habitId: Long, date: LocalDate)

    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId ORDER BY date ASC")
    fun observeForHabit(habitId: Long): Flow<List<HabitCompletionEntity>>

    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId AND date = :date")
    fun observeForHabitOnDate(habitId: Long, date: LocalDate): Flow<List<HabitCompletionEntity>>

    @Query(
        "SELECT * FROM habit_completions WHERE date BETWEEN :startInclusive AND :endInclusive ORDER BY date ASC"
    )
    fun observeInRange(startInclusive: LocalDate, endInclusive: LocalDate): Flow<List<HabitCompletionEntity>>

    @Query(
        "SELECT COALESCE(SUM(amount), 0.0) FROM habit_completions WHERE habitId = :habitId AND date = :date"
    )
    suspend fun totalAmountForHabitOnDate(habitId: Long, date: LocalDate): Double

    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId ORDER BY date ASC")
    suspend fun getAllForHabit(habitId: Long): List<HabitCompletionEntity>
}
