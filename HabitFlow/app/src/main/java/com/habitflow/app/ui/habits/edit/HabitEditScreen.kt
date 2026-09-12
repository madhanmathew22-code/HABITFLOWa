package com.habitflow.app.ui.habits.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.habitflow.app.ui.habits.form.BasicInfoStep
import com.habitflow.app.ui.habits.form.ScheduleStep
import com.habitflow.app.ui.habits.form.rememberHabitFormState

/**
 * Unlike habit creation, editing an existing habit doesn't need the
 * step-by-step wizard -- the person already knows what they're changing.
 * This reuses [BasicInfoStep] and [ScheduleStep] (the two steps relevant
 * to "what does this habit look like", as opposed to the one-time
 * onboarding-style Reminder/Finish steps) in a single scroll.
 */
@Composable
fun HabitEditScreen(onDone: () -> Unit, viewModel: HabitEditViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    val habit = state.habit

    LaunchedEffect(state.saved) {
        if (state.saved) onDone()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit habit") },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        if (habit == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                Text(if (state.isLoading) "Loading..." else "Habit not found", modifier = Modifier.padding(20.dp))
            }
            return@Scaffold
        }

        val formState = rememberHabitFormState(initial = habit)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            BasicInfoStep(formState)
            ScheduleStep(formState)

            if (state.errorMessage != null) {
                Text(state.errorMessage ?: "", color = MaterialTheme.colorScheme.error)
            }

            Button(
                onClick = { viewModel.save(formState.toHabit(existing = habit)) },
                enabled = formState.isValid,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Save changes")
            }
        }
    }
}
