package com.example.studyloop.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// DHURRSHAN'S PART: Shared bottom navigation for the four main screens.
@Composable
fun BottomNavigationBar(
    selectedScreen: String,
    onScreenSelected: (String) -> Unit
) {
    // Screen names must match the navigation routes in MainActivity.
    val screens = listOf(
        "Home" to "🏠",
        "Subjects" to "📚",
        "Planner" to "📅",
        "Quiz" to "📝",
        "Progress" to "📈"
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 0.dp
    ) {
        screens.forEach { (name, symbol) ->
            val isSelected = selectedScreen == name

            NavigationBarItem(
                selected = isSelected,

                // Ask MainActivity to open the selected screen.
                onClick = { onScreenSelected(name) },

                icon = {
                    Text(
                        text = symbol,
                        fontSize = 24.sp
                    )
                },

                label = {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) {
                            FontWeight.Bold
                        } else {
                            FontWeight.Normal
                        }
                    )
                },

                // Highlight the active tab using the shared green palette.
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
