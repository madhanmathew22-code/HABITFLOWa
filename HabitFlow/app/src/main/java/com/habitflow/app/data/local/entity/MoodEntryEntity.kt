package com.habitflow.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

/**
 * A lightweight daily mood check-in, independent of the journal. This is
 * what feeds the mood trend chart in Insights (section 15) even for users
 * who never write a journal entry. One row per calendar date.
 */
@Entity(tableName = "mood_entries", indices = [Index(value = ["date"], unique = true)])
data class MoodEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: LocalDate,
    val moodLevelOrdinal: Int,
)
