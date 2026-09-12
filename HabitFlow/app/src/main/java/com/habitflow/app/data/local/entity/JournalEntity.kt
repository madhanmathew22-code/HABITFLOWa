package com.habitflow.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

/**
 * A journal entry (section 13). Multiple entries per day are supported —
 * there's no uniqueness constraint on [date].
 */
@Entity(tableName = "journal_entries", indices = [Index("date")])
data class JournalEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: LocalDate,
    val title: String = "",
    val body: String = "",
    /** Nullable: mood tracking is optional per-user (section 14). */
    val moodLevelOrdinal: Int? = null,
    val createdAtEpochMillis: Long = System.currentTimeMillis(),
    val updatedAtEpochMillis: Long = System.currentTimeMillis(),
)

/** A photo attached to a journal entry. Stores a content:// / file:// URI, not the bytes. */
@Entity(
    tableName = "journal_media",
    foreignKeys = [
        ForeignKey(
            entity = JournalEntryEntity::class,
            parentColumns = ["id"],
            childColumns = ["journalEntryId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("journalEntryId")],
)
data class JournalMediaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val journalEntryId: Long,
    val uri: String,
    val sortOrder: Int = 0,
)

/**
 * Cross-reference letting a journal entry reference one or more habits
 * (section 13: "Linked habits"), independent of whether a completion was
 * actually logged that day.
 */
@Entity(
    tableName = "journal_habit_cross_ref",
    primaryKeys = ["journalEntryId", "habitId"],
    foreignKeys = [
        ForeignKey(
            entity = JournalEntryEntity::class,
            parentColumns = ["id"],
            childColumns = ["journalEntryId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("habitId")],
)
data class JournalHabitCrossRef(
    val journalEntryId: Long,
    val habitId: Long,
)
