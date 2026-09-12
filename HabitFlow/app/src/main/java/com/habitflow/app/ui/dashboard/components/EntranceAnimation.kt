package com.habitflow.app.ui.dashboard.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.delay

/**
 * Dashboard entry animation (section 1): each top-level block fades in and
 * moves up slightly, staggered by [index] so the header, KPI row, and main
 * cards appear in sequence rather than all at once. Short duration, natural
 * (non-bouncy) easing per spec ("Do NOT use excessive bouncing").
 *
 * Runs once per composition of the element it's applied to -- correct here
 * since each Dashboard block is only created once per screen entry, not
 * re-triggered on every recomposition (state updates like a habit toggle
 * don't replay the entrance).
 */
@Composable
fun Modifier.staggeredEntrance(index: Int, baseDelayMillis: Long = 70L): Modifier {
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(index * baseDelayMillis)
        started = true
    }
    val alpha by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(durationMillis = 420),
        label = "entranceAlpha",
    )
    val offsetY by animateFloatAsState(
        targetValue = if (started) 0f else 18f,
        animationSpec = tween(durationMillis = 420),
        label = "entranceOffsetY",
    )

    return this.graphicsLayer {
        this.alpha = alpha
        translationY = offsetY
    }
}
