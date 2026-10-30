package com.example.studyloop.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.studyloop.data.NoteEntity
import com.example.studyloop.data.TopicEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Displays and manages offline notes for one selected topic.
@Composable
fun NotesScreen(
    topic: TopicEntity?,
    notes: List<NoteEntity>,
    onBack: () -> Unit,
    onAddNote: (Int, String, String) -> Unit,
    onUpdateNote: (NoteEntity, String, String) -> Unit,
    onDeleteNote: (NoteEntity) -> Unit
) {
    // Controls the add and edit note form.
    var showForm by remember {
        mutableStateOf(false)
    }

    var editingNote by remember {
        mutableStateOf<NoteEntity?>(null)
    }

    // Stores the information entered in the form.
    var titleText by remember {
        mutableStateOf("")
    }

    var contentText by remember {
        mutableStateOf("")
    }

    // Stores validation messages.
    var titleError by remember {
        mutableStateOf<String?>(null)
    }

    var contentError by remember {
        mutableStateOf<String?>(null)
    }

    // Stores the note selected for deletion.
    var noteToDelete by remember {
        mutableStateOf<NoteEntity?>(null)
    }

    // Clears the form and returns to the note list.
    fun closeForm() {
        showForm = false
        editingNote = null
        titleText = ""
        contentText = ""
        titleError = null
        contentError = null
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
        // Back button and screen heading.
        item {
            TextButton(
                onClick = onBack,
                contentPadding = PaddingValues(0.dp)
            ) {
                Text("← Back to Topics")
            }

            Text(
                text = "Offline Notes",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = topic?.name ?: "Selected Topic",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Write and access your study notes without an internet connection.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Displays the note editor or the create-note button.
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
                                if (editingNote == null) {
                                    "Create Note"
                                } else {
                                    "Edit Note"
                                },
                            style =
                                MaterialTheme.typography.titleLarge
                        )

                        // Note title input.
                        OutlinedTextField(
                            value = titleText,
                            onValueChange = {
                                titleText = it
                                titleError = null
                            },
                            label = {
                                Text("Note title")
                            },
                            placeholder = {
                                Text("e.g. Important AWS Services")
                            },
                            isError = titleError != null,
                            supportingText = {
                                titleError?.let {
                                    Text(it)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        // Main offline note content.
                        OutlinedTextField(
                            value = contentText,
                            onValueChange = {
                                contentText = it
                                contentError = null
                            },
                            label = {
                                Text("Note content")
                            },
                            placeholder = {
                                Text(
                                    "Write your revision notes here..."
                                )
                            },
                            isError = contentError != null,
                            supportingText = {
                                contentError?.let {
                                    Text(it)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 5,
                            maxLines = 10
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement =
                                Arrangement.spacedBy(8.dp)
                        ) {
                            // Cancels note creation or editing.
                            OutlinedButton(
                                onClick = {
                                    closeForm()
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Cancel")
                            }

                            // Validates and saves the note.
                            Button(
                                onClick = {
                                    titleError =
                                        if (titleText.isBlank()) {
                                            "Please enter a title."
                                        } else {
                                            null
                                        }

                                    contentError =
                                        if (contentText.isBlank()) {
                                            "Please enter note content."
                                        } else {
                                            null
                                        }

                                    if (
                                        titleError == null &&
                                        contentError == null
                                    ) {
                                        val selectedNote =
                                            editingNote

                                        if (selectedNote == null) {
                                            topic?.let {
                                                onAddNote(
                                                    it.id,
                                                    titleText,
                                                    contentText
                                                )
                                            }
                                        } else {
                                            onUpdateNote(
                                                selectedNote,
                                                titleText,
                                                contentText
                                            )
                                        }

                                        closeForm()
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    if (editingNote == null) {
                                        "Save Note"
                                    } else {
                                        "Update Note"
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
                        editingNote = null
                        titleText = ""
                        contentText = ""
                        showForm = true
                    },
                    enabled = topic != null,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("+ Create Offline Note")
                }
            }
        }

        item {
            Text(
                text = "Saved Notes (${notes.size})",
                style = MaterialTheme.typography.titleMedium
            )
        }

        // Displays a genuine empty state.
        if (notes.isEmpty()) {
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
                            text = "No notes created yet",
                            style =
                                MaterialTheme.typography.titleMedium
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Create a note to store important information for this topic.",
                            style =
                                MaterialTheme.typography.bodyMedium,
                            color =
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            // Displays every real note stored under this topic.
            items(
                items = notes,
                key = { it.id }
            ) { note ->
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
                            text = note.title,
                            style =
                                MaterialTheme.typography.titleMedium
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = note.content,
                            style =
                                MaterialTheme.typography.bodyMedium,
                            maxLines = 5,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text =
                                "Updated ${formatNoteDate(note.updatedAt)}",
                            style =
                                MaterialTheme.typography.labelSmall,
                            color =
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement =
                                Arrangement.End
                        ) {
                            // Loads the selected note into the editor.
                            TextButton(
                                onClick = {
                                    editingNote = note
                                    titleText = note.title
                                    contentText = note.content
                                    titleError = null
                                    contentError = null
                                    showForm = true
                                }
                            ) {
                                Text("Edit")
                            }

                            // Opens the delete-confirmation dialog.
                            TextButton(
                                onClick = {
                                    noteToDelete = note
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

    // Prevents notes from being deleted accidentally.
    noteToDelete?.let { note ->
        AlertDialog(
            onDismissRequest = {
                noteToDelete = null
            },
            title = {
                Text("Delete Note?")
            },
            text = {
                Text(
                    "\"${note.title}\" will be permanently removed."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteNote(note)
                        noteToDelete = null
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
                        noteToDelete = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

// Formats the note update time for display.
private fun formatNoteDate(time: Long): String {
    return SimpleDateFormat(
        "dd MMM yyyy, h:mm a",
        Locale.getDefault()
    ).format(Date(time))
}