package com.habitflow.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Today
import androidx.compose.ui.graphics.vector.ImageVector

/** Top-level destinations shown in the bottom navigation bar (section 4). */
enum class HabitFlowDestination(
    val route: String,
    val labelRes: Int,
    val icon: ImageVector,
) {
    TODAY("today", com.habitflow.app.R.string.nav_today, Icons.Filled.Today),
    HABITS("habits", com.habitflow.app.R.string.nav_habits, Icons.Filled.Checklist),
    CALENDAR("calendar", com.habitflow.app.R.string.nav_calendar, Icons.Filled.CalendarMonth),
    JOURNAL("journal", com.habitflow.app.R.string.nav_journal, Icons.Filled.MenuBook),
    INSIGHTS("insights", com.habitflow.app.R.string.nav_insights, Icons.Filled.AutoGraph),
}

object HabitFlowRoutes {
    const val DASHBOARD = "dashboard"
    const val SETTINGS = "settings"
    const val CREATE_HABIT = "habit/create"
    const val HABIT_DETAIL_ARG = "habitId"
    const val HABIT_DETAIL = "habit/{$HABIT_DETAIL_ARG}"
    const val HABIT_EDIT = "habit/{$HABIT_DETAIL_ARG}/edit"
    const val JOURNAL_COMPOSE = "journal/compose"

    fun habitDetail(habitId: Long) = "habit/$habitId"
    fun habitEdit(habitId: Long) = "habit/$habitId/edit"
}
