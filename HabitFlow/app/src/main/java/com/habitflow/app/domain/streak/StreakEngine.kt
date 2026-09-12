package com.habitflow.app.domain.streak

import com.habitflow.app.domain.model.DayStatus
import com.habitflow.app.domain.model.FrequencyType
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.time.temporal.WeekFields

/**
 * Immutable inputs the streak engine needs about a single habit. Deliberately
 * decoupled from the Room entities so this whole file has zero Android
 * dependencies and can be unit tested as plain JVM code.
 */
data class StreakInput(
    val startDate: LocalDate,
    val endDate: LocalDate?,
    val frequencyType: FrequencyType,
    /** Bit 0 = Monday .. bit 6 = Sunday. Only used for SPECIFIC_WEEKDAYS. */
    val weekdaysMask: Int = 0,
    val timesPerWeek: Int? = null,
    val timesPerMonth: Int? = null,
    val everyNDays: Int? = null,
    val flexibleWindowDays: Int? = null,
    /** date -> total amount logged that day (sum of all completions). */
    val completedAmountByDate: Map<LocalDate, Double>,
    /** Inclusive pause ranges; a null end means "still paused". */
    val pauseRanges: List<ClosedRange<LocalDate>>,
    /** A day counts as "done" once its logged amount reaches this. 1.0 for boolean habits. */
    val dailyTargetAmount: Double = 1.0,
)

data class StreakResult(
    val currentStreak: Int,
    val bestStreak: Int,
    val totalCompletions: Int,
    val missedDays: Int,
    val skippedDays: Int,
    val pausedDays: Int,
    val completionRate: Double,
    val perfectWeeks: Int,
    val perfectMonths: Int,
)

/**
 * Computes streaks and consistency stats for a single habit.
 *
 * The core rule (section 11): the engine never just counts consecutive
 * calendar dates. It only evaluates *scheduled* days — a day the habit
 * wasn't scheduled on (wrong weekday, outside a "times per week" habit's
 * flexible days, before startDate/after endDate) is skipped entirely and
 * neither helps nor hurts the streak. Paused days are likewise neutral.
 * Only a scheduled, non-paused day with no completion breaks the streak.
 */
class StreakEngine {

    fun evaluate(input: StreakInput, today: LocalDate = LocalDate.now()): StreakResult {
        val lastDay = minOf(input.endDate ?: today, today)
        if (input.startDate.isAfter(lastDay)) {
            return StreakResult(0, 0, 0, 0, 0, 0, 0.0, 0, 0)
        }

        return when (input.frequencyType) {
            FrequencyType.DAILY,
            FrequencyType.SPECIFIC_WEEKDAYS,
            FrequencyType.EVERY_N_DAYS,
            FrequencyType.FLEXIBLE,
            -> evaluateDayBased(input, lastDay)

            FrequencyType.TIMES_PER_WEEK -> evaluatePeriodBased(
                input = input,
                lastDay = lastDay,
                periodOf = { date -> weekPeriodKey(date) },
                periodStart = { key -> weekPeriodStart(key) },
                target = input.timesPerWeek ?: 1,
                isPerfectPeriod = { completions, target -> completions >= target },
            )

            FrequencyType.TIMES_PER_MONTH -> evaluatePeriodBased(
                input = input,
                lastDay = lastDay,
                periodOf = { date -> date.year * 100 + date.monthValue },
                periodStart = { key -> LocalDate.of(key / 100, key % 100, 1) },
                target = input.timesPerMonth ?: 1,
                isPerfectPeriod = { completions, target -> completions >= target },
            )
        }
    }

    /** The per-date status used to render the calendar (section 12). */
    fun statusFor(input: StreakInput, date: LocalDate, today: LocalDate = LocalDate.now()): DayStatus {
        if (date.isAfter(today)) return if (isScheduled(input, date)) DayStatus.PENDING else DayStatus.NOT_SCHEDULED
        if (isPaused(input, date)) return DayStatus.PAUSED
        if (!isScheduled(input, date)) return DayStatus.NOT_SCHEDULED
        val amount = input.completedAmountByDate[date] ?: 0.0
        return if (amount >= input.dailyTargetAmount) {
            DayStatus.COMPLETED
        } else if (date.isEqual(today)) {
            DayStatus.PENDING
        } else {
            DayStatus.MISSED
        }
    }

