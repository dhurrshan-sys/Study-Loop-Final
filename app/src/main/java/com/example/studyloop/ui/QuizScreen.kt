package com.example.studyloop.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.studyloop.data.QuizEntity
import com.example.studyloop.data.QuizQuestionDraft
import com.example.studyloop.data.QuizQuestionEntity
import com.example.studyloop.data.SubjectEntity
import kotlinx.coroutines.launch

// Represents the four different states displayed inside the Quiz tab.
private enum class QuizMode {
    LIST,
    CREATE,
    PLAY,
    RESULT
}

// Main quiz screen that changes its content according to the selected mode.
@Composable
fun QuizScreen(
    quizzes: List<QuizEntity>,
    subjects: List<SubjectEntity>,
    onCreateQuiz: (
        String,
        Int?,
        List<QuizQuestionDraft>
    ) -> Unit,
    onDeleteQuiz: (QuizEntity) -> Unit,
    onLoadQuestions: suspend (
        Int
    ) -> List<QuizQuestionEntity>,
    onQuizCompleted: (
        Int,
        String,
        Int,
        Int
    ) -> Unit
) {
    // Controls which quiz section is currently displayed.
    var mode by remember {
        mutableStateOf(QuizMode.LIST)
    }

    // Stores the quiz currently selected by the user.
    var activeQuiz by remember {
        mutableStateOf<QuizEntity?>(null)
    }

    // Stores the questions loaded for the selected quiz.
    var activeQuestions by remember {
        mutableStateOf(
            emptyList<QuizQuestionEntity>()
        )
    }

    // Stores the score shown after completing a quiz.
    var lastScore by remember {
        mutableIntStateOf(0)
    }

    // Displays simple status or validation messages.
    var statusMessage by remember {
        mutableStateOf("")
    }

    // Allows Room questions to be loaded without freezing the UI.
    val coroutineScope = rememberCoroutineScope()

    when (mode) {

        // Displays all quizzes saved by the user.
        QuizMode.LIST -> {
            QuizList(
                quizzes = quizzes,
                subjects = subjects,
                statusMessage = statusMessage,

                onCreate = {
                    statusMessage = ""
                    mode = QuizMode.CREATE
                },

                onStart = { quiz ->
                    coroutineScope.launch {
                        val questions =
                            onLoadQuestions(quiz.id)

                        if (questions.isEmpty()) {
                            statusMessage =
                                "This quiz has no questions."
                        } else {
                            activeQuiz = quiz
                            activeQuestions = questions
                            statusMessage = ""
                            mode = QuizMode.PLAY
                        }
                    }
                },

                onDelete = onDeleteQuiz
            )
        }

        // Displays the quiz-creation form.
        QuizMode.CREATE -> {
            CreateQuizForm(
                subjects = subjects,

                onCancel = {
                    mode = QuizMode.LIST
                },

                onSave = {
                        title,
                        subjectId,
                        questions ->

                    onCreateQuiz(
                        title,
                        subjectId,
                        questions
                    )

                    statusMessage =
                        "Quiz added to your library."

                    mode = QuizMode.LIST
                }
            )
        }

        // Displays one question at a time.
        QuizMode.PLAY -> {
            val quiz = activeQuiz

            if (quiz == null) {
                mode = QuizMode.LIST
            } else {
                PlayQuiz(
                    quiz = quiz,
                    questions = activeQuestions,

                    onExit = {
                        mode = QuizMode.LIST
                    },

                    onFinished = { score ->
                        lastScore = score

                        // Saves the completed attempt.
                        onQuizCompleted(
                            quiz.id,
                            quiz.title,
                            score,
                            activeQuestions.size
                        )

                        mode = QuizMode.RESULT
                    }
                )
            }
        }

        // Displays the score after completing the quiz.
        QuizMode.RESULT -> {
            QuizResultView(
                quizTitle =
                    activeQuiz?.title.orEmpty(),
                score = lastScore,
                totalQuestions =
                    activeQuestions.size,

                onRetry = {
                    mode = QuizMode.PLAY
                },

                onBack = {
                    mode = QuizMode.LIST
                }
            )
        }
    }
}

