package com.habitflow.app.ui.theme

import androidx.compose.ui.graphics.Color

// HabitFlow's own palette: a calm, desaturated teal as the primary accent,
// warm neutral surfaces, and a small set of habit-card accent colors users
// can assign per habit. Deliberately avoids saturated "gamified" primaries.

val TealPrimaryLight = Color(0xFF1F6E63)
val TealPrimaryDark = Color(0xFF8FD6C6)

val OnPrimaryLight = Color(0xFFFFFFFF)
val OnPrimaryDark = Color(0xFF00382F)

val PrimaryContainerLight = Color(0xFFB6F0E1)
val PrimaryContainerDark = Color(0xFF005043)

val SecondaryLight = Color(0xFF4A635D)
val SecondaryDark = Color(0xFFB1CCC4)

val BackgroundLight = Color(0xFFFBFDFB)
val BackgroundDark = Color(0xFF0F1513)

val SurfaceLight = Color(0xFFF4F7F5)
val SurfaceDark = Color(0xFF171E1B)

val SurfaceVariantLight = Color(0xFFDBE5E1)
val SurfaceVariantDark = Color(0xFF3F4946)

val ErrorLight = Color(0xFFBA1A1A)
val ErrorDark = Color(0xFFFFB4AB)

val OutlineLight = Color(0xFF6F7975)
val OutlineDark = Color(0xFF89938F)

/** Assignable per-habit accent colors, shown in habit creation and cards. */
val HabitAccentColors: List<Color> = listOf(
    Color(0xFF1F6E63), // teal
    Color(0xFF3C6E9E), // blue
    Color(0xFF8A6D3B), // amber
    Color(0xFF7B5AA6), // violet
    Color(0xFFB1543F), // terracotta
    Color(0xFF5C7A3C), // olive
    Color(0xFF6E5A9E), // indigo
    Color(0xFF3F7A6E), // jade
)
