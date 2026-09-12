package com.habitflow.app.data.repository

import com.habitflow.app.data.local.dao.HabitCompletionDao
import com.habitflow.app.data.local.dao.HabitDao
import com.habitflow.app.data.local.dao.HabitPauseDao
import com.habitflow.app.data.local.dao.HabitReminderDao
import com.habitflow.app.data.local.entity.HabitCompletionEntity
import com.habitflow.app.data.local.entity.HabitPauseEntity
import com.habitflow.app.data.local.entity.HabitReminderEntity
import com.habitflow.app.domain.model.DayStatus
import com.habitflow.app.domain.model.Habit
import com.habitflow.app.domain.streak.StreakEngine
import com.habitflow.app.domain.streak.StreakInput
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HabitRepositoryImpl @Inject constructor(
    private val habitDao: HabitDao,
    private val completionDao: HabitCompletionDao,
    private val pauseDao: HabitPauseDao,
    private val reminderDao: HabitReminderDao,
    private val streakEngine: StreakEngine,
) : HabitRepository {

    override fun observeActiveHabits(): Flow<List<Habit>> =
        habitDao.observeActiveHabits().map { list -> list.map { it.toDomain() } }

    override fun observeHabitsScheduledOn(date: LocalDate): Flow<List<Habit>> =
        habitDao.observeHabitsActiveOn(date).map { list -> list.map { it.toDomain() } }

    override fun observeHabit(habitId: Long): Flow<Habit?> =
        habitDao.observeById(habitId).map { it?.toDomain() }

    override suspend fun createHabit(habit: Habit): Long = habitDao.insert(habit.toEntity())

    override suspend fun updateHabit(habit: Habit) = habitDao.update(habit.toEntity())

    override suspend fun archiveHabit(habitId: Long) = habitDao.archive(habitId)

    override suspend fun setPinned(habitId: Long, pinned: Boolean) = habitDao.setPinned(habitId, pinned)

    override suspend fun logCompletion(habitId: Long, date: LocalDate, amount: Double) {
        completionDao.insert(HabitCompletionEntity(habitId = habitId, date = date, amount = amount))
    }

    override suspend fun clearCompletion(habitId: Long, date: LocalDate) {
        completionDao.deleteAllForHabitOnDate(habitId, date)
    }

    override suspend fun totalLoggedAmount(habitId: Long, date: LocalDate): Double =
        completionDao.totalAmountForHabitOnDate(habitId, date)

    override suspend fun statusFor(habitId: Long, date: LocalDate): DayStatus {
        val input = buildStreakInput(habitId) ?: return DayStatus.NOT_SCHEDULED
        return streakEngine.statusFor(input, date)
    }

    override suspend fun currentStreak(habitId: Long): Int {
        val input = buildStreakInput(habitId) ?: return 0
        return streakEngine.evaluate(input).currentStreak
    }

    override suspend fun pauseHabit(habitId: Long, startDate: LocalDate, endDate: LocalDate?, reason: String) {
        pauseDao.insert(HabitPauseEntity(habitId = habitId, startDate = startDate, endDate = endDate, reason = reason))
    }

    override suspend fun resumeHabit(habitId: Long) {
        val today = LocalDate.now()
        val active = pauseDao.activePauseOn(habitId, today) ?: return
        pauseDao.update(active.copy(endDate = today.minusDays(1)))
    }

    override suspend fun addReminder(habitId: Long, hour: Int, minute: Int, weekdaysMask: Int, message: String?): Long =
        reminderDao.insert(
            HabitReminderEntity(
                habitId = habitId,
                hour = hour,
                minute = minute,
                weekdaysMask = weekdaysMask,
                customMessage = message,
            ),
        )

    override suspend fun getHabitOnce(habitId: Long): Habit? = habitDao.getById(habitId)?.toDomain()

    override suspend fun getActiveHabitsOnce(): List<Habit> =
        habitDao.observeActiveHabits().first().map { it.toDomain() }

    override suspend fun statusesForRange(habitId: Long, dates: List<LocalDate>): Map<LocalDate, DayStatus> {
        val input = buildStreakInput(habitId) ?: return dates.associateWith { DayStatus.NOT_SCHEDULED }
        return dates.associateWith { date -> streakEngine.statusFor(input, date) }
    }

    /** Assembles a [StreakInput] for [habitId] from its current DB state. */
    private suspend fun buildStreakInput(habitId: Long): StreakInput? {
        val habit = habitDao.getById(habitId) ?: return null
        val completions = completionDao.getAllForHabit(habitId)
        val completedAmountByDate = completions
            .groupBy { it.date }
            .mapValues { (_, rows) -> rows.sumOf { it.amount } }
        val pauses = pauseDao.getAllForHabit(habitId)
            .map { it.startDate..(it.endDate ?: LocalDate.now()) }

        return StreakInput(
            startDate = habit.startDate,
            endDate = habit.endDate,
            frequencyType = habit.frequencyType,
            weekdaysMask = habit.weekdaysMask,
            timesPerWeek = habit.timesPerWeek,
            timesPerMonth = habit.timesPerMonth,
            everyNDays = habit.everyNDays,
            flexibleWindowDays = habit.flexibleWindowDays,
            completedAmountByDate = completedAmountByDate,
            pauseRanges = pauses,
            dailyTargetAmount = habit.targetQuantity ?: 1.0,
        )
    }
}
