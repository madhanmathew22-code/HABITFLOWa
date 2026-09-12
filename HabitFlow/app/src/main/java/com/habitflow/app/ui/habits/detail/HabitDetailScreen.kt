@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.habitflow.app.ui.habits.detail

import ...
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.habitflow.app.domain.model.DayStatus

@Composable
fun HabitDetailScreen(
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,
    onDuplicated: (Long) -> Unit,
    viewModel: HabitDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val habitForDate = state.today
    var menuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(habitForDate?.habit?.name ?: "Habit") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (habitForDate != null) {
                        IconButton(onClick = viewModel::onTogglePinned) {
                            Icon(
                                Icons.Filled.PushPin,
                                contentDescription = if (habitForDate.habit.isPinned) "Unpin" else "Pin",
                                tint = if (habitForDate.habit.isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Filled.MoreVert, contentDescription = "More options")
                        }
                        DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                            DropdownMenuItem(
                                text = { Text("Edit") },
                                onClick = { menuExpanded = false; onEdit(viewModel.habitId) },
                            )
                            DropdownMenuItem(
                                text = { Text("Duplicate") },
                                onClick = { menuExpanded = false; viewModel.onDuplicate(onDuplicated) },
                            )
                            if (habitForDate.status == DayStatus.PAUSED) {
                                DropdownMenuItem(
                                    text = { Text("Resume") },
                                    onClick = { menuExpanded = false; viewModel.onResume() },
                                )
                            } else {
                                DropdownMenuItem(
                                    text = { Text("Pause") },
                                    onClick = { menuExpanded = false; viewModel.onPauseIndefinitely() },
                                )
                                DropdownMenuItem(
                                    text = { Text("Skip today") },
                                    onClick = { menuExpanded = false; viewModel.onSkipToday() },
                                )
                            }
                        }
                    }
                },
            )
        },
    ) { padding ->
        if (habitForDate == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(if (state.isLoading) "Loading..." else "Habit not found")
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (habitForDate.habit.description.isNotBlank()) {
                Text(habitForDate.habit.description, style = MaterialTheme.typography.bodyLarge)
            }

            Text(
                text = "\uD83D\uDD25 ${habitForDate.currentStreak} day streak",
                style = MaterialTheme.typography.titleMedium,
            )

            Text(
                text = "Frequency: ${habitForDate.habit.frequencyType.name.lowercase().replace('_', ' ')}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (habitForDate.status == DayStatus.PAUSED) {
                Text(
                    text = "Paused",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            if (habitForDate.habit.notes.isNotBlank()) {
                Text("Notes", style = MaterialTheme.typography.titleMedium)
                Text(habitForDate.habit.notes, style = MaterialTheme.typography.bodyMedium)
            }

            Button(
                onClick = viewModel::onToggleComplete,
                modifier = Modifier.fillMaxWidth(),
                enabled = habitForDate.status != DayStatus.PAUSED,
            ) {
                Icon(
                    imageVector = if (habitForDate.status == DayStatus.COMPLETED) Icons.Filled.Check else Icons.Filled.RadioButtonUnchecked,
                    contentDescription = null,
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (habitForDate.status == DayStatus.COMPLETED) "Completed today" else "Mark complete",
                )
            }

            TextButton(onClick = { viewModel.onArchive(onDone = onBack) }) {
                Text("Archive habit")
            }
        }
    }
}
