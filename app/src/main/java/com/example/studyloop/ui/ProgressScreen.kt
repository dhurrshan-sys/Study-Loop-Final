package com.example.studyloop.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studyloop.data.QuizResultEntity
import com.example.studyloop.ui.theme.StudyLoopTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Stores the visual style used for each performance level.
private data class PerformanceStyle(
    val icon: String,
    val label: String,
    val colour: Color
)

@Composable
fun ProgressScreen(
    results: List<QuizResultEntity>,

    // Opens the Quiz page from revision actions.
    onReviseNext: () -> Unit
) {
    // Removes invalid results that contain no questions.
    val validResults = results.filter {
        it.totalQuestions > 0
    }

    // Calculates the user's overall average percentage.
    val averagePercentage =
        if (validResults.isEmpty()) {
            0
        } else {
            validResults
                .map {
                    it.score * 100 / it.totalQuestions
                }
                .average()
                .toInt()
        }

    // Groups attempts by quiz and calculates each quiz average.
    val quizAverages = validResults
        .groupBy {
            it.quizTitle
        }
        .mapValues { entry ->
            entry.value
                .map {
                    it.score * 100 / it.totalQuestions
                }
                .average()
                .toInt()
        }

    // Identifies the strongest and weakest quizzes.
    val strongestQuiz =
        quizAverages.maxByOrNull {
            it.value
        }

    val weakestQuiz =
        quizAverages.minByOrNull {
            it.value
        }

    // LazyColumn keeps the screen smooth with many results.
    LazyColumn(
        modifier = Modifier.fillMaxSize(),

        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = 20.dp,
            bottom = 32.dp
        ),

        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Page title and explanation.
        item {
            ProgressHeader()
        }

        // Main progress statistics.
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ProgressStatCard(
                    icon = "🎯",
                    value = validResults.size.toString(),
                    label = "Attempts",
                    accentColour = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )

                ProgressStatCard(
                    icon = "📈",
                    value = "$averagePercentage%",
                    label = "Average",
                    accentColour = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (validResults.isEmpty()) {
            // Shown before the first quiz is completed.
            item {
                EmptyProgressCard()
            }
        } else {
            item {
                SectionHeading(
                    icon = "💡",
                    title = "Learning Insights"
                )
            }

            // Informational card for the best-performing quiz.
            strongestQuiz?.let { strongest ->
                item {
                    InsightCard(
                        icon = "🏆",
                        label = "Strongest Quiz",
                        quizTitle = strongest.key,
                        percentage = strongest.value,
                        accentColour = MaterialTheme.colorScheme.tertiary
                    )
                }
            }

            // Clickable recommendation for the weakest quiz.
            weakestQuiz?.let { weakest ->
                item {
                    InsightCard(
                        icon = "🔁",
                        label = "Revise Next",
                        quizTitle = weakest.key,
                        percentage = weakest.value,
                        accentColour = MaterialTheme.colorScheme.secondary,
                        actionLabel = "Open quizzes →",
                        onClick = onReviseNext
                    )
                }
            }

            item {
                SectionHeading(
                    icon = "🕘",
                    title = "Attempt History"
                )
            }

            // Displays all stored quiz attempts.
            items(
                items = validResults,
                key = { result ->
                    result.id
                }
            ) { result ->
                AttemptCard(
                    result = result,
                    onRevise = onReviseNext
                )
            }
        }
    }
}

