package com.habitflow.app.domain.model

import java.time.LocalDate

/**
 * The calendar (section 12) shows one cell per day across *all* habits, not
 * per-habit -- so a single [DayStatus] per habit isn't what a month grid
 * cell renders. This aggregates every active habit's status for one date
 * into counts, with [overallStatus] as the single value the grid cell
 * colors itself by.
 */
data class DaySummary(
    val date: LocalDate,
    val scheduledCount: Int,
    val completedCount: Int,
    val missedCount: Int,
    val pausedCount: Int,
    val pendingCount: Int,
) {
    val overallStatus: DayStatus
        get() = when {
            scheduledCount == 0 -> DayStatus.NOT_SCHEDULED
            completedCount == scheduledCount -> DayStatus.COMPLETED
            pausedCount == scheduledCount -> DayStatus.PAUSED
            missedCount == 0 && pendingCount > 0 -> DayStatus.PENDING
            else -> DayStatus.MISSED
        }

    companion object {
        fun empty(date: LocalDate) = DaySummary(date, 0, 0, 0, 0, 0)
    }
}
