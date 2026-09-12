package com.habitflow.app.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.habitflow.app.ui.calendar.CalendarScreen
import com.habitflow.app.ui.dashboard.DashboardDestination
import com.habitflow.app.ui.dashboard.DashboardScreen
import com.habitflow.app.ui.dashboard.DashboardSettingsPlaceholder
import com.habitflow.app.ui.dashboard.components.DashboardSidebar
import com.habitflow.app.ui.habits.HabitsScreen
import com.habitflow.app.ui.habits.create.CreateHabitRoute
import com.habitflow.app.ui.habits.detail.HabitDetailScreen
import com.habitflow.app.ui.habits.edit.HabitEditScreen
import com.habitflow.app.ui.insights.InsightsScreen
import com.habitflow.app.ui.journal.JournalScreen
import com.habitflow.app.ui.journal.compose.JournalComposeScreen
import com.habitflow.app.ui.theme.DashboardPaletteDark
import com.habitflow.app.ui.theme.DashboardPaletteLight

/** Maps a sidebar item to the underlying nav-graph route it opens. */
private fun DashboardDestination.route(): String = when (this) {
    DashboardDestination.DASHBOARD -> HabitFlowRoutes.DASHBOARD
    DashboardDestination.HABITS -> HabitFlowDestination.HABITS.route
    DashboardDestination.CALENDAR -> HabitFlowDestination.CALENDAR.route
    DashboardDestination.ANALYTICS -> HabitFlowDestination.INSIGHTS.route
    DashboardDestination.SETTINGS -> HabitFlowRoutes.SETTINGS
}

/**
 * Note: [HabitFlowDestination] (Today/Habits/Calendar/Journal/Insights,
 * originally meant for a phone-style bottom nav bar) is superseded here by
 * the screenshot's permanent sidebar over [DashboardDestination]
 * (Dashboard/Habits/Calendar/Analytics/Settings). Habits, Calendar, and
 * Insights are shared between both sets and keep their existing screens and
 * routes; Journal has no sidebar entry in the reference design, but its
 * route/composer are left reachable in the graph rather than deleted.
 */
@Composable
fun HabitFlowNavHost(navController: NavHostController = rememberNavController()) {
    // Hoisted above the NavHost (rather than living inside DashboardViewModel)
    // so the sidebar -- which persists across every sidebar destination, not
    // just the Dashboard screen itself -- can read/drive the same dark/light
    // state as the Dashboard content's theme toggle button (section 11).
    var isDarkTheme by rememberSaveable { mutableStateOf(true) }
    val palette = if (isDarkTheme) DashboardPaletteDark else DashboardPaletteLight

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val topLevelRoutes = remember {
        setOf(
            HabitFlowRoutes.DASHBOARD,
            HabitFlowDestination.HABITS.route,
            HabitFlowDestination.CALENDAR.route,
            HabitFlowDestination.INSIGHTS.route,
            HabitFlowRoutes.SETTINGS,
            HabitFlowDestination.JOURNAL.route,
        )
    }
    val isOnTopLevelScreen = currentDestination?.hierarchy?.any { dest -> dest.route?.let { topLevelRoutes.contains(it) } == true } == true
    val selectedSidebarItem = DashboardDestination.entries.firstOrNull { dest ->
        currentDestination?.hierarchy?.any { it.route == dest.route() } == true
    }

    Row(modifier = Modifier.fillMaxSize()) {
        if (isOnTopLevelScreen) {
            DashboardSidebar(
                selected = selectedSidebarItem,
                onSelect = { destination ->
                    navController.navigate(destination.route()) {
                        popUpTo(HabitFlowRoutes.DASHBOARD) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                palette = palette,
                footer = {
                    // Journal has no slot in the reference sidebar's 5 items,
                    // but dropping it from the nav graph would silently take
                    // the feature away -- so it gets a small footer link
                    // instead of a full sidebar row.
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                navController.navigate(HabitFlowDestination.JOURNAL.route) {
                                    popUpTo(HabitFlowRoutes.DASHBOARD) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                    ) {
                        Icon(Icons.Filled.MenuBook, contentDescription = "Journal", tint = palette.textSecondary, modifier = Modifier.size(20.dp))
                        Text(text = "Journal", color = palette.textSecondary)
                    }
                },
            )
        }

        Box(modifier = Modifier.weight(1f).fillMaxSize()) {
            NavHost(
                navController = navController,
                startDestination = HabitFlowRoutes.DASHBOARD,
            ) {
                composable(HabitFlowRoutes.DASHBOARD) {
                    DashboardScreen(
                        isDarkTheme = isDarkTheme,
                        onToggleTheme = { isDarkTheme = !isDarkTheme },
                        onOpenHabitCreation = { navController.navigate(HabitFlowRoutes.CREATE_HABIT) },
                    )
                }
                composable(HabitFlowDestination.HABITS.route) {
                    HabitsScreen(
                        onOpenHabitCreation = { navController.navigate(HabitFlowRoutes.CREATE_HABIT) },
                        onOpenHabitDetail = { habitId -> navController.navigate(HabitFlowRoutes.habitDetail(habitId)) },
                    )
                }
                composable(HabitFlowDestination.CALENDAR.route) { CalendarScreen() }
                composable(HabitFlowDestination.INSIGHTS.route) { InsightsScreen() }
                composable(HabitFlowRoutes.SETTINGS) { DashboardSettingsPlaceholder(palette = palette) }
                composable(HabitFlowDestination.JOURNAL.route) {
                    JournalScreen(onOpenComposer = { navController.navigate(HabitFlowRoutes.JOURNAL_COMPOSE) })
                }

                // Add Habit animation (section 7): fades/scales up rather
                // than the default slide-in-from-the-side transition, so it
                // reads as a modal sheet opening on top of the dashboard.
                composable(
                    route = HabitFlowRoutes.CREATE_HABIT,
                    enterTransition = {
                        fadeIn(animationSpec = tween(220)) +
                            scaleIn(initialScale = 0.94f, animationSpec = tween(220))
                    },
                    exitTransition = {
                        fadeOut(animationSpec = tween(180)) +
                            scaleOut(targetScale = 0.94f, animationSpec = tween(180))
                    },
                ) {
                    CreateHabitRoute(onDone = { navController.popBackStack() })
                }

                composable(HabitFlowRoutes.JOURNAL_COMPOSE) {
                    JournalComposeScreen(onDone = { navController.popBackStack() })
                }

                composable(
                    route = HabitFlowRoutes.HABIT_DETAIL,
                    arguments = listOf(
                        navArgument(HabitFlowRoutes.HABIT_DETAIL_ARG) { type = NavType.LongType },
                    ),
                ) {
                    HabitDetailScreen(
                        onBack = { navController.popBackStack() },
                        onEdit = { habitId -> navController.navigate(HabitFlowRoutes.habitEdit(habitId)) },
                        onDuplicated = { newHabitId ->
                            // Replace the current detail screen with the new copy's
                            // detail screen rather than stacking on top of it.
                            navController.navigate(HabitFlowRoutes.habitDetail(newHabitId)) {
                                popUpTo(HabitFlowRoutes.HABIT_DETAIL) { inclusive = true }
                            }
                        },
                    )
                }

                composable(
                    route = HabitFlowRoutes.HABIT_EDIT,
                    arguments = listOf(
                        navArgument(HabitFlowRoutes.HABIT_DETAIL_ARG) { type = NavType.LongType },
                    ),
                ) {
                    HabitEditScreen(onDone = { navController.popBackStack() })
                }
            }
        }
    }
}
