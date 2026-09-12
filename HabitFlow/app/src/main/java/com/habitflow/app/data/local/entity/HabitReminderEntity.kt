package com.habitflow.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A single local-notification reminder for a habit (section 19). A habit
 * can have zero or more of these; each fires independently via
 * WorkManager/AlarmManager, scheduled from [hour]/[minute] and, if
 * [weekdaysMask] is non-zero, restricted to those weekdays (bit 0 = Monday).
 */
@Entity(
    tableName = "habit_reminders",
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
data class HabitReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val habitId: Long,
    val hour: Int,
    val minute: Int,
    val weekdaysMask: Int = 0,
    val customMessage: String? = null,
    val isEnabled: Boolean = true,
)
