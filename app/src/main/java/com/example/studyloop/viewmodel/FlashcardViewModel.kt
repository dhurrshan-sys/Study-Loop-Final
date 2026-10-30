package com.example.studyloop.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.studyloop.data.AppDatabase
import com.example.studyloop.data.FlashcardEntity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FlashcardViewModel(application: Application) : AndroidViewModel(application) {

    private val flashcardDao = AppDatabase.getDatabase(application).flashcardDao()

    val flashcards = flashcardDao.getAllFlashcards().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    private val messageChannel = Channel<String>(Channel.BUFFERED)
    val messages = messageChannel.receiveAsFlow()

    fun addFlashcard(topicId: Int, question: String, answer: String) {
        val q = question.trim()
        val a = answer.trim()
        if (q.isEmpty() || a.isEmpty()) return

        performDatabaseAction(
            successMessage = "Flashcard saved.",
            errorMessage = "Could not save flashcard."
        ) {
            flashcardDao.insert(
                FlashcardEntity(
                    topicId = topicId,
                    question = q,
                    answer = a
                )
            )
        }
    }

    fun deleteFlashcard(card: FlashcardEntity) {
        performDatabaseAction(
            successMessage = "Flashcard deleted.",
            errorMessage = "Could not delete flashcard."
        ) {
            flashcardDao.delete(card)
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