package com.example.studyloop.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.studyloop.data.AppDatabase
import com.example.studyloop.data.NoteEntity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

// Manages offline notes and connects them to the Room database.
class NoteViewModel(application: Application) :
    AndroidViewModel(application) {

    // Provides access to the notes table.
    private val noteDao =
        AppDatabase.getDatabase(application).noteDao()

    // Returns only notes belonging to the selected topic.
    fun getNotesForTopic(
        topicId: Int
    ): Flow<List<NoteEntity>> {
        return noteDao.getNotesForTopic(topicId)
    }

    // Sends success or error feedback to MainActivity.
    private val messageChannel =
        Channel<String>(Channel.BUFFERED)

    val messages = messageChannel.receiveAsFlow()

    // Creates a new offline note under the selected topic.
    fun addNote(
        topicId: Int,
        title: String,
        content: String
    ) {
        val cleanTitle = title.trim()
        val cleanContent = content.trim()

        if (
            topicId <= 0 ||
            cleanTitle.isEmpty() ||
            cleanContent.isEmpty()
        ) {
            return
        }

        performDatabaseAction(
            successMessage = "Note saved.",
            errorMessage = "Could not save the note."
        ) {
            noteDao.insertNote(
                NoteEntity(
                    topicId = topicId,
                    title = cleanTitle,
                    content = cleanContent
                )
            )
        }
    }

    // Updates an existing note and records the latest edit time.
    fun updateNote(
        note: NoteEntity,
        title: String,
        content: String
    ) {
        val cleanTitle = title.trim()
        val cleanContent = content.trim()

        if (
            cleanTitle.isEmpty() ||
            cleanContent.isEmpty()
        ) {
            return
        }

        performDatabaseAction(
            successMessage = "Note updated.",
            errorMessage = "Could not update the note."
        ) {
            noteDao.updateNote(
                note.copy(
                    title = cleanTitle,
                    content = cleanContent,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    // Permanently deletes the selected note.
    fun deleteNote(note: NoteEntity) {
        performDatabaseAction(
            successMessage = "Note deleted.",
            errorMessage = "Could not delete the note."
        ) {
            noteDao.deleteNote(note)
        }
    }

    // Runs database operations without blocking the user interface.
    private fun performDatabaseAction(
        successMessage: String,
        errorMessage: String,
        action: suspend () -> Unit
    ) {
        viewModelScope.launch {
            val message = try {
                action()
                successMessage
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                errorMessage
            }

            messageChannel.send(message)
        }
    }
}