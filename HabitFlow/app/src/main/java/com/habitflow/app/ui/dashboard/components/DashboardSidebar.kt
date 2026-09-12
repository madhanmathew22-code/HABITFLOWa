package com.habitflow.app.ui.dashboard.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitflow.app.ui.dashboard.DashboardDestination
import com.habitflow.app.ui.theme.DashboardPalette

/**
 * The permanent left sidebar from the reference screenshot (section 8).
 * Selection uses a horizontal gradient pill that slides/fades between
 * items rather than popping, and each item gets a small icon/text color
 * animation and a press-scale micro-interaction (section 12).
 *
 * [footer] is an escape hatch for app features the reference sidebar
 * doesn't show (here: Journal) so wiring this sidebar in doesn't silently
 * remove access to them -- see [com.habitflow.app.ui.navigation.HabitFlowNavHost].
 */
@Composable
fun DashboardSidebar(
    selected: DashboardDestination?,
    onSelect: (DashboardDestination) -> Unit,
    palette: DashboardPalette,
    modifier: Modifier = Modifier,
    footer: (@Composable () -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(240.dp)
            .background(palette.sidebarBackground)
            .padding(vertical = 24.dp, horizontal = 16.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 8.dp, bottom = 32.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(
                        Brush.linearGradient(listOf(palette.accentBlue, palette.accentGreen)),
                    ),
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "HabitFlow",
                color = palette.textPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
            )
        }

        DashboardDestination.entries.forEach { destination ->
            SidebarItem(
                destination = destination,
                isSelected = destination == selected,
                onClick = { onSelect(destination) },
                palette = palette,
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        Spacer(modifier = Modifier.weight(1f))
        footer?.invoke()
    }
}

@Composable
private fun SidebarItem(
    destination: DashboardDestination,
    isSelected: Boolean,
    onClick: () -> Unit,
    palette: DashboardPalette,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = tween(120),
        label = "sidebarPressScale",
    )

    val background by animateColorAsState(
        targetValue = if (isSelected) palette.sidebarSelectedStart else Color.Transparent,
        animationSpec = tween(220),
        label = "sidebarItemBg",
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else palette.textSecondary,
        animationSpec = tween(220),
        label = "sidebarItemContent",
    )

    val icon = when (destination) {
        DashboardDestination.DASHBOARD -> Icons.Filled.Dashboard
        DashboardDestination.HABITS -> Icons.Filled.Checklist
        DashboardDestination.CALENDAR -> Icons.Filled.CalendarMonth
        DashboardDestination.ANALYTICS -> Icons.Filled.AutoGraph
        DashboardDestination.SETTINGS -> Icons.Filled.Settings
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .scale(pressScale)
            .clip(RoundedCornerShape(12.dp))
            .then(
                if (isSelected) {
                    Modifier.background(
                        Brush.horizontalGradient(
                            listOf(palette.sidebarSelectedStart, palette.sidebarSelectedEnd),
                        ),
                    )
                } else {
                    Modifier.background(background)
                },
            )
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Icon(icon, contentDescription = destination.label, tint = contentColor, modifier = Modifier.size(20.dp))
        Text(text = destination.label, color = contentColor, fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal)
    }
}
