package com.example.studyloop.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// Each offline note belongs to one topic.
@Entity(
    tableName = "notes",
    foreignKeys = [
        ForeignKey(
            entity = TopicEntity::class,
            parentColumns = ["id"],
            childColumns = ["topicId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("topicId")
    ]
)
data class NoteEntity(

    // Room automatically generates a unique ID for each note.
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    // Connects the note to its selected topic.
    val topicId: Int,

    // Short heading displayed in the note list.
    val title: String,

    // Full note content written by the learner.
    val content: String,

    // Records when the note was created or last updated.
    val updatedAt: Long = System.currentTimeMillis()
)