// Displays the user's saved quiz library.
@Composable
private fun QuizList(
    quizzes: List<QuizEntity>,
    subjects: List<SubjectEntity>,
    statusMessage: String,
    onCreate: () -> Unit,
    onStart: (QuizEntity) -> Unit,
    onDelete: (QuizEntity) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(
                rememberScrollState()
            )
            .padding(20.dp),

        verticalArrangement =
            Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "My Quizzes",
            style =
                MaterialTheme.typography.headlineMedium
        )

        Text(
            text =
                "Create your own questions and practise them offline."
        )

        Button(
            onClick = onCreate,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Create New Quiz")
        }

        if (statusMessage.isNotBlank()) {
            Text(
                text = statusMessage,
                color =
                    MaterialTheme.colorScheme.primary
            )
        }

        // Empty state displayed before any quizzes are created.
        if (quizzes.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text =
                        "No saved quizzes yet. Create your first quiz to begin.",
                    modifier = Modifier.padding(18.dp)
                )
            }
        } else {
            // Displays one card for every saved quiz.
            quizzes.forEach { quiz ->

                val subjectName =
                    subjects.find {
                        it.id == quiz.subjectId
                    }?.name ?: "General"

                Card(
                    modifier =
                        Modifier.fillMaxWidth(),

                    colors =
                        CardDefaults.cardColors(
                            containerColor =
                                MaterialTheme
                                    .colorScheme
                                    .surfaceVariant
                        )
                ) {
                    Column(
                        modifier =
                            Modifier.padding(16.dp),

                        verticalArrangement =
                            Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = quiz.title,
                            style =
                                MaterialTheme
                                    .typography
                                    .titleLarge
                        )

                        Text(
                            text =
                                "Subject: $subjectName"
                        )

                        Row(
                            horizontalArrangement =
                                Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    onStart(quiz)
                                }
                            ) {
                                Text("Start Quiz")
                            }

                            TextButton(
                                onClick = {
                                    onDelete(quiz)
                                }
                            ) {
                                Text("Delete")
                            }
                        }
                    }
                }
            }
        }
    }
}

