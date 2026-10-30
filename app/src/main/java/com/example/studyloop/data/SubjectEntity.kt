package com.example.studyloop.data

import androidx.room.Entity
import androidx.room.PrimaryKey

// DHURRSHAN'S PART: One subject saved in the local Room database.
// Samit Member 2 and Jia Yi Member 3 can later connect your study content to its id.
@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val name: String,
    val description: String = ""
)