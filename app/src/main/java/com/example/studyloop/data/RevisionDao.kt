package com.example.studyloop.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RevisionDao {
    @Query("SELECT * FROM revision_sessions")
    fun getAllSessions(): Flow<List<RevisionSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: RevisionSessionEntity)

    @Delete
    suspend fun delete(session: RevisionSessionEntity)
}