package com.example.studyloop.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// Stores the basic information for every user-created quiz.
@Entity(
    tableName = "quizzes",

    // Connects each quiz to an existing subject.
    // SET_NULL keeps the quiz if its subject is deleted.
    foreignKeys = [
        ForeignKey(
            entity = SubjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],

    // Improves database searching when quizzes are filtered by subject.
    indices = [Index("subjectId")]
)
data class QuizEntity(

    // Automatically generated unique ID for each quiz.
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    // User-entered quiz title.
    val title: String,

    // Optional ID of the subject connected to this quiz.
    // Null means the quiz belongs to the General category.
    val subjectId: Int? = null,

    // Records when the quiz was created.
    val createdAt: Long = System.currentTimeMillis()
)