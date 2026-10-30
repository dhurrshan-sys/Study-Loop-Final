package com.example.studyloop.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studyloop.data.FlashcardEntity
import com.example.studyloop.data.QuizResultEntity
import com.example.studyloop.data.RevisionSessionEntity
import com.example.studyloop.data.SubjectEntity
import com.example.studyloop.data.TopicEntity
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.delay

// HOME DASHBOARD:
// Displays real learning information obtained from the Room database.
@Composable
fun HomeScreen(
    subjects: List<SubjectEntity>,
    recentTopics: List<TopicEntity>,
    flashcards: List<FlashcardEntity>,
    sessions: List<RevisionSessionEntity>,
    quizResults: List<QuizResultEntity>,
    onOpenSubjects: () -> Unit,
    onOpenSubject: (Int) -> Unit,
    onOpenFlashcards: () -> Unit,
    onOpenPlanner: () -> Unit,
    onOpenQuiz: () -> Unit,
    onOpenProgress: () -> Unit
) {
    // Stores the current hour for the automatic greeting.
    var currentHour by remember {
        mutableStateOf(LocalTime.now().hour)
    }

    // Updates the device time once per minute.
    LaunchedEffect(Unit) {
        while (true) {
            currentHour = LocalTime.now().hour
            delay(60_000L)
        }
    }

    val greeting = when (currentHour) {
        in 5..11 -> "Good morning!"
        in 12..17 -> "Good afternoon!"
        else -> "Good evening!"
    }

    val greetingSymbol =
        if (currentHour in 5..17) "☀️" else "🌙"

    val today = LocalDate.now()

    // Calculates consecutive quiz-practice days.
    val practiceStreak = remember(quizResults) {
        calculatePracticeStreak(quizResults)
    }

    // Finds revision schedules matching today's date.
    val todaySessions = remember(sessions, today) {
        sessions.filter { session ->
            isScheduledForToday(
                dueDate = session.dueDate,
                today = today
            )
        }
    }

    // Finds the quiz attempt with the lowest percentage.
    val lowestResult = remember(quizResults) {
        quizResults
            .filter { it.totalQuestions > 0 }
            .minByOrNull { result ->
                result.score.toDouble() /
                        result.totalQuestions.toDouble()
            }
    }

    val lowestPercentage = lowestResult?.let { result ->
        (result.score * 100) / result.totalQuestions
    }

    // Avoids calling a high score a weak performance.
    val insightTitle = when {
        lowestResult == null -> "Quiz Insight"
        lowestPercentage != null &&
                lowestPercentage >= 80 -> "Strong Performance"
        else -> "Needs More Practice"
    }

    val insightDescription = when {
        lowestResult == null ->
            "Complete a quiz to receive a personalised learning insight."

        lowestPercentage != null &&
                lowestPercentage >= 80 ->
            "Your lowest result is ${lowestResult.quizTitle} " +
                    "at $lowestPercentage%. Keep practising!"

        else ->
            "Revise ${lowestResult?.quizTitle} again. " +
                    "Your lowest result is $lowestPercentage%."
    }

    // Allows the complete dashboard to scroll vertically.
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Application identity.
        item {
            Column {
                Text(
                    text = "StudyLoop",
                    style = MaterialTheme.typography.headlineMedium
                )

                Text(
                    text = "Study Anywhere, Remember More",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Personalised greeting card.
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor =
                        MaterialTheme.colorScheme.primaryContainer,
                    contentColor =
                        MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement =
                            Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = greetingSymbol,
                            fontSize = 40.sp
                        )

                        Text(
                            text = greeting,
                            style =
                                MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Text(
                        text = "Keep learning today!",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = when (subjects.size) {
                            0 ->
                                "Add your first subject to begin revising."

                            1 ->
                                "You currently have 1 saved subject."

                            else ->
                                "You currently have " +
                                        "${subjects.size} saved subjects."
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        // Starting action shown only when no subjects exist.
        if (subjects.isEmpty()) {
            item {
                Button(
                    onClick = onOpenSubjects,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add Your First Subject")
                }
            }
        }

        item {
            Text(
                text = "Your Learning Dashboard",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // REAL FEATURE 1:
        // Calculates consecutive quiz-practice days.
        item {
            DashboardCard(
                symbol = "🔥",
                title = "Practice Streak",
                value = when (practiceStreak) {
                    0 -> "Start your streak today"
                    1 -> "1 consecutive day"
                    else -> "$practiceStreak consecutive days"
                },
                description = if (practiceStreak == 0) {
                    "Complete a quiz today to begin tracking your streak."
                } else {
                    "Your streak increases when you practise on " +
                            "consecutive days."
                },
                actionText = "Tap to practise a quiz",
                onClick = onOpenQuiz
            )
        }

        // REAL FEATURE 2:
        // Shows the actual number of flashcards in Room.
        item {
            DashboardCard(
                symbol = "🎴",
                title = "Flashcard Review",
                value = when (flashcards.size) {
                    0 -> "No flashcards saved"
                    1 -> "1 flashcard ready"
                    else -> "${flashcards.size} flashcards ready"
                },
                description = if (flashcards.isEmpty()) {
                    "Create flashcards from your subjects and topics."
                } else {
                    "Review your saved cards to strengthen your memory."
                },
                actionText = "Tap to review flashcards",
                onClick = onOpenFlashcards
            )
        }

        // REAL FEATURE 3:
        // Reads today's revision schedules from Room.
        item {
            DashboardCard(
                symbol = "📅",
                title = "Today's Review",
                value = when (todaySessions.size) {
                    0 -> "Nothing scheduled today"
                    1 -> todaySessions.first().title
                    else ->
                        "${todaySessions.size} revision activities"
                },
                description = when {
                    todaySessions.isEmpty() ->
                        "Use the planner to schedule your next revision."

                    todaySessions.size == 1 ->
                        "You have one revision activity scheduled today."

                    else ->
                        "You have ${todaySessions.size} activities " +
                                "scheduled today."
                },
                actionText = "Tap to open planner",
                onClick = onOpenPlanner
            )
        }

        // REAL FEATURE 4:
        // Analyses saved quiz results and identifies the lowest score.
        item {
            DashboardCard(
                symbol = if (
                    lowestPercentage != null &&
                    lowestPercentage >= 80
                ) {
                    "🏆"
                } else {
                    "🎯"
                },
                title = insightTitle,
                value = when {
                    lowestResult == null ->
                        "No quiz information yet"

                    else ->
                        "${lowestResult.quizTitle}: " +
                                "$lowestPercentage%"
                },
                description = insightDescription,
                actionText = "Tap to view full progress",
                onClick = onOpenProgress
            )
        }

        // Recently created topic section.
        item {
            Text(
                text = "Recently Added Topics",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 12.dp)
            )
        }

        if (recentTopics.isEmpty()) {
            // Empty-state information.
            item {
                Text(
                    text =
                        "Add a topic inside a subject to see it here.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            // Displays the three most recent topics.
            items(
                items = recentTopics.take(3),
                key = { topic ->
                    "topic_${topic.id}"
                }
            ) { topic ->
                val subjectName = subjects
                    .find { subject ->
                        subject.id == topic.subjectId
                    }
                    ?.name ?: "Subject"

                Card(
                    onClick = {
                        onOpenSubject(topic.subjectId)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement =
                            Arrangement.spacedBy(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement =
                                Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = topic.name,
                                style =
                                    MaterialTheme.typography.titleMedium
                            )

                            Text(
                                text = subjectName,
                                style =
                                    MaterialTheme.typography.bodyMedium,
                                color =
                                    MaterialTheme.colorScheme
                                        .onSurfaceVariant
                            )
                        }

                        Badge(
                            containerColor =
                                MaterialTheme.colorScheme
                                    .secondaryContainer,
                            contentColor =
                                MaterialTheme.colorScheme
                                    .onSecondaryContainer
                        ) {
                            Text(
                                text = "Topic",
                                modifier = Modifier.padding(
                                    horizontal = 6.dp,
                                    vertical = 2.dp
                                )
                            )
                        }
                    }
                }
            }

            if (recentTopics.size > 3) {
                item {
                    Text(
                        text =
                            "Open Subjects to view all your topics.",
                        style = MaterialTheme.typography.bodyMedium,
                        color =
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// Reusable layout used for each dashboard feature.
@Composable
private fun DashboardCard(
    symbol: String,
    title: String,
    value: String,
    description: String,
    actionText: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = symbol,
                fontSize = 32.sp
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )

                // Displays the main calculated value prominently.
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = actionText,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

// Calculates consecutive days containing at least one quiz attempt.
private fun calculatePracticeStreak(
    results: List<QuizResultEntity>
): Int {
    if (results.isEmpty()) return 0

    val zoneId = ZoneId.systemDefault()

    // Converts timestamps into unique calendar dates.
    val practiceDates = results
        .map { result ->
            Instant.ofEpochMilli(result.completedAt)
                .atZone(zoneId)
                .toLocalDate()
        }
        .toSet()

    val today = LocalDate.now(zoneId)

    // A streak remains active when the last attempt was today or yesterday.
    var dateToCheck = when {
        today in practiceDates -> today
        today.minusDays(1) in practiceDates -> today.minusDays(1)
        else -> return 0
    }

    var streak = 0

    // Counts backwards until a date without practice is found.
    while (dateToCheck in practiceDates) {
        streak++
        dateToCheck = dateToCheck.minusDays(1)
    }

    return streak
}

// Checks whether a stored planner date represents today.
private fun isScheduledForToday(
    dueDate: String,
    today: LocalDate
): Boolean {
    val cleanDate = dueDate.trim()

    if (cleanDate.equals("Today", ignoreCase = true)) {
        return true
    }

    // Supports the simple date styles currently accepted by Planner.
    val acceptedFormats = listOf(
        DateTimeFormatter.ofPattern(
            "d MMM",
            Locale.ENGLISH
        ),
        DateTimeFormatter.ofPattern(
            "dd MMM",
            Locale.ENGLISH
        ),
        DateTimeFormatter.ofPattern(
            "d MMM yyyy",
            Locale.ENGLISH
        ),
        DateTimeFormatter.ofPattern(
            "dd MMM yyyy",
            Locale.ENGLISH
        )
    )

    return acceptedFormats.any { formatter ->
        val formattedToday = today.format(formatter)

        cleanDate.equals(
            formattedToday,
            ignoreCase = true
        )
    }
}