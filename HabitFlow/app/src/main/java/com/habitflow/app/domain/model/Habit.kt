package com.habitflow.app.domain.model

import java.time.LocalDate

/** Domain-layer representation of a habit. UI and use cases depend on this, never on the Room entity. */
data class Habit(
    val id: Long,
    val name: String,
    val description: String,
    val iconKey: String,
    val colorArgb: Int,
    val categoryId: Long?,
    val frequencyType: FrequencyType,
    val weekdaysMask: Int,
    val timesPerWeek: Int?,
    val timesPerMonth: Int?,
    val everyNDays: Int?,
    val flexibleWindowDays: Int?,
    val valueType: HabitValueType,
    val targetQuantity: Double?,
    val unit: String?,
    val difficulty: HabitDifficulty,
    val startDate: LocalDate,
    val endDate: LocalDate?,
    val notes: String,
    val isArchived: Boolean,
    val isPinned: Boolean,
    val isFavorite: Boolean,
    val sortOrder: Int,
    val stackAfterHabitId: Long?,
    val dependsOnHabitId: Long?,
)

/** A habit paired with its resolved state for a single date — what habit cards actually render. */
data class HabitForDate(
    val habit: Habit,
    val date: LocalDate,
    val loggedAmount: Double,
    val targetAmount: Double,
    val status: DayStatus,
    val currentStreak: Int,
)
