package com.habitflow.app.ui.habits.create

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habitflow.app.domain.usecase.AddHabitReminderUseCase
import com.habitflow.app.domain.usecase.CreateHabitUseCase
import com.habitflow.app.ui.habits.form.BasicInfoStep
import com.habitflow.app.ui.habits.form.ReminderStep
import com.habitflow.app.ui.habits.form.ReviewStep
import com.habitflow.app.ui.habits.form.ScheduleStep
import com.habitflow.app.ui.habits.form.rememberHabitFormState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private enum class CreateHabitStep(val title: String) {
    BASIC_INFO("Basic info"),
    SCHEDULE("Schedule"),
    REMINDER("Reminder"),
    REVIEW("Finish"),
}

/**
 * The multi-step animated flow from spec section 53 (Basic Info -> Schedule
 * -> Reminder -> Finish), replacing the single-step form from Phase A. The
 * ViewModel contract barely changed -- [submit] still just needs a fully
 * built [com.habitflow.app.domain.model.Habit] -- so this swap didn't touch
 * [CreateHabitUseCase] at all.
 */
@HiltViewModel
class CreateHabitViewModel @Inject constructor(
    private val createHabit: CreateHabitUseCase,
    private val addHabitReminder: AddHabitReminderUseCase,
) : ViewModel() {

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun submit(
        habit: com.habitflow.app.domain.model.Habit,
        reminderEnabled: Boolean,
        reminderHour: Int,
        reminderMinute: Int,
        onComplete: () -> Unit,
    ) {
        viewModelScope.launch {
            when (val result = createHabit(habit)) {
                is CreateHabitUseCase.Result.Success -> {
                    if (reminderEnabled) {
                        addHabitReminder(result.habitId, reminderHour, reminderMinute)
                    }
                    onComplete()
                }
                is CreateHabitUseCase.Result.Invalid -> _errorMessage.value = result.reason
            }
        }
    }
}

@Composable
fun CreateHabitRoute(onDone: () -> Unit, viewModel: CreateHabitViewModel = hiltViewModel()) {
    val formState = rememberHabitFormState()
    var stepIndex by rememberSaveable { mutableIntStateOf(0) }
    val steps = CreateHabitStep.entries
    val currentStep = steps[stepIndex]
    val errorMessage by viewModel.errorMessage.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text(currentStep.title) }) },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            StepIndicator(total = steps.size, current = stepIndex)

            AnimatedContent(
                targetState = stepIndex,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
                    } else {
                        slideInHorizontally { -it } togetherWith slideOutHorizontally { it }
                    }.using(SizeTransform(clip = false))
                },
                label = "create-habit-step",
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
            ) { index ->
                when (steps[index]) {
                    CreateHabitStep.BASIC_INFO -> BasicInfoStep(formState)
                    CreateHabitStep.SCHEDULE -> ScheduleStep(formState)
                    CreateHabitStep.REMINDER -> ReminderStep(formState)
                    CreateHabitStep.REVIEW -> ReviewStep(formState)
                }
            }

            if (errorMessage != null && currentStep == CreateHabitStep.REVIEW) {
                Text(
                    text = errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (stepIndex > 0) {
                    OutlinedButton(onClick = { stepIndex-- }, modifier = Modifier.weight(1f)) {
                        Text("Back")
                    }
                }
                Button(
                    onClick = {
                        if (currentStep == CreateHabitStep.BASIC_INFO && !formState.isValid) {
                            // Force validation message to show; block advancing.
                            return@Button
                        }
                        if (stepIndex < steps.lastIndex) {
                            stepIndex++
                        } else {
                            viewModel.submit(
                                habit = formState.toHabit(existing = null),
                                reminderEnabled = formState.reminderEnabled,
                                reminderHour = formState.reminderHour,
                                reminderMinute = formState.reminderMinute,
                                onComplete = onDone,
                            )
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = currentStep != CreateHabitStep.BASIC_INFO || formState.isValid,
                ) {
                    Text(if (stepIndex == steps.lastIndex) "Create habit" else "Next")
                }
            }
        }
    }
}

@Composable
private fun StepIndicator(total: Int, current: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        repeat(total) { index ->
            val active = index <= current
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .weight(1f)
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(
                        if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    ),
            )
        }
    }
}
