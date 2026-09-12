package com.habitflow.app.domain.streak

import com.habitflow.app.domain.model.FrequencyType
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.LocalDate

/**
 * NOTE: these tests were written against [StreakEngine] but could not be
 * executed in the environment this project was authored in (no JVM/Gradle
 * toolchain available there — see the project README). Run
 * `./gradlew testDebugUnitTest --tests StreakEngineTest` in Android Studio
 * before relying on this file; treat it as a strong starting point, not a
 * verified-green suite.
 */
class StreakEngineTest {

    private val engine = StreakEngine()

    @Test
    fun `daily habit with no misses gives a full streak`() {
        val start = LocalDate.of(2026, 1, 1)
        val today = LocalDate.of(2026, 1, 10)
        val completions = (0..9).associate { start.plusDays(it.toLong()) to 1.0 }

        val result = engine.evaluate(
            StreakInput(
                startDate = start,
                endDate = null,
                frequencyType = FrequencyType.DAILY,
                completedAmountByDate = completions,
                pauseRanges = emptyList(),
            ),
            today = today,
        )

        assertThat(result.currentStreak).isEqualTo(10)
        assertThat(result.bestStreak).isEqualTo(10)
        assertThat(result.missedDays).isEqualTo(0)
    }

    @Test
    fun `a single missed day resets current streak but keeps best`() {
        val start = LocalDate.of(2026, 1, 1)
        val today = LocalDate.of(2026, 1, 10)
        // Completed days 1-5 and 7-10; day 6 missed.
        val completions = buildMap {
            for (i in 0..4) put(start.plusDays(i.toLong()), 1.0)
            for (i in 6..9) put(start.plusDays(i.toLong()), 1.0)
        }

        val result = engine.evaluate(
            StreakInput(
                startDate = start,
                endDate = null,
                frequencyType = FrequencyType.DAILY,
                completedAmountByDate = completions,
                pauseRanges = emptyList(),
            ),
            today = today,
        )

        assertThat(result.bestStreak).isEqualTo(5)
        assertThat(result.currentStreak).isEqualTo(4)
        assertThat(result.missedDays).isEqualTo(1)
    }

    @Test
    fun `today not yet completed does not break or extend the streak`() {
        val start = LocalDate.of(2026, 1, 1)
        val today = LocalDate.of(2026, 1, 5)
        // Days 1-4 completed, day 5 (today) has nothing logged yet.
        val completions = (0..3).associate { start.plusDays(it.toLong()) to 1.0 }

        val result = engine.evaluate(
            StreakInput(
                startDate = start,
                endDate = null,
                frequencyType = FrequencyType.DAILY,
                completedAmountByDate = completions,
                pauseRanges = emptyList(),
            ),
            today = today,
        )

        assertThat(result.currentStreak).isEqualTo(4)
        assertThat(result.missedDays).isEqualTo(0)
    }

    @Test
    fun `a paused range is neutral and does not damage the streak`() {
        val start = LocalDate.of(2026, 9, 1)
        val today = LocalDate.of(2026, 9, 10)
        // Complete days 1-5, pause 6-8 (matches the spec's own example), complete 9-10.
        val completions = buildMap {
            for (i in 0..4) put(start.plusDays(i.toLong()), 1.0)
            put(LocalDate.of(2026, 9, 9), 1.0)
            put(LocalDate.of(2026, 9, 10), 1.0)
        }

        val result = engine.evaluate(
            StreakInput(
                startDate = start,
                endDate = null,
                frequencyType = FrequencyType.DAILY,
                completedAmountByDate = completions,
                pauseRanges = listOf(
                    LocalDate.of(2026, 9, 6)..LocalDate.of(2026, 9, 8),
                ),
            ),
            today = today,
        )

        assertThat(result.pausedDays).isEqualTo(3)
        assertThat(result.currentStreak).isEqualTo(7) // 5 before pause + 2 after, pause is neutral
        assertThat(result.missedDays).isEqualTo(0)
    }

    @Test
    fun `specific weekdays only counts the scheduled weekdays`() {
        // Monday, Wednesday, Friday only. weekdaysMask bit0=Mon .. bit6=Sun.
        val mask = (1 shl 0) or (1 shl 2) or (1 shl 4) // Mon, Wed, Fri
        val start = LocalDate.of(2026, 1, 5) // a Monday
        val today = LocalDate.of(2026, 1, 16) // following Friday

        // Complete every Mon/Wed/Fri in range.
        val completions = buildMap {
            var d = start
            while (!d.isAfter(today)) {
                val bit = d.dayOfWeek.value - 1
                if ((mask shr bit) and 1 == 1) put(d, 1.0)
                d = d.plusDays(1)
            }
        }

        val result = engine.evaluate(
            StreakInput(
                startDate = start,
                endDate = null,
                frequencyType = FrequencyType.SPECIFIC_WEEKDAYS,
                weekdaysMask = mask,
                completedAmountByDate = completions,
                pauseRanges = emptyList(),
            ),
            today = today,
        )

        // Mon 5, Wed 7, Fri 9, Mon 12, Wed 14, Fri 16 = 6 scheduled days, all completed.
        assertThat(result.currentStreak).isEqualTo(6)
        assertThat(result.completionRate).isEqualTo(1.0)
    }

    @Test
    fun `every N days only schedules every Nth day from start`() {
        val start = LocalDate.of(2026, 2, 1)
        val today = LocalDate.of(2026, 2, 9) // day offsets 0..8, every-2-days -> 0,2,4,6,8
        val completions = listOf(0, 2, 4, 6, 8).associate { start.plusDays(it.toLong()) to 1.0 }

        val result = engine.evaluate(
            StreakInput(
                startDate = start,
                endDate = null,
                frequencyType = FrequencyType.EVERY_N_DAYS,
                everyNDays = 2,
                completedAmountByDate = completions,
                pauseRanges = emptyList(),
            ),
            today = today,
        )

        assertThat(result.currentStreak).isEqualTo(5)
        assertThat(result.missedDays).isEqualTo(0)
    }

    @Test
    fun `times per week habit only breaks streak when a completed week ends short`() {
        val start = LocalDate.of(2026, 1, 5) // Monday
        // Week 1 (Jan 5-11): 3 completions, meets target of 3 -> perfect.
        // Week 2 (Jan 12-18): only 1 completion -> falls short, week has fully ended.
        // Week 3 (Jan 19-25), evaluated as of Jan 20 (Tuesday): 1 completion so far,
        //   week NOT over yet -> must not count as a miss.
        val completions = mapOf(
            LocalDate.of(2026, 1, 5) to 1.0,
            LocalDate.of(2026, 1, 7) to 1.0,
            LocalDate.of(2026, 1, 9) to 1.0,
            LocalDate.of(2026, 1, 13) to 1.0,
            LocalDate.of(2026, 1, 19) to 1.0,
        )
        val today = LocalDate.of(2026, 1, 20)

        val result = engine.evaluate(
            StreakInput(
                startDate = start,
                endDate = null,
                frequencyType = FrequencyType.TIMES_PER_WEEK,
                timesPerWeek = 3,
                completedAmountByDate = completions,
                pauseRanges = emptyList(),
            ),
            today = today,
        )

        // Week 2 fell short and has ended -> current streak resets to 0
        // going into week 3, which is still in progress.
        assertThat(result.currentStreak).isEqualTo(0)
        assertThat(result.bestStreak).isEqualTo(1)
    }
}
