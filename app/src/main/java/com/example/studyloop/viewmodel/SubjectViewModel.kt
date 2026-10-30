package com.example.studyloop.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.studyloop.data.AppDatabase
import com.example.studyloop.data.SubjectEntity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Manages subject information and database operations.
class SubjectViewModel(application: Application) :
    AndroidViewModel(application) {

    // Provides access to saved subjects.
    private val subjectDao =
        AppDatabase.getDatabase(application).subjectDao()

    // Provides access to flashcards connected to subject topics.
    private val flashcardDao =
        AppDatabase.getDatabase(application).flashcardDao()

    // Keeps the screens updated whenever subject data changes.
    val subjects = subjectDao.getAllSubjects().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    // Sends database success and error messages to MainActivity.
    private val messageChannel = Channel<String>(Channel.BUFFERED)
    val messages = messageChannel.receiveAsFlow()

    // Creates a subject after validating and cleaning its name.
    fun addSubject(name: String, description: String) {
        val cleanName = name.trim()

        if (cleanName.isEmpty()) return

        performDatabaseAction(
            successMessage = "Subject saved.",
            errorMessage = "Could not save the subject. Please try again."
        ) {
            subjectDao.insertSubject(
                SubjectEntity(
                    name = cleanName,
                    description = description.trim()
                )
            )
        }
    }

    // Updates an existing subject while keeping its database ID.
    fun updateSubject(subject: SubjectEntity) {
        if (subject.name.isBlank()) return

        performDatabaseAction(
            successMessage = "Subject updated.",
            errorMessage = "Could not update the subject. Please try again."
        ) {
            subjectDao.updateSubject(
                subject.copy(
                    name = subject.name.trim(),
                    description = subject.description.trim()
                )
            )
        }
    }

    // Deletes flashcards first and then deletes the selected subject.
    // Topic records are automatically removed through Room's cascade rule.
    fun deleteSubject(subject: SubjectEntity) {
        performDatabaseAction(
            successMessage = "Subject and its study content deleted.",
            errorMessage = "Could not delete the subject. Please try again."
        ) {
            flashcardDao.deleteBySubjectId(subject.id)
            subjectDao.deleteSubject(subject)
        }
    }

    // Performs database work without blocking the user interface.
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