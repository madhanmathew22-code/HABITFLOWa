package com.habitflow.app.domain.model

import java.time.LocalDate

data class JournalEntry(
    val id: Long,
    val date: LocalDate,
    val title: String,
    val body: String,
    val moodLevel: MoodLevel?,
    val photoUris: List<String> = emptyList(),
    val createdAtEpochMillis: Long,
)

/** One day's mood check-in, independent of any journal entry (section 14). */
data class MoodDay(
    val date: LocalDate,
    val moodLevel: MoodLevel,
)
