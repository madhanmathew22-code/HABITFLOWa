package com.habitflow.app.ui.dashboard

/**
 * The five permanent sidebar items from the reference screenshot. This is
 * deliberately separate from [com.habitflow.app.ui.navigation.HabitFlowDestination]
 * (the phone-form-factor bottom-nav set, which also includes Journal but has
 * no Settings): the Dashboard's sidebar is a different information
 * architecture layered on top of the same underlying screens.
 */
enum class DashboardDestination(val label: String) {
    DASHBOARD("Dashboard"),
    HABITS("Habits"),
    CALENDAR("Calendar"),
    ANALYTICS("Analytics"),
    SETTINGS("Settings"),
}
