package com.habitflow.app.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitflow.app.ui.theme.DashboardPalette

/**
 * Placeholder for the sidebar's "Settings" item. The reference screenshot
 * shows a settings icon in the sidebar but no settings screen content, and
 * there's no existing Settings screen or domain model (theme, notifications,
 * etc. aren't modeled anywhere in the app) to build a real one against --
 * so this stays a clearly-labeled placeholder rather than inventing
 * settings that don't do anything.
 */
@Composable
fun DashboardSettingsPlaceholder(palette: DashboardPalette, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(palette.background)
            .padding(28.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Settings", color = palette.textPrimary, fontWeight = FontWeight.Bold, fontSize = 22.sp)
            Text(
                text = "Coming soon.",
                color = palette.textSecondary,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}
