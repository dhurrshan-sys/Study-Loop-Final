package com.example.studyloop

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.studyloop.ui.BottomNavigationBar
import com.example.studyloop.ui.FlashcardScreen
import com.example.studyloop.ui.HomeScreen
import com.example.studyloop.ui.NotesScreen
import com.example.studyloop.ui.PlannerScreen
import com.example.studyloop.ui.ProgressScreen
import com.example.studyloop.ui.QuizScreen
import com.example.studyloop.ui.SubjectsScreen
import com.example.studyloop.ui.TopicScreen
import com.example.studyloop.ui.theme.StudyLoopTheme
import com.example.studyloop.viewmodel.FlashcardViewModel
import com.example.studyloop.viewmodel.NoteViewModel
import com.example.studyloop.viewmodel.QuizViewModel
import com.example.studyloop.viewmodel.RevisionViewModel
import com.example.studyloop.viewmodel.SubjectViewModel
import com.example.studyloop.viewmodel.TopicViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// Main entry point for the StudyLoop application.
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Allows StudyLoop to display behind the system bars.
        enableEdgeToEdge()

        // Keeps status-bar and navigation-bar icons white.
        WindowCompat.getInsetsController(
            window,
            window.decorView
        ).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }

        setContent {
            StudyLoopTheme {

                // Controls navigation between all application screens.
                val navController = rememberNavController()

                // Creates each ViewModel used by StudyLoop.
                val subjectViewModel:
                        SubjectViewModel = viewModel()

                val topicViewModel:
                        TopicViewModel = viewModel()

                val revisionViewModel:
                        RevisionViewModel = viewModel()

                val flashcardViewModel:
                        FlashcardViewModel = viewModel()

                val quizViewModel:
                        QuizViewModel = viewModel()

                val noteViewModel:
                        NoteViewModel = viewModel()

                // Observes information from the Room database.
                val subjects by
                subjectViewModel.subjects.collectAsState()

                val recentTopics by
                topicViewModel.recentTopics.collectAsState()

                val sessions by
                revisionViewModel.sessions.collectAsState()

                val flashcards by
                flashcardViewModel.flashcards.collectAsState()

                val quizzes by
                quizViewModel.quizzes.collectAsState()

                val quizResults by
                quizViewModel.results.collectAsState()

                // Displays feedback messages to the user.
                val snackbarHostState = remember {
                    SnackbarHostState()
                }

                // Receives subject-related feedback.
                LaunchedEffect(subjectViewModel) {
                    subjectViewModel.messages.collect { message ->
                        snackbarHostState.showSnackbar(message)
                    }
                }

                // Receives topic-related feedback.
                LaunchedEffect(topicViewModel) {
                    topicViewModel.messages.collect { message ->
                        snackbarHostState.showSnackbar(message)
                    }
                }

                // Receives planner-related feedback.
                LaunchedEffect(revisionViewModel) {
                    revisionViewModel.messages.collect { message ->
                        snackbarHostState.showSnackbar(message)
                    }
                }

                // Receives flashcard-related feedback.
                LaunchedEffect(flashcardViewModel) {
                    flashcardViewModel.messages.collect { message ->
                        snackbarHostState.showSnackbar(message)
                    }
                }

                // Receives quiz and progress feedback.
                LaunchedEffect(quizViewModel) {
                    quizViewModel.messages.collect { message ->
                        snackbarHostState.showSnackbar(message)
                    }
                }

                // Receives note-related feedback.
                LaunchedEffect(noteViewModel) {
                    noteViewModel.messages.collect { message ->
                        snackbarHostState.showSnackbar(message)
                    }
                }

                // Reads the route currently displayed.
                val currentRoute =
                    navController
                        .currentBackStackEntryAsState()
                        .value
                        ?.destination
                        ?.route

                // Keeps the correct bottom item selected
                // when viewing a secondary screen.
                val selectedScreen =
                    when (currentRoute) {
                        "SubjectDetail/{subjectId}" ->
                            "Subjects"

                        "Flashcards/{topicId}" ->
                            "Subjects"

                        "Notes/{topicId}" ->
                            "Subjects"

                        "Flashcards" ->
                            "Home"

                        else ->
                            currentRoute ?: "Home"
                    }

                // Opens a main tab without duplicating it.
                val openTab: (String) -> Unit = { screen ->
                    navController.navigate(screen) {
                        popUpTo("Home") {
                            saveState = true
                        }

                        launchSingleTop = true
                        restoreState = true
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),

                    snackbarHost = {
                        SnackbarHost(
                            hostState = snackbarHostState
                        )
                    },

                    bottomBar = {
                        BottomNavigationBar(
                            selectedScreen = selectedScreen,
                            onScreenSelected = openTab
                        )
                    }
                ) { innerPadding ->

                    NavHost(
                        navController = navController,
                        startDestination = "Home",

                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        // ---------------------------------------------
                        // HOME DASHBOARD
                        // ---------------------------------------------

                        composable("Home") {
                            HomeScreen(
                                subjects = subjects,
                                recentTopics = recentTopics,
                                flashcards = flashcards,
                                sessions = sessions,
                                quizResults = quizResults,

                                onOpenSubjects = {
                                    openTab("Subjects")
                                },

                                onOpenSubject = { subjectId ->
                                    navController.navigate(
                                        "SubjectDetail/$subjectId"
                                    )
                                },

                                onOpenFlashcards = {
                                    navController.navigate(
                                        "Flashcards"
                                    )
                                },

                                onOpenPlanner = {
                                    openTab("Planner")
                                },

                                onOpenQuiz = {
                                    openTab("Quiz")
                                },

                                onOpenProgress = {
                                    openTab("Progress")
                                }
                            )
                        }

                        // ---------------------------------------------
                        // SUBJECT MANAGEMENT
                        // ---------------------------------------------

                        composable("Subjects") {
                            SubjectsScreen(
                                subjects = subjects,

                                onOpenSubject = { subjectId ->
                                    navController.navigate(
                                        "SubjectDetail/$subjectId"
                                    )
                                },

                                onAddSubject =
                                    subjectViewModel::addSubject,

                                onUpdateSubject =
                                    subjectViewModel::updateSubject,

                                onDeleteSubject =
                                    subjectViewModel::deleteSubject
                            )
                        }

                        // ---------------------------------------------
                        // SUBJECT DETAILS AND TOPICS
                        // ---------------------------------------------

                        composable(
                            route = "SubjectDetail/{subjectId}"
                        ) { entry ->

                            // Reads the selected subject ID.
                            val subjectId =
                                entry.arguments
                                    ?.getString("subjectId")
                                    ?.toIntOrNull()
                                    ?: -1

                            // Observes topics for this subject.
                            val topicsFlow =
                                remember(subjectId) {
                                    topicViewModel
                                        .getTopicsForSubject(
                                            subjectId
                                        )
                                }

                            val topics by
                            topicsFlow.collectAsState(
                                initial = emptyList()
                            )

                            TopicScreen(
                                subject = subjects.find {
                                    it.id == subjectId
                                },

                                topics = topics,

                                onBack = {
                                    navController.popBackStack()
                                },

                                onAddTopic = {
                                        name,
                                        description ->

                                    if (subjectId > 0) {
                                        topicViewModel.addTopic(
                                            subjectId,
                                            name,
                                            description
                                        )
                                    }
                                },

                                onUpdateTopic =
                                    topicViewModel::updateTopic,

                                onDeleteTopic =
                                    topicViewModel::deleteTopic,

                                onOpenNotes = { topicId ->
                                    navController.navigate(
                                        "Notes/$topicId"
                                    )
                                },

                                onOpenFlashcards = { topicId ->
                                    navController.navigate(
                                        "Flashcards/$topicId"
                                    )
                                }
                            )
                        }

                        // ---------------------------------------------
                        // OFFLINE NOTES
                        // ---------------------------------------------

                        composable(
                            route = "Notes/{topicId}"
                        ) { entry ->

                            // Reads the selected topic ID.
                            val topicId =
                                entry.arguments
                                    ?.getString("topicId")
                                    ?.toIntOrNull()
                                    ?: -1

                            // Observes notes for this topic.
                            val notesFlow =
                                remember(topicId) {
                                    noteViewModel
                                        .getNotesForTopic(
                                            topicId
                                        )
                                }

                            val notes by
                            notesFlow.collectAsState(
                                initial = emptyList()
                            )

                            NotesScreen(
                                topic = recentTopics.find {
                                    it.id == topicId
                                },

                                notes = notes,

                                onBack = {
                                    navController.popBackStack()
                                },

                                onAddNote =
                                    noteViewModel::addNote,

                                onUpdateNote =
                                    noteViewModel::updateNote,

                                onDeleteNote =
                                    noteViewModel::deleteNote
                            )
                        }

                        // ---------------------------------------------
                        // REVISION PLANNER
                        // ---------------------------------------------

                        composable("Planner") {
                            PlannerScreen(
                                sessions = sessions,

                                onAddSession =
                                    revisionViewModel::addSession,

                                onDeleteSession =
                                    revisionViewModel::deleteSession
                            )
                        }

                        // ---------------------------------------------
                        // QUIZ SYSTEM
                        // ---------------------------------------------

                        composable("Quiz") {
                            QuizScreen(
                                quizzes = quizzes,
                                subjects = subjects,

                                onCreateQuiz =
                                    quizViewModel::createQuiz,

                                onDeleteQuiz =
                                    quizViewModel::deleteQuiz,

                                onLoadQuestions =
                                    quizViewModel::getQuestions,

                                // Saves the quiz result and schedules
                                // the learner's next revision.
                                onQuizCompleted = {
                                        quizId,
                                        quizTitle,
                                        score,
                                        totalQuestions ->

                                    // Saves the attempt for Progress.
                                    quizViewModel.saveResult(
                                        quizId,
                                        quizTitle,
                                        score,
                                        totalQuestions
                                    )

                                    // Calculates the result percentage.
                                    val percentage =
                                        if (totalQuestions > 0) {
                                            score * 100 /
                                                    totalQuestions
                                        } else {
                                            0
                                        }

                                    // Lower scores are reviewed sooner.
                                    val daysUntilReview =
                                        when {
                                            percentage < 50 ->
                                                1

                                            percentage < 80 ->
                                                3

                                            else ->
                                                7
                                        }

                                    // Calculates the next review date.
                                    val calendar =
                                        Calendar
                                            .getInstance()
                                            .apply {
                                                add(
                                                    Calendar.DAY_OF_YEAR,
                                                    daysUntilReview
                                                )
                                            }

                                    val reviewDate =
                                        SimpleDateFormat(
                                            "dd MMM yyyy",
                                            Locale.getDefault()
                                        ).format(
                                            calendar.time
                                        )

                                    val reviewTitle =
                                        "Review: $quizTitle"

                                    // Prevents duplicate review sessions.
                                    val scheduleAlreadyExists =
                                        sessions.any { session ->
                                            session.title ==
                                                    reviewTitle &&
                                                    session.dueDate ==
                                                    reviewDate
                                        }

                                    // Creates a spaced-repetition reminder.
                                    if (!scheduleAlreadyExists) {
                                        revisionViewModel.addSession(
                                            title = reviewTitle,
                                            dueDate = reviewDate,

                                            description =
                                                "Automatically scheduled " +
                                                        "after scoring " +
                                                        "$percentage%. " +
                                                        "Review this quiz " +
                                                        "again in " +
                                                        "$daysUntilReview " +
                                                        "day(s)."
                                        )
                                    }
                                }
                            )
                        }

                        // ---------------------------------------------
                        // PROGRESS AND LEARNING INSIGHTS
                        // ---------------------------------------------

                        composable("Progress") {
                            ProgressScreen(
                                results = quizResults,

                                // This callback is shared by both:
                                // 1. Open quizzes under Revise Next
                                // 2. Revise again under weak attempts
                                onReviseNext = {
                                    openTab("Quiz")
                                }
                            )
                        }

                        // ---------------------------------------------
                        // GENERAL FLASHCARD REVIEW
                        // ---------------------------------------------

                        composable("Flashcards") {
                            FlashcardScreen(
                                subjects = subjects,
                                topics = recentTopics,
                                cards = flashcards,
                                initialTopicId = null,

                                onBack = {
                                    navController.popBackStack()
                                },

                                onAddFlashcard =
                                    flashcardViewModel::addFlashcard,

                                onDeleteFlashcard =
                                    flashcardViewModel::deleteFlashcard
                            )
                        }

                        // ---------------------------------------------
                        // TOPIC-SPECIFIC FLASHCARDS
                        // ---------------------------------------------

                        composable(
                            route = "Flashcards/{topicId}"
                        ) { entry ->

                            // Reads the selected topic ID.
                            val topicId =
                                entry.arguments
                                    ?.getString("topicId")
                                    ?.toIntOrNull()

                            FlashcardScreen(
                                subjects = subjects,
                                topics = recentTopics,
                                cards = flashcards,
                                initialTopicId = topicId,

                                onBack = {
                                    navController.popBackStack()
                                },

                                onAddFlashcard =
                                    flashcardViewModel::addFlashcard,

                                onDeleteFlashcard =
                                    flashcardViewModel::deleteFlashcard
                            )
                        }
                    }
                }
            }
        }
    }
}