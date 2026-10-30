package com.example.studyloop.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.studyloop.data.AppDatabase
import com.example.studyloop.data.QuizEntity
import com.example.studyloop.data.QuizQuestionDraft
import com.example.studyloop.data.QuizQuestionEntity
import com.example.studyloop.data.QuizResultEntity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Connects the quiz interface to the Room database.
class QuizViewModel(
    application: Application
) : AndroidViewModel(application) {

    // Gets access to the quiz and quiz-result database operations.
    private val database = AppDatabase.getDatabase(application)
    private val quizDao = database.quizDao()
    private val resultDao = database.quizResultDao()

    // Observes all saved user-created quizzes.
    // The interface automatically updates when quizzes are added or deleted.
    val quizzes = quizDao.getAllQuizzes().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    // Observes every saved quiz attempt for the Progress screen.
    val results = resultDao.getAllResults().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    // Keeps the latest result available if another screen requires it.
    val latestResult = resultDao.getLatestResult().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null
    )

    // Sends success and error messages to the shared Snackbar.
    private val messageChannel = Channel<String>(
        capacity = Channel.BUFFERED
    )

    val messages = messageChannel.receiveAsFlow()

    // Validates and saves a complete user-created quiz.
    fun createQuiz(
        title: String,
        subjectId: Int?,
        questions: List<QuizQuestionDraft>
    ) {
        val cleanTitle = title.trim()

        // Checks that every question contains four answers
        // and has a valid correct-answer position.
        val questionsAreValid = questions.isNotEmpty() &&
                questions.all { draft ->
                    draft.question.isNotBlank() &&
                            draft.options.size == 4 &&
                            draft.options.all { it.isNotBlank() } &&
                            draft.correctAnswerIndex in draft.options.indices
                }

        if (cleanTitle.isEmpty() || !questionsAreValid) {
            sendMessage(
                "Enter a title and complete every quiz question."
            )
            return
        }

        // Database work is performed in a coroutine so the UI does not freeze.
        viewModelScope.launch {
            val message = try {

                // Saves the quiz first and receives its generated ID.
                val quizId = quizDao.insertQuiz(
                    QuizEntity(
                        title = cleanTitle,
                        subjectId = subjectId
                    )
                ).toInt()

                // Converts the temporary question drafts into Room entities.
                val questionEntities = questions.map { draft ->
                    QuizQuestionEntity(
                        quizId = quizId,
                        question = draft.question.trim(),
                        optionA = draft.options[0].trim(),
                        optionB = draft.options[1].trim(),
                        optionC = draft.options[2].trim(),
                        optionD = draft.options[3].trim(),
                        correctAnswerIndex =
                            draft.correctAnswerIndex,
                        explanation =
                            draft.explanation.trim()
                    )
                }

                // Saves every question under the generated quiz ID.
                quizDao.insertQuestions(questionEntities)

                "Quiz saved."
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                "Could not save quiz."
            }

            messageChannel.send(message)
        }
    }

    // Loads all questions belonging to the selected quiz.
    suspend fun getQuestions(
        quizId: Int
    ): List<QuizQuestionEntity> {
        return quizDao.getQuestionsForQuiz(quizId)
    }

    // Deletes a quiz from Room.
    // Its questions are removed automatically through cascade deletion.
    fun deleteQuiz(quiz: QuizEntity) {
        viewModelScope.launch {
            val message = try {
                quizDao.deleteQuiz(quiz)
                "Quiz deleted."
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                "Could not delete quiz."
            }

            messageChannel.send(message)
        }
    }

    // Validates and saves one completed quiz attempt.
    fun saveResult(
        quizId: Int,
        quizTitle: String,
        score: Int,
        totalQuestions: Int
    ) {
        // Prevents invalid results from entering the database.
        if (
            totalQuestions <= 0 ||
            score !in 0..totalQuestions
        ) {
            sendMessage("Invalid quiz result.")
            return
        }

        viewModelScope.launch {
            val message = try {

                // Stores the attempt for history and progress calculations.
                resultDao.insertResult(
                    QuizResultEntity(
                        quizId = quizId,
                        quizTitle = quizTitle,
                        score = score,
                        totalQuestions = totalQuestions
                    )
                )

                "Quiz result saved."
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                "Could not save quiz result."
            }

            messageChannel.send(message)
        }
    }

    // Helper function for sending validation messages.
    private fun sendMessage(message: String) {
        viewModelScope.launch {
            messageChannel.send(message)
        }
    }
}