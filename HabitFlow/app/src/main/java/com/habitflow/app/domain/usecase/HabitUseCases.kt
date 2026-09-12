package com.habitflow.app.domain.usecase

import com.habitflow.app.data.repository.HabitRepository
import com.habitflow.app.domain.model.DayStatus
import com.habitflow.app.domain.model.Habit
import com.habitflow.app.domain.model.HabitForDate
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

/**
 * Section 28 specifies UI -> ViewModel -> UseCase -> Repository. Phase A of
 * HabitFlow 2.0 closes that gap: ViewModels below now depend on these
 * instead of on HabitRepository directly. Each use case is intentionally
 * thin -- the actual logic still lives in HabitRepository/StreakEngine --
 * this layer exists so a ViewModel's dependencies read as "the actions this
 * screen can perform" rather than "the full repository surface", and so
 * cross-cutting rules (like dependency-cycle checks in Phase B) have an
 * obvious home that isn't the repository or the UI.
 */

class ObserveHabitsScheduledOnUseCase @Inject constructor(
    private val repository: HabitRepository,
) {
    operator fun invoke(date: LocalDate): Flow<List<Habit>> = repository.observeHabitsScheduledOn(date)
}

class ObserveActiveHabitsUseCase @Inject constructor(
    private val repository: HabitRepository,
) {
    operator fun invoke(): Flow<List<Habit>> = repository.observeActiveHabits()
}

class ObserveHabitUseCase @Inject constructor(
    private val repository: HabitRepository,
) {
    operator fun invoke(habitId: Long): Flow<Habit?> = repository.observeHabit(habitId)
}

class GetHabitDayStateUseCase @Inject constructor(
    private val repository: HabitRepository,
) {
    suspend operator fun invoke(habit: Habit, date: LocalDate): HabitForDate = HabitForDate(
        habit = habit,
        date = date,
        loggedAmount = repository.totalLoggedAmount(habit.id, date),
        targetAmount = habit.targetQuantity ?: 1.0,
        status = repository.statusFor(habit.id, date),
        currentStreak = repository.currentStreak(habit.id),
    )
}

class ToggleHabitCompletionUseCase @Inject constructor(
    private val repository: HabitRepository,
) {
    suspend operator fun invoke(habitForDate: HabitForDate) {
        if (habitForDate.status == DayStatus.COMPLETED) {
            repository.clearCompletion(habitForDate.habit.id, habitForDate.date)
        } else {
            repository.logCompletion(habitForDate.habit.id, habitForDate.date, habitForDate.targetAmount)
        }
    }
}

/**
 * Validates and creates a habit. This is the seam where habit-stacking /
 * dependency cycle-prevention (section 10: "Prevent circular dependencies")
 * belongs once that UI exists -- today it only guards against the two
 * checks that are already meaningful (blank name, a habit depending on
 * itself), so Phase B can extend [invoke] rather than re-plumb a new use case.
 */
class CreateHabitUseCase @Inject constructor(
    private val repository: HabitRepository,
) {
    sealed interface Result {
        data class Success(val habitId: Long) : Result
        data class Invalid(val reason: String) : Result
    }

    suspend operator fun invoke(habit: Habit): Result {
        if (habit.name.isBlank()) {
            return Result.Invalid("Habit name can't be empty.")
        }
        if (habit.dependsOnHabitId == habit.id && habit.id != 0L) {
            return Result.Invalid("A habit can't depend on itself.")
        }
        if (habit.stackAfterHabitId == habit.id && habit.id != 0L) {
            return Result.Invalid("A habit can't stack after itself.")
        }
        val id = repository.createHabit(habit)
        return Result.Success(id)
    }
}

class UpdateHabitUseCase @Inject constructor(
    private val repository: HabitRepository,
) {
    suspend operator fun invoke(habit: Habit): CreateHabitUseCase.Result {
        if (habit.name.isBlank()) return CreateHabitUseCase.Result.Invalid("Habit name can't be empty.")
        if (habit.dependsOnHabitId == habit.id) return CreateHabitUseCase.Result.Invalid("A habit can't depend on itself.")
        if (habit.stackAfterHabitId == habit.id) return CreateHabitUseCase.Result.Invalid("A habit can't stack after itself.")
        repository.updateHabit(habit)
        return CreateHabitUseCase.Result.Success(habit.id)
    }
}

class ArchiveHabitUseCase @Inject constructor(
    private val repository: HabitRepository,
) {
    suspend operator fun invoke(habitId: Long) = repository.archiveHabit(habitId)
}

class SetHabitPinnedUseCase @Inject constructor(
    private val repository: HabitRepository,
) {
    suspend operator fun invoke(habitId: Long, pinned: Boolean) = repository.setPinned(habitId, pinned)
}

class PauseHabitUseCase @Inject constructor(
    private val repository: HabitRepository,
) {
    suspend operator fun invoke(habitId: Long, startDate: LocalDate, endDate: LocalDate?, reason: String = "") =
        repository.pauseHabit(habitId, startDate, endDate, reason)
}

class ResumeHabitUseCase @Inject constructor(
    private val repository: HabitRepository,
) {
    suspend operator fun invoke(habitId: Long) = repository.resumeHabit(habitId)
}

/**
 * "Skip today" (section 8) is modeled as a single-day pause rather than a
 * new concept: [HabitRepository.pauseHabit] with startDate == endDate ==
 * today. StreakEngine already treats any pause range as neutral, so this
 * gets correct streak-preserving behavior for free, with no schema change
 * and no new branch in the engine.
 */
class SkipHabitTodayUseCase @Inject constructor(
    private val repository: HabitRepository,
) {
    suspend operator fun invoke(habitId: Long) {
        val today = LocalDate.now()
        repository.pauseHabit(habitId, today, today, reason = "skipped")
    }
}

/**
 * Copies a habit's definition (schedule, value type, color, etc.) into a
 * brand-new habit starting today, per section 8 ("Duplicate"). Deliberately
 * does NOT copy completion history, streaks, pauses, or reminders -- a
 * duplicate is a fresh habit, not a fork of an existing one's progress.
 */
class DuplicateHabitUseCase @Inject constructor(
    private val repository: HabitRepository,
    private val createHabit: CreateHabitUseCase,
) {
    suspend operator fun invoke(habitId: Long): CreateHabitUseCase.Result {
        val source = repository.getHabitOnce(habitId)
            ?: return CreateHabitUseCase.Result.Invalid("Habit not found.")
        val copy = source.copy(
            id = 0,
            name = "${source.name} (copy)",
            startDate = LocalDate.now(),
            endDate = null,
            isArchived = false,
            isPinned = false,
            isFavorite = false,
        )
        return createHabit(copy)
    }
}

class AddHabitReminderUseCase @Inject constructor(
    private val repository: HabitRepository,
) {
    suspend operator fun invoke(habitId: Long, hour: Int, minute: Int, weekdaysMask: Int = 0, message: String? = null): Long =
        repository.addReminder(habitId, hour, minute, weekdaysMask, message)
}

class GetHabitOnceUseCase @Inject constructor(
    private val repository: HabitRepository,
) {
    suspend operator fun invoke(habitId: Long): Habit? = repository.getHabitOnce(habitId)
}
