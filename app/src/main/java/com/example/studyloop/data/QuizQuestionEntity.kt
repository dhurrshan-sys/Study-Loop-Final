package com.example.studyloop.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// Stores every question belonging to a user-created quiz.
@Entity(
    tableName = "quiz_questions",

    // Connects each question to its parent quiz.
    // CASCADE automatically deletes the questions when the quiz is deleted.
    foreignKeys = [
        ForeignKey(
            entity = QuizEntity::class,
            parentColumns = ["id"],
            childColumns = ["quizId"],
            onDelete = ForeignKey.CASCADE
        )
    ],

    // Improves database performance when loading questions for a quiz.
    indices = [Index("quizId")]
)
data class QuizQuestionEntity(

    // Automatically generated unique ID for each question.
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    // ID of the quiz that owns this question.
    val quizId: Int,

    // Question entered by the user.
    val question: String,

    // Four possible multiple-choice answers.
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,

    // Position of the correct answer from 0 to 3.
    val correctAnswerIndex: Int,

    // Optional explanation displayed after answering.
    val explanation: String = ""
) {
    // Combines the four database columns into a list for the quiz interface.
    val options: List<String>
        get() = listOf(optionA, optionB, optionC, optionD)
}

// Temporarily stores a question while the user is creating a quiz.
// It does not become a Room table because it is only used by the form.
data class QuizQuestionDraft(
    val question: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val explanation: String = ""
)