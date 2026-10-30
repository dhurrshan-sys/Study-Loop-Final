package com.example.studyloop.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

// DHURRSHAN'S PART: Database actions for topics.
@Dao
interface TopicDao {

    // Topics inside one selected subject.
    @Query("SELECT * FROM topics WHERE subjectId = :subjectId ORDER BY name ASC")
    fun getTopicsForSubject(subjectId: Int): Flow<List<TopicEntity>>

    // Newest topics first, for the Home dashboard.
    @Query("SELECT * FROM topics ORDER BY id DESC")
    fun getAllTopics(): Flow<List<TopicEntity>>

    @Insert
    suspend fun insertTopic(topic: TopicEntity)

    @Update
    suspend fun updateTopic(topic: TopicEntity)

    @Delete
    suspend fun deleteTopic(topic: TopicEntity)
}