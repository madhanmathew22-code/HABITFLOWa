package com.habitflow.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

/**
 * An inclusive [startDate, endDate] range during which a habit was paused.
 * [endDate] is null while the pause is still active (not yet resumed).
 *
 * This is what lets the streak engine treat "September 10 -> September 15"
 * as neutral days rather than missed days (section 8).
 */
@Entity(
    tableName = "habit_pauses",
    foreignKeys = [
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("habitId")],
)
data class HabitPauseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val habitId: Long,
    val startDate: LocalDate,
    val endDate: LocalDate? = null,
    val reason: String = "",
)
