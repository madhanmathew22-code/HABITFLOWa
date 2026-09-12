package com.habitflow.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.habitflow.app.data.local.converter.Converters
import com.habitflow.app.data.local.dao.CategoryDao
import com.habitflow.app.data.local.dao.HabitCompletionDao
import com.habitflow.app.data.local.dao.HabitDao
import com.habitflow.app.data.local.dao.HabitPauseDao
import com.habitflow.app.data.local.dao.HabitReminderDao
import com.habitflow.app.data.local.dao.JournalDao
import com.habitflow.app.data.local.dao.MoodDao
import com.habitflow.app.data.local.entity.CategoryEntity
import com.habitflow.app.data.local.entity.HabitCompletionEntity
import com.habitflow.app.data.local.entity.HabitEntity
import com.habitflow.app.data.local.entity.HabitPauseEntity
import com.habitflow.app.data.local.entity.HabitReminderEntity
import com.habitflow.app.data.local.entity.JournalEntryEntity
import com.habitflow.app.data.local.entity.JournalHabitCrossRef
import com.habitflow.app.data.local.entity.JournalMediaEntity
import com.habitflow.app.data.local.entity.MoodEntryEntity

/**
 * Schema v1.
 *
 * NOTE for future migrations: once this ships, every schema change needs a
 * real Migration object here (Room's fallbackToDestructiveMigration must
 * never be used in release builds — it would silently wipe a user's habit
 * history, which section 26 explicitly says the app must never do to user
 * data).
 */
@Database(
    entities = [
        HabitEntity::class,
        HabitCompletionEntity::class,
        HabitPauseEntity::class,
        HabitReminderEntity::class,
        CategoryEntity::class,
        JournalEntryEntity::class,
        JournalMediaEntity::class,
        JournalHabitCrossRef::class,
        MoodEntryEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class HabitFlowDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    abstract fun habitCompletionDao(): HabitCompletionDao
    abstract fun habitPauseDao(): HabitPauseDao
    abstract fun habitReminderDao(): HabitReminderDao
    abstract fun categoryDao(): CategoryDao
    abstract fun journalDao(): JournalDao
    abstract fun moodDao(): MoodDao

    companion object {
        const val DATABASE_NAME = "habitflow.db"
    }
}
