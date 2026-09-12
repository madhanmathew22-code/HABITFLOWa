package com.habitflow.app.ui.insights

import androidx.compose.runtime.Composable
import com.habitflow.app.ui.common.PlaceholderScreen

/**
 * TODO(sections 15-16): consistency stats, heatmap, and the local
 * rule-based Smart Insights engine, built on top of StreakEngine.evaluate()
 * across all habits. StreakEngine already returns everything the charts
 * need (completionRate, perfectWeeks, perfectMonths, etc.) -- this screen
 * is the next natural piece to build.
 */
@Composable
fun InsightsScreen() {
    PlaceholderScreen(
        title = "Complete a few habits to unlock insights.",
        subtitle = "Coming in the next iteration -- the data layer is already wired.",
    )
}
