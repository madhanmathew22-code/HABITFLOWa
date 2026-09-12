package com.habitflow.app.data

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.habitflow.app.data.local.HabitFlowDatabase
import com.habitflow.app.data.local.entity.HabitEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

/**
 * Runs against a real (in-memory) SQLite database via
 * androidx.test.runner.AndroidJUnitRunner, so it needs a device/emulator or
 * `./gradlew connectedDebugAndroidTest` -- it was not executed in the
 * environment this project was authored in (no Android runtime available
 * there). See the README for context.
 */
@RunWith(AndroidJUnit4::class)
class HabitDaoTest {

    private lateinit var db: HabitFlowDatabase

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, HabitFlowDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertHabit_thenObserveActiveHabits_returnsIt() = runBlocking {
        val habit = HabitEntity(
            name = "Drink Water",
            colorArgb = 0xFF1F6E63.toInt(),
            startDate = LocalDate.of(2026, 1, 1),
        )
        val id = db.habitDao().insert(habit)

        val active = db.habitDao().observeActiveHabits().first()

        assertThat(active).hasSize(1)
        assertThat(active.first().id).isEqualTo(id)
        assertThat(active.first().name).isEqualTo("Drink Water")
    }

    @Test
    fun archivingHabit_removesItFromActiveList() = runBlocking {
        val id = db.habitDao().insert(
            HabitEntity(
                name = "Meditate",
                colorArgb = 0xFF3C6E9E.toInt(),
                startDate = LocalDate.of(2026, 1, 1),
            ),
        )

        db.habitDao().archive(id)

        val active = db.habitDao().observeActiveHabits().first()
        val archived = db.habitDao().observeArchivedHabits().first()

        assertThat(active).isEmpty()
        assertThat(archived).hasSize(1)
    }

    @Test
    fun completionsAccumulate_forTheSameHabitAndDate() = runBlocking {
        val habitId = db.habitDao().insert(
            HabitEntity(
                name = "Read",
                colorArgb = 0xFF8A6D3B.toInt(),
                startDate = LocalDate.of(2026, 1, 1),
            ),
        )
        val date = LocalDate.of(2026, 1, 5)

        db.habitCompletionDao().insert(
            com.habitflow.app.data.local.entity.HabitCompletionEntity(habitId = habitId, date = date, amount = 10.0),
        )
        db.habitCompletionDao().insert(
            com.habitflow.app.data.local.entity.HabitCompletionEntity(habitId = habitId, date = date, amount = 15.0),
        )

        val total = db.habitCompletionDao().totalAmountForHabitOnDate(habitId, date)

        assertThat(total).isEqualTo(25.0)
    }
}
