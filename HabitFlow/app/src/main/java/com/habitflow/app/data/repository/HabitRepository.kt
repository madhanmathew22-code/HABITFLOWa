package com.habitflow.app.data.repository

import com.habitflow.app.domain.model.DayStatus
import com.habitflow.app.domain.model.Habit
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface HabitRepository {

    fun observeActiveHabits(): Flow<List<Habit>>

    fun observeHabitsScheduledOn(date: LocalDate): Flow<List<Habit>>

    fun observeHabit(habitId: Long): Flow<Habit?>

    suspend fun createHabit(habit: Habit): Long

    suspend fun updateHabit(habit: Habit)

    suspend fun archiveHabit(habitId: Long)

    suspend fun setPinned(habitId: Long, pinned: Boolean)

    /**
     * Records (or increments) a completion for [habitId] on [date].
     * For BOOLEAN habits this is called once per day; for QUANTITY/DURATION
     * habits it can be called multiple times to accumulate toward the goal.
     */
    suspend fun logCompletion(habitId: Long, date: LocalDate, amount: Double = 1.0)

    /** Removes today's completion(s) for [habitId] on [date] — the "undo" action on a habit card. */
    suspend fun clearCompletion(habitId: Long, date: LocalDate)

    suspend fun totalLoggedAmount(habitId: Long, date: LocalDate): Double

    /** Resolved calendar status for one habit on one date (section 12). */
    suspend fun statusFor(habitId: Long, date: LocalDate): DayStatus

    suspend fun currentStreak(habitId: Long): Int

    suspend fun pauseHabit(habitId: Long, startDate: LocalDate, endDate: LocalDate?, reason: String = "")

    suspend fun resumeHabit(habitId: Long)

    /** Section 19: adds a reminder row for [habitId]. Scheduling the actual notification is Phase H. */
    suspend fun addReminder(habitId: Long, hour: Int, minute: Int, weekdaysMask: Int, message: String?): Long

    suspend fun getHabitOnce(habitId: Long): Habit?

    suspend fun getActiveHabitsOnce(): List<Habit>

    /**
     * Resolves [DayStatus] for every date in [dates] for one habit in a
     * single pass -- builds the habit's [com.habitflow.app.domain.streak.StreakInput]
     * once (one completion-history read) rather than once per date, which
     * matters once the Calendar screen (Phase C) asks for a whole month at
     * once across every habit.
     */
    suspend fun statusesForRange(habitId: Long, dates: List<LocalDate>): Map<LocalDate, DayStatus>
}
