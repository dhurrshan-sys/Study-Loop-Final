package com.example.studyloop.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// STUDYLOOP MIDNIGHT BLUE THEME:
// Uses electric blue, lavender and cyan accents.
private val StudyLoopMidnightScheme = darkColorScheme(

    // Main electric-blue colour for buttons and selected items.
    primary = Color(0xFF7DA7FF),
    onPrimary = Color(0xFF002B69),
    primaryContainer = Color(0xFF203F75),
    onPrimaryContainer = Color(0xFFD8E2FF),

    // Lavender supporting colour.
    secondary = Color(0xFFC4B5FD),
    onSecondary = Color(0xFF2E1B69),
    secondaryContainer = Color(0xFF44327A),
    onSecondaryContainer = Color(0xFFE9DDFF),

    // Cyan colour for highlights and progress information.
    tertiary = Color(0xFF67E8F9),
    onTertiary = Color(0xFF00363D),
    tertiaryContainer = Color(0xFF14515B),
    onTertiaryContainer = Color(0xFFA5F3FC),

    // Main midnight background.
    background = Color(0xFF0A0F1E),
    onBackground = Color(0xFFF1F5F9),

    // Standard surfaces and cards.
    surface = Color(0xFF0A0F1E),
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = Color(0xFF202A44),
    onSurfaceVariant = Color(0xFFC4CBE0),

    // Surface levels create separation between interface elements.
    surfaceContainerLowest = Color(0xFF070B16),
    surfaceContainerLow = Color(0xFF101729),
    surfaceContainer = Color(0xFF171E32),
    surfaceContainerHigh = Color(0xFF202A44),
    surfaceContainerHighest = Color(0xFF2A3553),

    // Borders and dividers.
    outline = Color(0xFF8993AC),
    outlineVariant = Color(0xFF3B4663),

    // Errors and deletion actions.
    error = Color(0xFFFF8A80),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color(0xFFFFDAD6),

    // Additional Material colours.
    surfaceTint = Color(0xFF7DA7FF),
    inverseSurface = Color(0xFFE2E8F0),
    inverseOnSurface = Color(0xFF182033),
    inversePrimary = Color(0xFF345FA8)
)

// Applies the dark Midnight Blue theme to the whole application.
@Composable
fun StudyLoopTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = StudyLoopMidnightScheme,
        typography = Typography,
        content = content
    )
}