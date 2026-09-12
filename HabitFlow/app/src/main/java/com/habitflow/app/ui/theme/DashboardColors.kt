package com.habitflow.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * The Dashboard screen (ui/dashboard) intentionally does NOT use
 * MaterialTheme.colorScheme. The reference screenshot is a specific deep
 * navy "premium analytics" look -- distinct from HabitFlow's teal brand
 * theme used everywhere else in the app -- and section: "Keep the reference
 * screenshot as the visual design source of truth" asks for that exact look
 * to be preserved rather than re-derived from the app's normal palette.
 *
 * [DashboardPalette] bundles both a dark and light variant so the Dashboard
 * still responds to the theme toggle (section 11), animating between them,
 * without pulling in Material You / dynamic color.
 */
data class DashboardPalette(
    val background: Color,
    val sidebarBackground: Color,
    val cardBackground: Color,
    val cardBackgroundElevated: Color,
    val cardBorder: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val accentGreen: Color,
    val accentGreenSoft: Color,
    val accentBlue: Color,
    val accentBlueSoft: Color,
    val accentPurple: Color,
    val accentPurpleSoft: Color,
    val accentPink: Color,
    val accentPinkSoft: Color,
    val accentCyan: Color,
    val ringTrack: Color,
    val chartBarTop: Color,
    val chartBarBottom: Color,
    val sidebarSelectedStart: Color,
    val sidebarSelectedEnd: Color,
    val isDark: Boolean,
)

val DashboardPaletteDark = DashboardPalette(
    background = Color(0xFF0A0F1E),
    sidebarBackground = Color(0xFF0A0F1E),
    cardBackground = Color(0xFF121A2E),
    cardBackgroundElevated = Color(0xFF16203A),
    cardBorder = Color(0xFF232C45),
    textPrimary = Color(0xFFF2F5FA),
    textSecondary = Color(0xFFA9B3CC),
    textTertiary = Color(0xFF6E7796),
    accentGreen = Color(0xFF2ED18F),
    accentGreenSoft = Color(0xFF1B3B33),
    accentBlue = Color(0xFF3D8BFF),
    accentBlueSoft = Color(0xFF1B2E52),
    accentPurple = Color(0xFF8B6BF2),
    accentPurpleSoft = Color(0xFF2C2650),
    accentPink = Color(0xFFF2537B),
    accentPinkSoft = Color(0xFF3E2036),
    accentCyan = Color(0xFF3EE0C8),
    ringTrack = Color(0xFF1E2740),
    chartBarTop = Color(0xFF4E96FF),
    chartBarBottom = Color(0xFF2C5FD6),
    sidebarSelectedStart = Color(0xFF4E63F0),
    sidebarSelectedEnd = Color(0xFF6B4FE0),
    isDark = true,
)

val DashboardPaletteLight = DashboardPalette(
    background = Color(0xFFF3F5FA),
    sidebarBackground = Color(0xFFFFFFFF),
    cardBackground = Color(0xFFFFFFFF),
    cardBackgroundElevated = Color(0xFFFFFFFF),
    cardBorder = Color(0xFFE3E7F2),
    textPrimary = Color(0xFF171B2E),
    textSecondary = Color(0xFF5A6180),
    textTertiary = Color(0xFF9298B3),
    accentGreen = Color(0xFF1DAE79),
    accentGreenSoft = Color(0xFFDBF6EA),
    accentBlue = Color(0xFF2E6FE0),
    accentBlueSoft = Color(0xFFDCE7FC),
    accentPurple = Color(0xFF7358D6),
    accentPurpleSoft = Color(0xFFE7E1FA),
    accentPink = Color(0xFFE0416B),
    accentPinkSoft = Color(0xFFFBDCE4),
    accentCyan = Color(0xFF1BB7A0),
    ringTrack = Color(0xFFE5E9F5),
    chartBarTop = Color(0xFF4E96FF),
    chartBarBottom = Color(0xFF8FB8FF),
    sidebarSelectedStart = Color(0xFF4E63F0),
    sidebarSelectedEnd = Color(0xFF6B4FE0),
    isDark = false,
)