@Composable
private fun ProgressHeader() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header icon container.
        Surface(
            modifier = Modifier.size(52.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "📊",
                    fontSize = 26.sp
                )
            }
        }

        Column {
            Text(
                text = "My Progress",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "See your results and what to revise next.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ProgressStatCard(
    icon: String,
    value: String,
    label: String,
    accentColour: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),

        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Coloured icon background.
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(
                        accentColour.copy(alpha = 0.16f)
                    ),

                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = icon,
                    fontSize = 20.sp
                )
            }

            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = accentColour
                )

                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SectionHeading(
    icon: String,
    title: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = icon,
            fontSize = 19.sp
        )

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun InsightCard(
    icon: String,
    label: String,
    quizTitle: String,
    percentage: Int,
    accentColour: Color,

    // Remains null when the card is informational.
    actionLabel: String? = null,
    onClick: (() -> Unit)? = null
) {
    // Adds clickable behaviour only when an action exists.
    val cardModifier =
        if (onClick != null) {
            Modifier
                .fillMaxWidth()
                .clickable {
                    onClick()
                }
        } else {
            Modifier.fillMaxWidth()
        }

    Card(
        modifier = cardModifier,
        shape = RoundedCornerShape(18.dp),

        colors = CardDefaults.cardColors(
            containerColor = accentColour.copy(alpha = 0.12f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Learning insight icon.
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(
                        accentColour.copy(alpha = 0.18f)
                    ),

                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = icon,
                    fontSize = 22.sp
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    color = accentColour,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = quizTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Indicates that the card can be opened.
                actionLabel?.let { action ->
                    Text(
                        text = action,
                        style = MaterialTheme.typography.labelMedium,
                        color = accentColour,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Compact percentage badge.
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = accentColour.copy(alpha = 0.18f)
            ) {
                Text(
                    text = "$percentage%",

                    modifier = Modifier.padding(
                        horizontal = 12.dp,
                        vertical = 7.dp
                    ),

                    fontWeight = FontWeight.Bold,
                    color = accentColour
                )
            }
        }
    }
}

@Composable
private fun AttemptCard(
    result: QuizResultEntity,

    // Opens the Quiz page when revision is required.
    onRevise: () -> Unit
) {
    // Calculates the percentage for this attempt.
    val percentage =
        if (result.totalQuestions > 0) {
            result.score * 100 / result.totalQuestions
        } else {
            0
        }

    // Selects a suitable icon, label and colour.
    val performance = getPerformanceStyle(percentage)

    // Converts the saved timestamp into a readable date.
    val formattedDate =
        SimpleDateFormat(
            "dd MMM yyyy, h:mm a",
            Locale.getDefault()
        ).format(
            Date(result.completedAt)
        )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),

        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Performance-level icon.
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(
                            RoundedCornerShape(14.dp)
                        )
                        .background(
                            performance.colour.copy(
                                alpha = 0.16f
                            )
                        ),

                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = performance.icon,
                        fontSize = 21.sp
                    )
                }

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    // Two lines prevent long quiz titles from being hidden.
                    Text(
                        text = result.quizTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Percentage badge.
                Surface(
                    shape = RoundedCornerShape(12.dp),

                    color = performance.colour.copy(
                        alpha = 0.16f
                    )
                ) {
                    Text(
                        text = "$percentage%",

                        modifier = Modifier.padding(
                            horizontal = 11.dp,
                            vertical = 6.dp
                        ),

                        fontWeight = FontWeight.Bold,
                        color = performance.colour
                    )
                }
            }

            // Visual representation of the quiz score.
            LinearProgressIndicator(
                progress = {
                    percentage / 100f
                },

                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp)
                    .clip(CircleShape),

                color = performance.colour,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${result.score}/${result.totalQuestions} correct",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (percentage < 40) {
                    // Weak attempts provide a direct revision action.
                    Text(
                        text = "Revise again →",

                        modifier = Modifier.clickable {
                            onRevise()
                        },

                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = performance.colour
                    )
                } else {
                    // Other attempts display their performance status.
                    Text(
                        text = performance.label,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = performance.colour
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyProgressCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),

        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),

            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "📊",
                fontSize = 40.sp
            )

            Text(
                text = "No results yet",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Complete a quiz to start tracking your progress.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Determines the appearance of an attempt based on its score.
@Composable
private fun getPerformanceStyle(
    percentage: Int
): PerformanceStyle {
    return when {
        percentage >= 80 ->
            PerformanceStyle(
                icon = "🏆",
                label = "Excellent",
                colour = MaterialTheme.colorScheme.tertiary
            )

        percentage >= 60 ->
            PerformanceStyle(
                icon = "👍",
                label = "Good",
                colour = MaterialTheme.colorScheme.primary
            )

        percentage >= 40 ->
            PerformanceStyle(
                icon = "📘",
                label = "Keep practising",
                colour = MaterialTheme.colorScheme.secondary
            )

        else ->
            PerformanceStyle(
                icon = "🔁",
                label = "Revise again",
                colour = MaterialTheme.colorScheme.error
            )
    }
}

// Displays a sample Progress screen inside Android Studio
// without requiring the emulator to run.
@Preview(
    name = "Progress Screen Preview",
    showBackground = true,
    backgroundColor = 0xFF071126
)
@Composable
private fun ProgressScreenPreview() {
    StudyLoopTheme {
        ProgressScreen(
            // These results are only used inside the preview.
            results = listOf(
                QuizResultEntity(
                    id = 1,
                    quizId = 1,
                    quizTitle = "Cloud Computing Fundamentals",
                    score = 8,
                    totalQuestions = 10,
                    completedAt = System.currentTimeMillis()
                ),
                QuizResultEntity(
                    id = 2,
                    quizId = 2,
                    quizTitle = "Database Management Practice Questions",
                    score = 3,
                    totalQuestions = 10,
                    completedAt = System.currentTimeMillis()
                )
            ),

            // Navigation is not required inside the preview.
            onReviseNext = {}
        )
    }
}