package com.habitflow.app.ui.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Maps the free-form `iconKey` strings stored on [com.habitflow.app.data.local.entity.HabitEntity]
 * and [com.habitflow.app.data.local.entity.CategoryEntity] to an actual
 * ImageVector. Previously nothing did this -- habit/category icons were
 * stored but never rendered anywhere.
 *
 * Kept as a flat object rather than an enum on the entity itself so adding
 * an icon later never requires a Room migration -- iconKey is just a
 * string column, and an unknown key safely falls back to [DEFAULT].
 */
object IconRegistry {

    val DEFAULT: ImageVector = Icons.Filled.CheckCircle

    private val keyToIcon: Map<String, ImageVector> = mapOf(
        "check_circle" to Icons.Filled.CheckCircle,
        "water_drop" to Icons.Filled.LocalDrink,
        "book" to Icons.Filled.Book,
        "run" to Icons.Filled.DirectionsRun,
        "fitness" to Icons.Filled.FitnessCenter,
        "meditation" to Icons.Filled.SelfImprovement,
        "mindfulness" to Icons.Filled.Spa,
        "work" to Icons.Filled.Work,
        "code" to Icons.Filled.Code,
        "sleep" to Icons.Filled.Bedtime,
        "finance" to Icons.Filled.MonetizationOn,
        "social" to Icons.Filled.Groups,
        "energy" to Icons.Filled.Bolt,
        "health" to Icons.Filled.Favorite,
        "study" to Icons.Filled.School,
        "personal" to Icons.Filled.Person,
        "lifestyle" to Icons.Filled.Star,
    )

    /** Every selectable icon, in a stable order, for icon-picker UI. */
    val selectableKeys: List<String> = keyToIcon.keys.toList()

    fun iconFor(key: String?): ImageVector = keyToIcon[key] ?: DEFAULT
}