    // ---- day-based frequencies (DAILY / SPECIFIC_WEEKDAYS / EVERY_N_DAYS / FLEXIBLE) ----

    private fun evaluateDayBased(input: StreakInput, lastDay: LocalDate): StreakResult {
        var current = 0
        var best = 0
        var running = 0
        var total = 0
        var missed = 0
        var paused = 0
        var scheduledCount = 0
        var completedScheduledCount = 0

        var date = input.startDate
        while (!date.isAfter(lastDay)) {
            when {
                isPaused(input, date) -> {
                    paused++
                    // Neutral: does not break, does not extend.
                }
                !isScheduled(input, date) -> {
                    // Not scheduled at all: also neutral.
                }
                else -> {
                    scheduledCount++
                    val amount = input.completedAmountByDate[date] ?: 0.0
                    if (amount >= input.dailyTargetAmount) {
                        running++
                        completedScheduledCount++
                        total++
                        best = maxOf(best, running)
                    } else if (date.isBefore(LocalDate.now())) {
                        missed++
                        running = 0
                    }
                    // If date == today and not yet completed, don't break the
                    // streak yet -- the day isn't over.
                }
            }
            date = date.plusDays(1)
        }
        current = running

        val completionRate = if (scheduledCount == 0) 0.0 else completedScheduledCount.toDouble() / scheduledCount

        return StreakResult(
            currentStreak = current,
            bestStreak = best,
            totalCompletions = total,
            missedDays = missed,
            skippedDays = 0,
            pausedDays = paused,
            completionRate = completionRate,
            perfectWeeks = countPerfectWeeks(input, lastDay),
            perfectMonths = countPerfectMonths(input, lastDay),
        )
    }

    private fun isScheduled(input: StreakInput, date: LocalDate): Boolean {
        if (date.isBefore(input.startDate)) return false
        if (input.endDate != null && date.isAfter(input.endDate)) return false

        return when (input.frequencyType) {
            FrequencyType.DAILY -> true
            FrequencyType.SPECIFIC_WEEKDAYS -> {
                val bit = date.dayOfWeek.value - 1 // Monday = 0
                (input.weekdaysMask shr bit) and 1 == 1
            }
            FrequencyType.EVERY_N_DAYS -> {
                val n = input.everyNDays?.takeIf { it > 0 } ?: 1
                ChronoUnit.DAYS.between(input.startDate, date) % n == 0L
            }
            FrequencyType.FLEXIBLE -> true // flexible habits are "schedulable" every day within their window
            FrequencyType.TIMES_PER_WEEK, FrequencyType.TIMES_PER_MONTH -> true
        }
    }

    private fun isPaused(input: StreakInput, date: LocalDate): Boolean =
        input.pauseRanges.any { range -> !date.isBefore(range.start) && !date.isAfter(range.endInclusive) }

    // ---- period-based frequencies (TIMES_PER_WEEK / TIMES_PER_MONTH) ----

