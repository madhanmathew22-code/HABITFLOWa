package com.habitflow.app.ui.habits.form

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.habitflow.app.domain.model.FrequencyType
import com.habitflow.app.domain.model.HabitDifficulty
import com.habitflow.app.domain.model.HabitValueType
import com.habitflow.app.ui.common.IconRegistry
import com.habitflow.app.ui.common.WeekdaySelector
import com.habitflow.app.ui.theme.HabitAccentColors

@Composable
fun BasicInfoStep(state: HabitFormState) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        OutlinedTextField(
            value = state.name,
            onValueChange = { state.name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = state.nameError != null,
            supportingText = state.nameError?.let { { Text(it) } },
        )
        OutlinedTextField(
            value = state.description,
            onValueChange = { state.description = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth(),
        )

        Text("Icon", style = MaterialTheme.typography.titleMedium)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(IconRegistry.selectableKeys) { key ->
                val selected = key == state.iconKey
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clickable { state.iconKey = key }
                        .background(
                            if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(IconRegistry.iconFor(key), contentDescription = key)
                }
            }
        }

        Text("Color", style = MaterialTheme.typography.titleMedium)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(HabitAccentColors) { color ->
                val argb = color.toArgb8()
                val selected = argb == state.colorArgb
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clickable { state.colorArgb = argb }
                        .background(color, CircleShape)
                        .then(
                            if (selected) Modifier.border(2.dp, MaterialTheme.colorScheme.onSurface, CircleShape) else Modifier,
                        ),
                )
            }
        }

        Text("Difficulty", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            HabitDifficulty.entries.forEach { option ->
                FilterChip(
                    selected = state.difficulty == option,
                    onClick = { state.difficulty = option },
                    label = { Text(option.name.lowercase().replaceFirstChar { it.uppercase() }) },
                )
            }
        }
    }
}

@Composable
fun ScheduleStep(state: HabitFormState) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Frequency", style = MaterialTheme.typography.titleMedium)
        FrequencyChips(state)

        when (state.frequencyType) {
            FrequencyType.SPECIFIC_WEEKDAYS -> {
                Text("On these days", style = MaterialTheme.typography.titleMedium)
                WeekdaySelector(mask = state.weekdaysMask, onMaskChange = { state.weekdaysMask = it })
            }
            FrequencyType.TIMES_PER_WEEK -> {
                OutlinedTextField(
                    value = state.timesPerWeekText,
                    onValueChange = { state.timesPerWeekText = it.filter(Char::isDigit) },
                    label = { Text("Times per week") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            FrequencyType.TIMES_PER_MONTH -> {
                OutlinedTextField(
                    value = state.timesPerMonthText,
                    onValueChange = { state.timesPerMonthText = it.filter(Char::isDigit) },
                    label = { Text("Times per month") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            FrequencyType.EVERY_N_DAYS -> {
                OutlinedTextField(
                    value = state.everyNDaysText,
                    onValueChange = { state.everyNDaysText = it.filter(Char::isDigit) },
                    label = { Text("Every N days") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            FrequencyType.DAILY, FrequencyType.FLEXIBLE -> Unit
        }

        Text("What does completing it record?", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            HabitValueType.entries.forEach { option ->
                FilterChip(
                    selected = state.valueType == option,
                    onClick = { state.valueType = option },
                    label = { Text(option.name.lowercase().replace('_', ' ')) },
                )
            }
        }
        if (state.valueType != HabitValueType.BOOLEAN) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = state.targetQuantityText,
                    onValueChange = { state.targetQuantityText = it },
                    label = { Text("Target") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = state.unit,
                    onValueChange = { state.unit = it },
                    label = { Text("Unit (ml, pages...)") },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun FrequencyChips(state: HabitFormState) {
    val rows = FrequencyType.entries.chunked(3)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { option ->
                    FilterChip(
                        selected = state.frequencyType == option,
                        onClick = { state.frequencyType = option },
                        label = { Text(option.name.lowercase().replace('_', ' ')) },
                    )
                }
            }
        }
    }
}

@Composable
fun ReminderStep(state: HabitFormState) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Remind me", style = MaterialTheme.typography.titleMedium)
            Switch(checked = state.reminderEnabled, onCheckedChange = { state.reminderEnabled = it })
        }

        if (state.reminderEnabled) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    NumberStepper(
                        value = state.reminderHour,
                        range = 0..23,
                        onChange = { state.reminderHour = it },
                    )
                    Text(" : ", style = MaterialTheme.typography.headlineMedium)
                    NumberStepper(
                        value = state.reminderMinute,
                        range = 0..59,
                        step = 5,
                        onChange = { state.reminderMinute = it },
                    )
                }
            }
        }

        Text("Notes", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            value = state.notes,
            onValueChange = { state.notes = it },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
        )
    }
}

@Composable
private fun NumberStepper(value: Int, range: IntRange, step: Int = 1, onChange: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        FilledIconButton(onClick = { onChange(((value - step).coerceIn(range.first, range.last))) }) {
            Icon(Icons.Filled.Remove, contentDescription = "Decrease")
        }
        Text(
            text = value.toString().padStart(2, '0'),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(horizontal = 12.dp),
        )
        FilledIconButton(onClick = { onChange(((value + step).coerceIn(range.first, range.last))) }) {
            Icon(Icons.Filled.Add, contentDescription = "Increase")
        }
    }
}

@Composable
fun ReviewStep(state: HabitFormState) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Your habit is ready \uD83C\uDFAF", style = MaterialTheme.typography.headlineMedium)
        ReviewRow("Name", state.name.ifBlank { "-" })
        ReviewRow("Frequency", state.frequencyType.name.lowercase().replace('_', ' '))
        ReviewRow("Tracks", state.valueType.name.lowercase().replace('_', ' '))
        if (state.reminderEnabled) {
            ReviewRow(
                "Reminder",
                "%02d:%02d".format(state.reminderHour, state.reminderMinute),
            )
        }
    }
}

@Composable
private fun ReviewRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleMedium)
    }
}

private fun Color.toArgb8(): Int = android.graphics.Color.argb(
    (alpha * 255).toInt(),
    (red * 255).toInt(),
    (green * 255).toInt(),
    (blue * 255).toInt(),
)
