package com.example.studyloop.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.studyloop.data.AppDatabase
import com.example.studyloop.data.TopicEntity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Connects saved topics to the application's screens.
class TopicViewModel(application: Application) : AndroidViewModel(application) {

    // Provides access to saved topics.
    private val topicDao =
        AppDatabase.getDatabase(application).topicDao()

    // Provides access to flashcards connected to each topic.
    private val flashcardDao =
        AppDatabase.getDatabase(application).flashcardDao()

    // Supplies recently created topics to the Home dashboard.
    val recentTopics = topicDao.getAllTopics().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    // Sends database success or error messages to MainActivity.
    private val messageChannel = Channel<String>(Channel.BUFFERED)
    val messages = messageChannel.receiveAsFlow()

    // Returns topics belonging to the selected subject.
    fun getTopicsForSubject(subjectId: Int): Flow<List<TopicEntity>> {
        return topicDao.getTopicsForSubject(subjectId)
    }

    // Creates a topic after validating its name and subject.
    fun addTopic(
        subjectId: Int,
        name: String,
        description: String
    ) {
        val cleanName = name.trim()

        if (cleanName.isEmpty() || subjectId <= 0) return

        performDatabaseAction(
            successMessage = "Topic saved.",
            errorMessage = "Could not save the topic. Please try again."
        ) {
            topicDao.insertTopic(
                TopicEntity(
                    subjectId = subjectId,
                    name = cleanName,
                    description = description.trim()
                )
            )
        }
    }

    // Updates an existing topic while retaining its database ID.
    fun updateTopic(topic: TopicEntity) {
        if (topic.name.isBlank()) return

        performDatabaseAction(
            successMessage = "Topic updated.",
            errorMessage = "Could not update the topic. Please try again."
        ) {
            topicDao.updateTopic(
                topic.copy(
                    name = topic.name.trim(),
                    description = topic.description.trim()
                )
            )
        }
    }

    // Deletes the topic and every flashcard saved under that topic.
    // This prevents hidden flashcards from remaining in the database.
    fun deleteTopic(topic: TopicEntity) {
        performDatabaseAction(
            successMessage = "Topic and its flashcards deleted.",
            errorMessage = "Could not delete the topic. Please try again."
        ) {
            flashcardDao.deleteByTopicId(topic.id)
            topicDao.deleteTopic(topic)
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