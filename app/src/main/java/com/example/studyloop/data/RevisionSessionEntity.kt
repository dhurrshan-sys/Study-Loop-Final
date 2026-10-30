package com.example.studyloop.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "revision_sessions")
data class RevisionSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val dueDate: String,
    val description: String = ""
)
