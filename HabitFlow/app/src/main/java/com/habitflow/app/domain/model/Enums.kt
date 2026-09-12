package com.habitflow.app.domain.model

/** How a habit's schedule is defined (section 7). */
enum class FrequencyType {
    DAILY,
    SPECIFIC_WEEKDAYS,
    TIMES_PER_WEEK,
    TIMES_PER_MONTH,
    EVERY_N_DAYS,
    FLEXIBLE,
}

/** What a single completion of a habit actually records (section 7). */
enum class HabitValueType {
    BOOLEAN,
    DURATION_MINUTES,
    QUANTITY,
}

enum class Weekday(val isoDayNumber: Int) {
    MONDAY(1), TUESDAY(2), WEDNESDAY(3), THURSDAY(4),
    FRIDAY(5), SATURDAY(6), SUNDAY(7);

    companion object {
        fun fromIso(isoDayNumber: Int): Weekday =
            entries.first { it.isoDayNumber == isoDayNumber }
    }
}

enum class HabitDifficulty { EASY, MEDIUM, HARD }

enum class MoodLevel(val emoji: String) {
    GREAT("😄"),
    GOOD("🙂"),
    OKAY("😐"),
    LOW("😔"),
    DIFFICULT("😣"),
}

/** Per-date status used by the calendar (section 12) and streak engine. */
enum class DayStatus {
    COMPLETED,
    PENDING,
    MISSED,
    NOT_SCHEDULED,
    PAUSED,
}
