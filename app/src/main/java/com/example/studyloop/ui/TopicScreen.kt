package com.example.studyloop.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.studyloop.data.SubjectEntity
import com.example.studyloop.data.TopicEntity

// Manages topics belonging to the selected subject.
@Composable
fun TopicScreen(
    subject: SubjectEntity?,
    topics: List<TopicEntity>,
    onBack: () -> Unit,
    onAddTopic: (String, String) -> Unit,
    onUpdateTopic: (TopicEntity) -> Unit,
    onDeleteTopic: (TopicEntity) -> Unit,
    onOpenNotes: (Int) -> Unit,
    onOpenFlashcards: (Int) -> Unit
) {
    // Controls topic creation and editing.
    var showForm by remember {
        mutableStateOf(false)
    }

    var editingTopic by remember {
        mutableStateOf<TopicEntity?>(null)
    }

    // Stores form input.
    var name by remember {
        mutableStateOf("")
    }

    var description by remember {
        mutableStateOf("")
    }

    // Displays validation and deletion confirmation.
    var nameError by remember {
        mutableStateOf<String?>(null)
    }

    var topicToDelete by remember {
        mutableStateOf<TopicEntity?>(null)
    }

    // Clears and closes the topic form.
    fun closeForm() {
        showForm = false
        editingTopic = null
        name = ""
        description = ""
        nameError = null
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            top = 12.dp,
            end = 16.dp,
            bottom = 32.dp
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Back navigation and subject information.
        item {
            TextButton(
                onClick = onBack,
                contentPadding = PaddingValues(0.dp)
            ) {
                Text("← Back to Subjects")
            }

            Text(
                text = subject?.name ?: "Subject Topics",
                style = MaterialTheme.typography.headlineMedium
            )

            if (!subject?.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = subject?.description.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                    color =
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Displays the topic form or add-topic button.
        if (showForm) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor =
                            MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement =
                            Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text =
                                if (editingTopic == null) {
                                    "Create Topic"
                                } else {
                                    "Edit Topic"
                                },
                            style =
                                MaterialTheme.typography.titleLarge
                        )

                        // Topic name input.
                        OutlinedTextField(
                            value = name,
                            onValueChange = {
                                name = it
                                nameError = null
                            },
                            label = {
                                Text("Topic name")
                            },
                            placeholder = {
                                Text("e.g. AWS Features")
                            },
                            isError = nameError != null,
                            supportingText = {
                                nameError?.let {
                                    Text(it)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        // Optional topic description.
                        OutlinedTextField(
                            value = description,
                            onValueChange = {
                                description = it
                            },
                            label = {
                                Text("Description")
                            },
                            placeholder = {
                                Text(
                                    "e.g. EC2, S3, Lambda and RDS"
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            maxLines = 4
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement =
                                Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    closeForm()
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Cancel")
                            }

                            // Creates or updates the topic.
                            Button(
                                onClick = {
                                    if (name.isBlank()) {
                                        nameError =
                                            "Please enter a topic name."
                                    } else {
                                        val selectedTopic =
                                            editingTopic

                                        if (selectedTopic == null) {
                                            onAddTopic(
                                                name.trim(),
                                                description.trim()
                                            )
                                        } else {
                                            onUpdateTopic(
                                                selectedTopic.copy(
                                                    name = name.trim(),
                                                    description =
                                                        description.trim()
                                                )
                                            )
                                        }

                                        closeForm()
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    if (editingTopic == null) {
                                        "Save Topic"
                                    } else {
                                        "Update Topic"
                                    }
                                )
                            }
                        }
                    }
                }
            }
        } else {
            item {
                Button(
                    onClick = {
                        editingTopic = null
                        name = ""
                        description = ""
                        showForm = true
                    },
                    enabled = subject != null,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("+ Add Topic")
                }
            }
        }

        item {
            Text(
                text = "Topics (${topics.size})",
                style = MaterialTheme.typography.titleMedium
            )
        }

        // Genuine empty state when no topics exist.
        if (topics.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor =
                            MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment =
                            Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No topics created yet",
                            style =
                                MaterialTheme.typography.titleMedium
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Add a topic to organise your notes and flashcards.",
                            style =
                                MaterialTheme.typography.bodyMedium,
                            color =
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            // Displays topics saved under the selected subject.
            items(
                items = topics,
                key = { it.id }
            ) { topic ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor =
                            MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = topic.name,
                            style =
                                MaterialTheme.typography.titleMedium
                        )

                        if (topic.description.isNotBlank()) {
                            Spacer(modifier = Modifier.height(5.dp))

                            Text(
                                text = topic.description,
                                style =
                                    MaterialTheme.typography.bodyMedium,
                                color =
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Main learning tools connected to this topic.
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement =
                                Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    onOpenNotes(topic.id)
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Notes")
                            }

                            Button(
                                onClick = {
                                    onOpenFlashcards(topic.id)
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Flashcards")
                            }
                        }

                        // Topic-management controls.
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement =
                                Arrangement.End
                        ) {
                            TextButton(
                                onClick = {
                                    editingTopic = topic
                                    name = topic.name
                                    description = topic.description
                                    nameError = null
                                    showForm = true
                                }
                            ) {
                                Text("Edit")
                            }

                            TextButton(
                                onClick = {
                                    topicToDelete = topic
                                }
                            ) {
                                Text(
                                    text = "Delete",
                                    color =
                                        MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Confirms deletion because related notes and flashcards are removed.
    topicToDelete?.let { topic ->
        AlertDialog(
            onDismissRequest = {
                topicToDelete = null
            },
            title = {
                Text("Delete Topic?")
            },
            text = {
                Text(
                    "\"${topic.name}\" and its saved notes and flashcards will be permanently removed."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteTopic(topic)
                        topicToDelete = null
                    }
                ) {
                    Text(
                        text = "Delete",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        topicToDelete = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}