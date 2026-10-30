package com.example.studyloop.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FlashcardDao {

    // Returns every saved flashcard.
    @Query("SELECT * FROM flashcards")
    fun getAllFlashcards(): Flow<List<FlashcardEntity>>

    // Returns flashcards belonging to one selected topic.
    @Query("SELECT * FROM flashcards WHERE topicId = :topicId")
    fun getFlashcardsForTopic(topicId: Int): Flow<List<FlashcardEntity>>

    // Saves a new flashcard.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(card: FlashcardEntity)

    // Updates an existing flashcard.
    @Update
    suspend fun update(card: FlashcardEntity)

    // Deletes one selected flashcard.
    @Delete
    suspend fun delete(card: FlashcardEntity)

    // Deletes all flashcards belonging to one topic.
    @Query("DELETE FROM flashcards WHERE topicId = :topicId")
    suspend fun deleteByTopicId(topicId: Int)

    // Deletes flashcards from every topic inside a deleted subject.
    // The subquery first identifies all topics belonging to that subject.
    @Query(
        """
        DELETE FROM flashcards
        WHERE topicId IN (
            SELECT id FROM topics WHERE subjectId = :subjectId
        )
        """
    )
    suspend fun deleteBySubjectId(subjectId: Int)
}