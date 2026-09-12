package com.habitflow.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

/**
 * One recorded completion of a habit on a given date.
 *
 * Multiple rows for the same (habitId, date) are allowed on purpose: a
 * TIMES_PER_WEEK habit like "Exercise 3x/week" can be completed more than
 * once on the same day (section 7: "Allow additional completions beyond
 * the target"), and the streak engine sums [amount] per day rather than
 * assuming one row = one day.
 */
@Entity(
    tableName = "habit_completions",
    foreignKeys = [
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("habitId"), Index("date"), Index("habitId", "date")],
)
data class HabitCompletionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val habitId: Long,
    val date: LocalDate,
    /** 1.0 for boolean habits; minutes for duration habits; raw value for quantity habits. */
    val amount: Double = 1.0,
    val note: String = "",
    val loggedAtEpochMillis: Long = System.currentTimeMillis(),
)
