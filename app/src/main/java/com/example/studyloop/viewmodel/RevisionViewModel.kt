package com.example.studyloop.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.studyloop.data.AppDatabase
import com.example.studyloop.data.RevisionSessionEntity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RevisionViewModel(application: Application) : AndroidViewModel(application) {

    private val revisionDao = AppDatabase.getDatabase(application).revisionDao()

    val sessions = revisionDao.getAllSessions().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    private val messageChannel = Channel<String>(Channel.BUFFERED)
    val messages = messageChannel.receiveAsFlow()

    fun addSession(title: String, dueDate: String, description: String = "") {
        val cleanTitle = title.trim()
        if (cleanTitle.isEmpty()) return

        performDatabaseAction(
            successMessage = "Revision schedule saved.",
            errorMessage = "Could not save revision schedule."
        ) {
            revisionDao.insert(
                RevisionSessionEntity(
                    title = cleanTitle,
                    dueDate = dueDate.trim().ifBlank { "Today" },
                    description = description.trim()
                )
            )
        }
    }

    fun deleteSession(session: RevisionSessionEntity) {
        performDatabaseAction(
            successMessage = "Revision schedule deleted.",
            errorMessage = "Could not delete revision schedule."
        ) {
            revisionDao.delete(session)
        }
    }

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