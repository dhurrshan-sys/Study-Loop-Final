package com.example.studyloop.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

// Provides the database operations required by the quiz feature.
@Dao
interface QuizDao {

    // Continuously observes all saved quizzes.
    // The newest quizzes are displayed first.
    @Query("SELECT * FROM quizzes ORDER BY createdAt DESC")
    fun getAllQuizzes(): Flow<List<QuizEntity>>

    // Loads all questions belonging to one selected quiz.
    @Query(
        "SELECT * FROM quiz_questions " +
                "WHERE quizId = :quizId ORDER BY id"
    )
    suspend fun getQuestionsForQuiz(
        quizId: Int
    ): List<QuizQuestionEntity>

    // Inserts a new quiz and returns its generated database ID.
    // The returned ID is used when saving the quiz's questions.
    @Insert
    suspend fun insertQuiz(
        quiz: QuizEntity
    ): Long

    // Inserts all questions created for a quiz.
    @Insert
    suspend fun insertQuestions(
        questions: List<QuizQuestionEntity>
    )

    // Deletes a saved quiz.
    // Its connected questions are removed automatically through CASCADE.
    @Delete
    suspend fun deleteQuiz(
        quiz: QuizEntity
    )
}