// Form used to create a quiz and add multiple questions.
@Composable
private fun CreateQuizForm(
    subjects: List<SubjectEntity>,
    onCancel: () -> Unit,
    onSave: (
        String,
        Int?,
        List<QuizQuestionDraft>
    ) -> Unit
) {
    // Quiz information.
    var title by remember {
        mutableStateOf("")
    }

    var selectedSubjectId by remember {
        mutableStateOf<Int?>(null)
    }

    // Current question information.
    var question by remember {
        mutableStateOf("")
    }

    var optionA by remember {
        mutableStateOf("")
    }

    var optionB by remember {
        mutableStateOf("")
    }

    var optionC by remember {
        mutableStateOf("")
    }

    var optionD by remember {
        mutableStateOf("")
    }

    var explanation by remember {
        mutableStateOf("")
    }

    var correctIndex by remember {
        mutableIntStateOf(0)
    }

    var errorMessage by remember {
        mutableStateOf("")
    }

    // Temporarily stores all questions before the quiz is saved.
    val draftQuestions = remember {
        mutableStateListOf<QuizQuestionDraft>()
    }

    // Clears the question fields after adding a question.
    fun clearQuestionForm() {
        question = ""
        optionA = ""
        optionB = ""
        optionC = ""
        optionD = ""
        explanation = ""
        correctIndex = 0
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(
                rememberScrollState()
            )
            .padding(20.dp),

        verticalArrangement =
            Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Create Quiz",
            style =
                MaterialTheme.typography.headlineMedium
        )

        OutlinedTextField(
            value = title,

            onValueChange = {
                title = it
                errorMessage = ""
            },

            label = {
                Text("Quiz title")
            },

            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Subject selection is optional.
        Text(
            text = "Choose a subject",
            style =
                MaterialTheme.typography.titleMedium
        )

        FilterChip(
            selected =
                selectedSubjectId == null,

            onClick = {
                selectedSubjectId = null
            },

            label = {
                Text("General")
            }
        )

        subjects.forEach { subject ->
            FilterChip(
                selected =
                    selectedSubjectId == subject.id,

                onClick = {
                    selectedSubjectId = subject.id
                },

                label = {
                    Text(subject.name)
                }
            )
        }

        Text(
            text = "Add Question",
            style =
                MaterialTheme.typography.titleLarge
        )

        OutlinedTextField(
            value = question,

            onValueChange = {
                question = it
                errorMessage = ""
            },

            label = {
                Text("Question")
            },

            modifier = Modifier.fillMaxWidth()
        )

        // Creates the four multiple-choice answer fields.
        val optionValues = listOf(
            optionA,
            optionB,
            optionC,
            optionD
        )

        val optionSetters:
                List<(String) -> Unit> = listOf(
            { optionA = it },
            { optionB = it },
            { optionC = it },
            { optionD = it }
        )

        optionValues.forEachIndexed {
                index,
                value ->

            Row(
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                // Radio button marks the correct answer.
                RadioButton(
                    selected =
                        correctIndex == index,

                    onClick = {
                        correctIndex = index
                    }
                )

                OutlinedTextField(
                    value = value,

                    onValueChange = {
                        optionSetters[index](it)
                        errorMessage = ""
                    },

                    label = {
                        Text(
                            "Option ${'A' + index}"
                        )
                    },

                    modifier =
                        Modifier.weight(1f),

                    singleLine = true
                )
            }
        }

        Text(
            text =
                "Select the radio button beside the correct answer."
        )

        OutlinedTextField(
            value = explanation,

            onValueChange = {
                explanation = it
            },

            label = {
                Text("Explanation (optional)")
            },

            modifier = Modifier.fillMaxWidth()
        )

        // Adds the completed question to the temporary draft list.
        OutlinedButton(
            onClick = {
                val options = listOf(
                    optionA,
                    optionB,
                    optionC,
                    optionD
                )

                if (
                    question.isBlank() ||
                    options.any { it.isBlank() }
                ) {
                    errorMessage =
                        "Enter the question and all four answers."
                } else {
                    draftQuestions.add(
                        QuizQuestionDraft(
                            question =
                                question.trim(),

                            options =
                                options.map {
                                    it.trim()
                                },

                            correctAnswerIndex =
                                correctIndex,

                            explanation =
                                explanation.trim()
                        )
                    )

                    clearQuestionForm()
                    errorMessage = ""
                }
            },

            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text =
                    "Add Question (${draftQuestions.size} added)"
            )
        }

        // Shows questions that have been prepared but not yet saved.
        draftQuestions.forEachIndexed {
                index,
                draft ->

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {
                    Text(
                        text =
                            "${index + 1}. ${draft.question}",

                        modifier =
                            Modifier.weight(1f)
                    )

                    TextButton(
                        onClick = {
                            draftQuestions.removeAt(
                                index
                            )
                        }
                    ) {
                        Text("Remove")
                    }
                }
            }
        }

        if (errorMessage.isNotBlank()) {
            Text(
                text = errorMessage,
                color =
                    MaterialTheme.colorScheme.error
            )
        }

        // Saves the quiz only after validation succeeds.
        Button(
            onClick = {
                when {
                    title.isBlank() -> {
                        errorMessage =
                            "Enter a quiz title."
                    }

                    draftQuestions.isEmpty() -> {
                        errorMessage =
                            "Add at least one question."
                    }

                    else -> {
                        onSave(
                            title.trim(),
                            selectedSubjectId,
                            draftQuestions.toList()
                        )
                    }
                }
            },

            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Quiz")
        }

        TextButton(
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cancel")
        }
    }
}

