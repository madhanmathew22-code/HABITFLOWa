package com.habitflow.app.domain.usecase

import com.habitflow.app.data.repository.HabitRepository
import com.habitflow.app.domain.model.DaySummary
import com.habitflow.app.domain.model.DayStatus
import com.habitflow.app.domain.model.HabitForDate
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

/**
 * Builds one [DaySummary] per day of [yearMonth] across every active habit.
 * Efficient by construction: [HabitRepository.statusesForRange] does one
 * completion-history read per habit for the whole month, not one per
 * habit-day, so this is O(habits) database round trips regardless of how
 * many days are in the month.
 *
 * Thin wrapper around [GetDateRangeSummaryUseCase] -- kept as its own class
 * (rather than callers passing a date list themselves) since "a calendar
 * month" is a meaningful, reusable unit on its own (Calendar screen,
 * Dashboard's streak calendar).
 */
class GetCalendarMonthUseCase @Inject constructor(
    private val getDateRangeSummary: GetDateRangeSummaryUseCase,
) {
    suspend operator fun invoke(yearMonth: YearMonth): Map<LocalDate, DaySummary> {
        val dates = (1..yearMonth.lengthOfMonth()).map { yearMonth.atDay(it) }
        return getDateRangeSummary(dates)
    }
}

/**
 * Same aggregation as [GetCalendarMonthUseCase] but for an arbitrary list of
 * dates rather than a whole calendar month -- e.g. the Dashboard's current
 * Mon-Sun week, which can straddle a month boundary that [GetCalendarMonthUseCase]
 * alone can't express.
 */
class GetDateRangeSummaryUseCase @Inject constructor(
    private val repository: HabitRepository,
) {
    suspend operator fun invoke(dates: List<LocalDate>): Map<LocalDate, DaySummary> {
        val habits = repository.getActiveHabitsOnce()

        if (habits.isEmpty()) {
            return dates.associateWith { DaySummary.empty(it) }
        }

        val statusesByHabit = habits.map { habit -> repository.statusesForRange(habit.id, dates) }

        return dates.associateWith { date ->
            var scheduled = 0
            var completed = 0
            var missed = 0
            var paused = 0
            var pending = 0
            statusesByHabit.forEach { statusesForHabit ->
                when (statusesForHabit[date]) {
                    DayStatus.COMPLETED -> { scheduled++; completed++ }
                    DayStatus.MISSED -> { scheduled++; missed++ }
                    DayStatus.PAUSED -> { scheduled++; paused++ }
                    DayStatus.PENDING -> { scheduled++; pending++ }
                    DayStatus.NOT_SCHEDULED, null -> Unit
                }
            }
            DaySummary(date, scheduled, completed, missed, paused, pending)
        }
    }
}

/**
 * Per-habit breakdown for a single selected date (section 12: "Habits,
 * Completions, Missed habits" shown when a date is tapped). Reuses
 * [GetHabitDayStateUseCase] per habit -- cheap here since it's just one
 * date, unlike the whole-month case above.
 */
class GetDayDetailUseCase @Inject constructor(
    private val repository: HabitRepository,
    private val getHabitDayState: GetHabitDayStateUseCase,
) {
    suspend operator fun invoke(date: LocalDate): List<HabitForDate> {
        val habits = repository.getActiveHabitsOnce()
        return habits
            .map { habit -> getHabitDayState(habit, date) }
            .filter { it.status != DayStatus.NOT_SCHEDULED }
    }
}
