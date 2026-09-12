package com.habitflow.app.di

import android.content.ContentValues
import android.content.Context
import androidx.room.Room
import com.habitflow.app.data.local.HabitFlowDatabase
import com.habitflow.app.data.local.dao.CategoryDao
import com.habitflow.app.data.local.dao.HabitCompletionDao
import com.habitflow.app.data.local.dao.HabitDao
import com.habitflow.app.data.local.dao.HabitPauseDao
import com.habitflow.app.data.local.dao.HabitReminderDao
import com.habitflow.app.data.local.dao.JournalDao
import com.habitflow.app.data.local.dao.MoodDao
import com.habitflow.app.data.local.entity.DefaultCategories
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * A process-lifetime scope for one-shot DB setup work (default category
     * seeding) that shouldn't be tied to any particular screen's ViewModel.
     */
    @Provides
    @Singleton
    fun provideApplicationScope(): CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        applicationScope: CoroutineScope,
    ): HabitFlowDatabase =
        Room.databaseBuilder(context, HabitFlowDatabase::class.java, HabitFlowDatabase.DATABASE_NAME)
            .addCallback(object : androidx.room.RoomDatabase.Callback() {
                override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                    super.onCreate(db)
                    // Seed the nine default categories (section 22) exactly once,
                    // on first-ever database creation. Inserted as raw ContentValues
                    // (rather than via CategoryDao) so this callback doesn't need a
                    // second HabitFlowDatabase instance from the Hilt graph.
                    applicationScope.launch {
                        DefaultCategories.seed.forEach { category ->
                            val values = ContentValues().apply {
                                put("name", category.name)
                                put("iconKey", category.iconKey)
                                put("colorArgb", category.colorArgb)
                                put("isCustom", if (category.isCustom) 1 else 0)
                                put("sortOrder", category.sortOrder)
                            }
                            db.insert("categories", android.database.sqlite.SQLiteDatabase.CONFLICT_IGNORE, values)
                        }
                    }
                }
            })
            .build()

    @Provides
    fun provideHabitDao(db: HabitFlowDatabase): HabitDao = db.habitDao()

    @Provides
    fun provideHabitCompletionDao(db: HabitFlowDatabase): HabitCompletionDao = db.habitCompletionDao()

    @Provides
    fun provideHabitPauseDao(db: HabitFlowDatabase): HabitPauseDao = db.habitPauseDao()

    @Provides
    fun provideHabitReminderDao(db: HabitFlowDatabase): HabitReminderDao = db.habitReminderDao()

    @Provides
    fun provideCategoryDao(db: HabitFlowDatabase): CategoryDao = db.categoryDao()

    @Provides
    fun provideJournalDao(db: HabitFlowDatabase): JournalDao = db.journalDao()

    @Provides
    fun provideMoodDao(db: HabitFlowDatabase): MoodDao = db.moodDao()
}