// Displays questions one at a time and calculates the score.
@Composable
private fun PlayQuiz(
    quiz: QuizEntity,
    questions: List<QuizQuestionEntity>,
    onExit: () -> Unit,
    onFinished: (Int) -> Unit
) {
    // Quiz attempt state.
    var questionIndex by remember(quiz.id) {
        mutableIntStateOf(0)
    }

    var selectedAnswer by remember(quiz.id) {
        mutableIntStateOf(-1)
    }

    var answerSubmitted by remember(quiz.id) {
        mutableStateOf(false)
    }

    var score by remember(quiz.id) {
        mutableIntStateOf(0)
    }

    val currentQuestion =
        questions[questionIndex]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(
                rememberScrollState()
            )
            .padding(20.dp),

        verticalArrangement =
            Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = quiz.title,
            style =
                MaterialTheme.typography.headlineMedium
        )

        Text(
            text =
                "Question ${questionIndex + 1} of ${questions.size}"
        )

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = currentQuestion.question,
                modifier = Modifier.padding(16.dp),
                style =
                    MaterialTheme.typography.titleLarge
            )
        }

        // Displays all four answer options.
        currentQuestion.options.forEachIndexed {
                optionIndex,
                option ->

            Card(
                modifier =
                    Modifier.fillMaxWidth(),

                colors =
                    CardDefaults.cardColors(
                        containerColor = when {

                            // Correct answer becomes green.
                            answerSubmitted &&
                                    optionIndex ==
                                    currentQuestion
                                        .correctAnswerIndex -> {
                                MaterialTheme
                                    .colorScheme
                                    .primaryContainer
                            }

                            // Incorrect selected answer becomes red.
                            answerSubmitted &&
                                    optionIndex ==
                                    selectedAnswer -> {
                                MaterialTheme
                                    .colorScheme
                                    .errorContainer
                            }

                            else -> {
                                MaterialTheme
                                    .colorScheme
                                    .surfaceVariant
                            }
                        }
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected =
                            selectedAnswer ==
                                    optionIndex,

                        onClick = {
                            if (!answerSubmitted) {
                                selectedAnswer =
                                    optionIndex
                            }
                        },

                        enabled =
                            !answerSubmitted
                    )

                    Text(
                        text = option,
                        modifier =
                            Modifier.weight(1f)
                    )
                }
            }
        }

        // Gives immediate feedback after checking an answer.
        if (answerSubmitted) {
            val isCorrect =
                selectedAnswer ==
                        currentQuestion
                            .correctAnswerIndex

            Text(
                text = if (isCorrect) {
                    "Correct!"
                } else {
                    "Incorrect. Correct answer: " +
                            currentQuestion.options[
                                currentQuestion
                                    .correctAnswerIndex
                            ]
                },

                color = if (isCorrect) {
                    MaterialTheme
                        .colorScheme
                        .primary
                } else {
                    MaterialTheme
                        .colorScheme
                        .error
                }
            )

            if (
                currentQuestion
                    .explanation
                    .isNotBlank()
            ) {
                Text(
                    text =
                        "Explanation: " +
                                currentQuestion
                                    .explanation
                )
            }
        }

        // Checks the answer, moves forward or finishes the quiz.
        Button(
            onClick = {
                if (!answerSubmitted) {

                    if (
                        selectedAnswer ==
                        currentQuestion
                            .correctAnswerIndex
                    ) {
                        score++
                    }

                    answerSubmitted = true
                } else if (
                    questionIndex <
                    questions.lastIndex
                ) {
                    questionIndex++
                    selectedAnswer = -1
                    answerSubmitted = false
                } else {
                    onFinished(score)
                }
            },

            enabled =
                answerSubmitted ||
                        selectedAnswer in
                        currentQuestion
                            .options
                            .indices,

            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = when {
                    !answerSubmitted ->
                        "Check Answer"

                    questionIndex <
                            questions.lastIndex ->
                        "Next Question"

                    else ->
                        "Finish Quiz"
                }
            )
        }

        TextButton(
            onClick = onExit,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Exit Quiz")
        }
    }
}

// Displays the completed score and navigation options.
@Composable
private fun QuizResultView(
    quizTitle: String,
    score: Int,
    totalQuestions: Int,
    onRetry: () -> Unit,
    onBack: () -> Unit
) {
    val percentage =
        if (totalQuestions > 0) {
            score * 100 / totalQuestions
        } else {
            0
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),

        horizontalAlignment =
            Alignment.CenterHorizontally,

        verticalArrangement =
            Arrangement.Center
    ) {
        Text(
            text = "Quiz Completed",
            style =
                MaterialTheme.typography.headlineMedium
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Text(
            text = quizTitle,
            style =
                MaterialTheme.typography.titleLarge
        )

        Text(
            text =
                "Score: $score / $totalQuestions",
            style =
                MaterialTheme.typography.headlineSmall
        )

        Text(
            text = "Percentage: $percentage%"
        )

        Text(
            text =
                "Your result has been saved to Progress."
        )

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        Button(
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Try Again")
        }

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back to My Quizzes")
        }
    }
}