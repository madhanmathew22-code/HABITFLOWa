@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

package com.habitflow.app.ui.journal

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.habitflow.app.domain.model.JournalEntry
import com.habitflow.app.domain.model.MoodLevel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun JournalScreen(
    onOpenComposer: () -> Unit,
    viewModel: JournalViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onOpenComposer) {
                Icon(Icons.Filled.Add, contentDescription = "New journal entry")
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Text("Journal", style = MaterialTheme.typography.headlineMedium)
            }
            item {
                MoodStrip(
                    last7DaysMood = state.last7DaysMood,
                    onSetTodayMood = viewModel::onSetTodayMood,
                )
            }

            if (state.entries.isEmpty() && !state.isLoading) {
                item {
                    Column(modifier = Modifier.padding(vertical = 48.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Your story starts here.", style = MaterialTheme.typography.titleLarge)
                    }
                }
            } else {
                items(state.entries, key = { it.id }) { entry ->
                    JournalEntryCard(entry = entry, onDelete = { viewModel.onDeleteEntry(entry.id) })
                }
            }
        }
    }
}

@Composable
private fun MoodStrip(last7DaysMood: Map<LocalDate, MoodLevel>, onSetTodayMood: (MoodLevel) -> Unit) {
    val today = LocalDate.now()
    Column {
        Text("How are you today?", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MoodLevel.entries.forEach { mood ->
                val selected = last7DaysMood[today] == mood
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clickable { onSetTodayMood(mood) }
                        .background(
                            if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(mood.emoji, style = MaterialTheme.typography.titleLarge)
                }
            }
        }
    }
}

@Composable
private fun JournalEntryCard(entry: JournalEntry, onDelete: () -> Unit) {
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = {}, onLongClick = { menuExpanded = true }),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Box {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        entry.date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d")),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    entry.moodLevel?.let { Text(it.emoji, style = MaterialTheme.typography.titleMedium) }
                }
                if (entry.title.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(entry.title, style = MaterialTheme.typography.titleMedium)
                }
                if (entry.body.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(entry.body, style = MaterialTheme.typography.bodyMedium, maxLines = 4)
                }
                if (entry.photoUris.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(entry.photoUris) { uri ->
                            AsyncImage(
                                model = uri,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                            )
                        }
                    }
                }
            }

            DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                DropdownMenuItem(text = { Text("Delete") }, onClick = { menuExpanded = false; onDelete() })
            }
        }
    }
}
