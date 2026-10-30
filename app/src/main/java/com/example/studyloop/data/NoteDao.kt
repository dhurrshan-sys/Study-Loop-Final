package com.example.studyloop.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    // Returns notes belonging to the selected topic.
    // Recently updated notes are displayed first.
    @Query(
        """
        SELECT * FROM notes
        WHERE topicId = :topicId
        ORDER BY updatedAt DESC
        """
    )
    fun getNotesForTopic(
        topicId: Int
    ): Flow<List<NoteEntity>>

    // Saves a new offline note.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity)

    // Saves changes made to an existing note.
    @Update
    suspend fun updateNote(note: NoteEntity)

    // Permanently deletes the selected note.
    @Delete
    suspend fun deleteNote(note: NoteEntity)
}