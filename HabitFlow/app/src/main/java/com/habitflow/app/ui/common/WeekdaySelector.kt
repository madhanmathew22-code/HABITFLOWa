package com.habitflow.app.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.habitflow.app.domain.model.Weekday

/**
 * The single shared place that knows the weekday <-> bit convention (bit 0
 * = Monday .. bit 6 = Sunday). Both [com.habitflow.app.data.local.entity.HabitEntity.weekdaysMask]
 * and [com.habitflow.app.data.local.entity.HabitReminderEntity.weekdaysMask]
 * use this convention; previously each hand-rolled `1 shl n` independently
 * (audit item #6) with no shared source of truth.
 */
object WeekdayMask {
    fun bitFor(weekday: Weekday): Int = weekday.isoDayNumber - 1 // Monday(1) -> bit 0

    fun contains(mask: Int, weekday: Weekday): Boolean = (mask shr bitFor(weekday)) and 1 == 1

    fun toggled(mask: Int, weekday: Weekday): Int = mask xor (1 shl bitFor(weekday))

    fun toSet(mask: Int): Set<Weekday> = Weekday.entries.filter { contains(mask, it) }.toSet()

    fun fromSet(weekdays: Set<Weekday>): Int = weekdays.fold(0) { acc, day -> acc or (1 shl bitFor(day)) }
}

/** A row of 7 toggleable weekday chips (Mon..Sun), bound to a bitmask. */
@Composable
fun WeekdaySelector(mask: Int, onMaskChange: (Int) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Weekday.entries.forEach { day ->
            val selected = WeekdayMask.contains(mask, day)
            FilterChip(
                selected = selected,
                onClick = { onMaskChange(WeekdayMask.toggled(mask, day)) },
                label = { Text(day.name.take(1)) },
            )
        }
    }
}
