package com.example.studyloop.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

// DHURRSHAN'S PART: Database actions for subjects.
@Dao
interface SubjectDao {

    // Observe all saved subjects, sorted by name.
    // Flow sends an updated list when the subjects change.
    @Query("SELECT * FROM subjects ORDER BY name ASC")
    fun getAllSubjects(): Flow<List<SubjectEntity>>

    // Add a new subject and return its generated ID.
    @Insert
    suspend fun insertSubject(subject: SubjectEntity): Long

    // Save changes to an existing subject.
    @Update
    suspend fun updateSubject(subject: SubjectEntity)

    // Remove an existing subject.
    @Delete
    suspend fun deleteSubject(subject: SubjectEntity)
}