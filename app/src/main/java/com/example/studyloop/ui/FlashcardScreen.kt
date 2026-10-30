package com.example.studyloop.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studyloop.data.FlashcardEntity
import com.example.studyloop.data.SubjectEntity
import com.example.studyloop.data.TopicEntity

// FLASHCARD SCREEN:
// Creates and reviews flashcards belonging to specific subjects and topics.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardScreen(
    subjects: List<SubjectEntity>,
    topics: List<TopicEntity>,
    cards: List<FlashcardEntity>,
    initialTopicId: Int? = null,
    onBack: () -> Unit,
    onAddFlashcard: (Int, String, String) -> Unit,
    onDeleteFlashcard: (FlashcardEntity) -> Unit
) {
    // Finds the topic selected from the Topic screen.
    val initialTopic = topics.find {
        it.id == initialTopicId
    }

    // Selects the subject belonging to the initial topic.
    var selectedSubjectId by remember {
        mutableIntStateOf(
            initialTopic?.subjectId
                ?: subjects.firstOrNull()?.id
                ?: -1
        )
    }

    // Selects the initial topic or the first available topic.
    var selectedTopicId by remember {
        mutableIntStateOf(
            initialTopicId
                ?: topics.firstOrNull {
                    it.subjectId == selectedSubjectId
                }?.id
                ?: -1
        )
    }

    var cardIndex by remember {
        mutableIntStateOf(0)
    }

    var showAnswer by remember {
        mutableStateOf(false)
    }

    var showAddDialog by remember {
        mutableStateOf(false)
    }

    var cardToDelete by remember {
        mutableStateOf<FlashcardEntity?>(null)
    }

    var newQuestion by remember {
        mutableStateOf("")
    }

    var newAnswer by remember {
        mutableStateOf("")
    }

    var questionError by remember {
        mutableStateOf<String?>(null)
    }

    var answerError by remember {
        mutableStateOf<String?>(null)
    }

    // Updates the selected topic when this screen is opened
    // from a particular topic.
    LaunchedEffect(initialTopicId, topics) {
        val requestedTopic = topics.find {
            it.id == initialTopicId
        }

        if (requestedTopic != null) {
            selectedSubjectId = requestedTopic.subjectId
            selectedTopicId = requestedTopic.id
        }
    }

    // Keeps the selected subject valid when database data changes.
    LaunchedEffect(subjects) {
        if (
            subjects.none { it.id == selectedSubjectId } &&
            subjects.isNotEmpty()
        ) {
            selectedSubjectId = subjects.first().id
        }
    }

    // Topics belonging to the selected subject.
    val subjectTopics = topics.filter {
        it.subjectId == selectedSubjectId
    }

    // Keeps the selected topic valid after changing subjects.
    LaunchedEffect(selectedSubjectId, subjectTopics) {
        if (subjectTopics.none { it.id == selectedTopicId }) {
            selectedTopicId =
                subjectTopics.firstOrNull()?.id ?: -1
        }
    }

    val selectedSubject = subjects.find {
        it.id == selectedSubjectId
    }

    val selectedTopic = topics.find {
        it.id == selectedTopicId
    }

    // Shows only flashcards saved under the selected topic.
    val filteredCards = cards.filter {
        it.topicId == selectedTopicId
    }

    // Returns to the first card whenever the topic or card list changes.
    LaunchedEffect(selectedTopicId, filteredCards.size) {
        cardIndex = 0
        showAnswer = false
    }

    // Prevents an invalid index after deleting a card.
    val safeCardIndex = when {
        filteredCards.isEmpty() -> 0
        cardIndex >= filteredCards.size ->
            filteredCards.lastIndex
        else -> cardIndex
    }

    val currentCard = filteredCards.getOrNull(safeCardIndex)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Screen title and back navigation.
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = onBack
            ) {
                Text("← Back")
            }

            Spacer(
                modifier = Modifier.weight(1f)
            )

            Text(
                text = "Flashcards",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        // SUBJECT SELECTION
        Text(
            text = "Select Subject",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(
            modifier = Modifier.height(4.dp)
        )

        if (subjects.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text =
                        "Create a subject and topic before adding flashcards.",
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = subjects,
                    key = { subject ->
                        subject.id
                    }
                ) { subject ->
                    FilterChip(
                        selected =
                            subject.id == selectedSubjectId,

                        onClick = {
                            selectedSubjectId = subject.id

                            // Automatically selects the first topic
                            // belonging to the new subject.
                            selectedTopicId = topics
                                .firstOrNull {
                                    it.subjectId == subject.id
                                }
                                ?.id ?: -1
                        },

                        label = {
                            Text(subject.name)
                        }
                    )
                }
            }
        }

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        // TOPIC SELECTION
        Text(
            text = "Select Topic",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(
            modifier = Modifier.height(4.dp)
        )

        if (
            subjects.isNotEmpty() &&
            subjectTopics.isEmpty()
        ) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text =
                        "Add a topic under ${selectedSubject?.name ?: "this subject"} before creating flashcards.",
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = subjectTopics,
                    key = { topic ->
                        topic.id
                    }
                ) { topic ->
                    FilterChip(
                        selected =
                            topic.id == selectedTopicId,

                        onClick = {
                            selectedTopicId = topic.id
                        },

                        label = {
                            Text(topic.name)
                        }
                    )
                }
            }
        }

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        // Selected topic, card total and creation button.
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement =
                Arrangement.spacedBy(8.dp)
        ) {
            Badge(
                containerColor =
                    MaterialTheme.colorScheme.secondaryContainer,
                contentColor =
                    MaterialTheme.colorScheme.onSecondaryContainer
            ) {
                Text(
                    text = selectedTopic?.name ?: "No topic selected",
                    modifier = Modifier.padding(
                        horizontal = 8.dp,
                        vertical = 4.dp
                    )
                )
            }

            Spacer(
                modifier = Modifier.weight(1f)
            )

            Text(
                text = if (filteredCards.isEmpty()) {
                    "0 cards"
                } else {
                    "Card ${safeCardIndex + 1} " +
                            "of ${filteredCards.size}"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Button(
            onClick = {
                newQuestion = ""
                newAnswer = ""
                questionError = null
                answerError = null
                showAddDialog = true
            },
            enabled = selectedTopic != null,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Create Flashcard")
        }

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        if (currentCard == null) {
            // Genuine empty state when Room contains no cards
            // for the selected topic.
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor =
                        MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🎴",
                        fontSize = 48.sp
                    )

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    Text(
                        text = if (selectedTopic == null) {
                            "Select or create a topic first"
                        } else {
                            "No flashcards for this topic"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center
                    )

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    Text(
                        text = if (selectedTopic == null) {
                            "Flashcards must belong to a topic."
                        } else {
                            "Create your first question and answer " +
                                    "for ${selectedTopic.name}."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color =
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // Displays the current question or answer.
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (showAnswer) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },

                    contentColor = if (showAnswer) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (showAnswer) {
                            "ANSWER"
                        } else {
                            "QUESTION"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(
                        modifier = Modifier.height(16.dp)
                    )

                    Text(
                        text = if (showAnswer) {
                            currentCard.answer
                        } else {
                            currentCard.question
                        },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            // Reveals or hides the flashcard answer.
            Button(
                onClick = {
                    showAnswer = !showAnswer
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (showAnswer) {
                        "Hide Answer"
                    } else {
                        "Show Answer"
                    }
                )
            }

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            // Moves between saved cards.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        showAnswer = false

                        cardIndex = if (safeCardIndex > 0) {
                            safeCardIndex - 1
                        } else {
                            filteredCards.lastIndex
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Previous")
                }

                Button(
                    onClick = {
                        showAnswer = false

                        cardIndex =
                            (safeCardIndex + 1) %
                                    filteredCards.size
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Next")
                }
            }

            // Allows the currently displayed card to be removed.
            TextButton(
                onClick = {
                    cardToDelete = currentCard
                },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Delete This Flashcard")
            }
        }
    }

    // Form for creating a flashcard under the selected topic.
    if (
        showAddDialog &&
        selectedTopic != null
    ) {
        AlertDialog(
            onDismissRequest = {
                showAddDialog = false
            },

            title = {
                Text("Create Flashcard")
            },

            text = {
                Column(
                    modifier =
                        Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement =
                        Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text =
                            "${selectedSubject?.name ?: "Subject"} • " +
                                    selectedTopic.name,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = newQuestion,
                        onValueChange = {
                            newQuestion = it
                            questionError = null
                        },
                        label = {
                            Text("Question")
                        },
                        placeholder = {
                            Text("Example: What is cloud computing?")
                        },
                        isError = questionError != null,
                        supportingText = {
                            questionError?.let {
                                Text(it)
                            }
                        },
                        minLines = 2,
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newAnswer,
                        onValueChange = {
                            newAnswer = it
                            answerError = null
                        },
                        label = {
                            Text("Answer")
                        },
                        placeholder = {
                            Text("Enter the correct explanation")
                        },
                        isError = answerError != null,
                        supportingText = {
                            answerError?.let {
                                Text(it)
                            }
                        },
                        minLines = 2,
                        maxLines = 5,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },

            confirmButton = {
                Button(
                    onClick = {
                        val cleanQuestion = newQuestion.trim()
                        val cleanAnswer = newAnswer.trim()

                        questionError = if (
                            cleanQuestion.isBlank()
                        ) {
                            "Question is required."
                        } else {
                            null
                        }

                        answerError = if (
                            cleanAnswer.isBlank()
                        ) {
                            "Answer is required."
                        } else {
                            null
                        }

                        if (
                            questionError == null &&
                            answerError == null
                        ) {
                            // Saves the card under the exact selected topic.
                            onAddFlashcard(
                                selectedTopic.id,
                                cleanQuestion,
                                cleanAnswer
                            )

                            showAddDialog = false
                        }
                    }
                ) {
                    Text("Save Flashcard")
                }
            },

            dismissButton = {
                TextButton(
                    onClick = {
                        showAddDialog = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Confirms deletion to prevent accidental data loss.
    cardToDelete?.let { card ->
        AlertDialog(
            onDismissRequest = {
                cardToDelete = null
            },

            title = {
                Text("Delete Flashcard?")
            },

            text = {
                Text(
                    "This question and answer will be permanently removed."
                )
            },

            confirmButton = {
                Button(
                    onClick = {
                        onDeleteFlashcard(card)
                        cardToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text("Delete")
                }
            },

            dismissButton = {
                TextButton(
                    onClick = {
                        cardToDelete = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}