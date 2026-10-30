package com.example.studyloop.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

// Stores the result of every completed quiz attempt.
@Entity(tableName = "quiz_results")
data class QuizResultEntity(

    // Automatically generated unique ID for each attempt.
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    // ID of the attempted quiz.
    // It is nullable so older results can still remain in the database.
    val quizId: Int? = null,

    // Stores the quiz title with the result.
    // This allows Progress to display the title even if the quiz is deleted.
    @ColumnInfo(defaultValue = "'Practice Quiz'")
    val quizTitle: String = "Practice Quiz",

    // Number of correctly answered questions.
    val score: Int,

    // Total number of questions in the attempted quiz.
    val totalQuestions: Int,

    // Records the date and time when the quiz was completed.
    val completedAt: Long = System.currentTimeMillis()
)