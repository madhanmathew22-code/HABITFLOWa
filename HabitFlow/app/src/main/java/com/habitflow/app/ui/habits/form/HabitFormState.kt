package com.habitflow.app.ui.habits.form

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.habitflow.app.domain.model.FrequencyType
import com.habitflow.app.domain.model.Habit
import com.habitflow.app.domain.model.HabitDifficulty
import com.habitflow.app.domain.model.HabitValueType
import com.habitflow.app.ui.theme.HabitAccentColors
import java.time.LocalDate

/**
 * Everything the habit creation wizard and the habit edit screen both need
 * to render and validate. Pulling this out means those two surfaces share
 * one implementation of "what a habit's editable fields are" rather than
 * two copies drifting apart (edit screen didn't exist before this phase).
 *
 * This is intentionally NOT a ViewModel -- it's UI-local, `remember`-scoped
 * state. [CreateHabitViewModel]/[com.habitflow.app.ui.habits.edit.HabitEditViewModel]
 * only ever see the final [toHabit] result, not these intermediate fields.
 */
class HabitFormState(initial: Habit?) {
    var name by mutableStateOf(initial?.name ?: "")
    var description by mutableStateOf(initial?.description ?: "")
    var iconKey by mutableStateOf(initial?.iconKey ?: "check_circle")
    var colorArgb by mutableStateOf(initial?.colorArgb ?: HabitAccentColors.first().toArgbIntValue())
    var frequencyType by mutableStateOf(initial?.frequencyType ?: FrequencyType.DAILY)
    var weekdaysMask by mutableStateOf(initial?.weekdaysMask ?: 0)
    var timesPerWeekText by mutableStateOf(initial?.timesPerWeek?.toString() ?: "3")
    var timesPerMonthText by mutableStateOf(initial?.timesPerMonth?.toString() ?: "15")
    var everyNDaysText by mutableStateOf(initial?.everyNDays?.toString() ?: "2")
    var valueType by mutableStateOf(initial?.valueType ?: HabitValueType.BOOLEAN)
    var targetQuantityText by mutableStateOf(initial?.targetQuantity?.toString() ?: "1")
    var unit by mutableStateOf(initial?.unit ?: "")
    var difficulty by mutableStateOf(initial?.difficulty ?: HabitDifficulty.MEDIUM)
    var notes by mutableStateOf(initial?.notes ?: "")
    var reminderEnabled by mutableStateOf(false)
    var reminderHour by mutableStateOf(8)
    var reminderMinute by mutableStateOf(0)

    val nameError: String?
        get() = if (name.isBlank()) "Name is required" else null

    val isValid: Boolean get() = nameError == null

    /** Builds the [Habit] this form currently represents, preserving [existing]'s identity fields. */
    fun toHabit(existing: Habit?): Habit = Habit(
        id = existing?.id ?: 0,
        name = name.trim(),
        description = description.trim(),
        iconKey = iconKey,
        colorArgb = colorArgb,
        categoryId = existing?.categoryId,
        frequencyType = frequencyType,
        weekdaysMask = if (frequencyType == FrequencyType.SPECIFIC_WEEKDAYS) weekdaysMask else 0,
        timesPerWeek = if (frequencyType == FrequencyType.TIMES_PER_WEEK) timesPerWeekText.toIntOrNull() else null,
        timesPerMonth = if (frequencyType == FrequencyType.TIMES_PER_MONTH) timesPerMonthText.toIntOrNull() else null,
        everyNDays = if (frequencyType == FrequencyType.EVERY_N_DAYS) everyNDaysText.toIntOrNull() else null,
        flexibleWindowDays = existing?.flexibleWindowDays,
        valueType = valueType,
        targetQuantity = targetQuantityText.toDoubleOrNull() ?: 1.0,
        unit = unit.trim().ifBlank { null },
        difficulty = difficulty,
        startDate = existing?.startDate ?: LocalDate.now(),
        endDate = existing?.endDate,
        notes = notes.trim(),
        isArchived = existing?.isArchived ?: false,
        isPinned = existing?.isPinned ?: false,
        isFavorite = existing?.isFavorite ?: false,
        sortOrder = existing?.sortOrder ?: 0,
        stackAfterHabitId = existing?.stackAfterHabitId,
        dependsOnHabitId = existing?.dependsOnHabitId,
    )
}

@Composable
fun rememberHabitFormState(initial: Habit? = null): HabitFormState =
    remember(initial?.id) { HabitFormState(initial) }

private fun androidx.compose.ui.graphics.Color.toArgbIntValue(): Int = android.graphics.Color.argb(
    (alpha * 255).toInt(),
    (red * 255).toInt(),
    (green * 255).toInt(),
    (blue * 255).toInt(),
)