    private fun evaluatePeriodBased(
        input: StreakInput,
        lastDay: LocalDate,
        periodOf: (LocalDate) -> Int,
        periodStart: (Int) -> LocalDate,
        target: Int,
        isPerfectPeriod: (completions: Int, target: Int) -> Boolean,
    ): StreakResult {
        // Group non-paused days from startDate..lastDay by period, summing
        // completions per period, then walk periods oldest->newest.
        val completionsByPeriod = linkedMapOf<Int, Int>()
        val pausedDaysTotal = mutableSetOf<LocalDate>()

        var date = input.startDate
        while (!date.isAfter(lastDay)) {
            if (isPaused(input, date)) {
                pausedDaysTotal += date
            } else {
                val key = periodOf(date)
                val amount = input.completedAmountByDate[date] ?: 0.0
                if (amount >= input.dailyTargetAmount) {
                    completionsByPeriod[key] = (completionsByPeriod[key] ?: 0) + 1
                } else {
                    completionsByPeriod.putIfAbsent(key, completionsByPeriod[key] ?: 0)
                }
            }
            date = date.plusDays(1)
        }

        val orderedPeriods = completionsByPeriod.keys.sorted()
        val today = LocalDate.now()
        var running = 0
        var best = 0
        var missedPeriods = 0
        var total = 0
        var perfectPeriodCount = 0

        for ((index, periodKey) in orderedPeriods.withIndex()) {
            val completions = completionsByPeriod[periodKey] ?: 0
            val isCurrentUnfinishedPeriod = index == orderedPeriods.lastIndex && !periodHasEnded(periodStart(periodKey), periodOf, today)

            if (isPerfectPeriod(completions, target)) {
                running++
                total += completions
                perfectPeriodCount++
                best = maxOf(best, running)
            } else if (!isCurrentUnfinishedPeriod) {
                missedPeriods++
                running = 0
                total += completions
            }
            // else: current in-progress period that hasn't hit target yet
            // doesn't break the streak until the period actually ends.
        }

        val completionRate = if (orderedPeriods.isEmpty()) 0.0 else perfectPeriodCount.toDouble() / orderedPeriods.size

        return StreakResult(
            currentStreak = running,
            bestStreak = best,
            totalCompletions = total,
            missedDays = missedPeriods,
            skippedDays = 0,
            pausedDays = pausedDaysTotal.size,
            completionRate = completionRate,
            perfectWeeks = if (periodOf(input.startDate) == weekPeriodKey(input.startDate)) perfectPeriodCount.takeIf { input.frequencyType == FrequencyType.TIMES_PER_WEEK } ?: 0 else 0,
            perfectMonths = perfectPeriodCount.takeIf { input.frequencyType == FrequencyType.TIMES_PER_MONTH } ?: 0,
        )
    }

    private fun periodHasEnded(periodStart: LocalDate, periodOf: (LocalDate) -> Int, today: LocalDate): Boolean {
        // A period has ended once "today" has moved into a later period.
        return periodOf(today) != periodOf(periodStart)
    }

    private fun weekPeriodKey(date: LocalDate): Int {
        val weekFields = WeekFields.ISO
        return date.get(weekFields.weekBasedYear()) * 100 + date.get(weekFields.weekOfWeekBasedYear())
    }

    private fun weekPeriodStart(key: Int): LocalDate {
        val year = key / 100
        val week = key % 100
        return LocalDate.of(year, 1, 4)
            .with(WeekFields.ISO.weekOfWeekBasedYear(), week.toLong())
            .with(DayOfWeek.MONDAY)
    }

    private fun countPerfectWeeks(input: StreakInput, lastDay: LocalDate): Int {
        if (input.frequencyType == FrequencyType.TIMES_PER_WEEK) return 0 // already reflected in perfectPeriods above
        var count = 0
        var cursor = input.startDate.with(DayOfWeek.MONDAY)
        while (!cursor.isAfter(lastDay)) {
            val weekEnd = minOf(cursor.plusDays(6), lastDay)
            val scheduledDays = generateSequence(cursor) { it.plusDays(1) }
                .takeWhile { !it.isAfter(weekEnd) }
                .filter { isScheduled(input, it) && !isPaused(input, it) }
                .toList()
            if (scheduledDays.isNotEmpty() && scheduledDays.all { (input.completedAmountByDate[it] ?: 0.0) >= input.dailyTargetAmount }) {
                count++
            }
            cursor = cursor.plusWeeks(1)
        }
        return count
    }

    private fun countPerfectMonths(input: StreakInput, lastDay: LocalDate): Int {
        if (input.frequencyType == FrequencyType.TIMES_PER_MONTH) return 0
        var count = 0
        var cursor = input.startDate.withDayOfMonth(1)
        while (!cursor.isAfter(lastDay)) {
            val monthEnd = minOf(cursor.plusMonths(1).minusDays(1), lastDay)
            val scheduledDays = generateSequence(cursor) { it.plusDays(1) }
                .takeWhile { !it.isAfter(monthEnd) }
                .filter { isScheduled(input, it) && !isPaused(input, it) }
                .toList()
            if (scheduledDays.isNotEmpty() && scheduledDays.all { (input.completedAmountByDate[it] ?: 0.0) >= input.dailyTargetAmount }) {
                count++
            }
            cursor = cursor.plusMonths(1)
        }
        return count
    }
}
