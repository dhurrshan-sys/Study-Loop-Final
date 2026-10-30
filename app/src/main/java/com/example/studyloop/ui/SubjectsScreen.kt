package com.example.studyloop.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.studyloop.data.SubjectEntity
import java.util.Locale

// DHURRSHAN'S PART: Display and manage subjects stored in Room.
@Composable
fun SubjectsScreen(
    subjects: List<SubjectEntity>,
    onOpenSubject: (Int) -> Unit,
    onAddSubject: (String, String) -> Unit,
    onUpdateSubject: (SubjectEntity) -> Unit,
    onDeleteSubject: (SubjectEntity) -> Unit
) {
    var showForm by remember { mutableStateOf(false) }
    var editingSubject by remember {
        mutableStateOf<SubjectEntity?>(null)
    }
    var subjectToDelete by remember {
        mutableStateOf<SubjectEntity?>(null)
    }

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf<String?>(null) }

    // DHURRSHAN'S PART: Remember search and sorting during screen recreation.
    var searchText by rememberSaveable { mutableStateOf("") }
    var sortAscending by rememberSaveable { mutableStateOf(true) }
    var showSortMenu by remember { mutableStateOf(false) }

    // Filter the displayed list without changing the saved Room data.
    val matchingSubjects = subjects.filter { subject ->
        subject.name.contains(
            searchText.trim(),
            ignoreCase = true
        )
    }

    // Sort names without treating capital letters differently.
    val displayedSubjects = if (sortAscending) {
        matchingSubjects.sortedBy { it.name.lowercase(Locale.ROOT) }
    } else {
        matchingSubjects.sortedByDescending {
            it.name.lowercase(Locale.ROOT)
        }
    }

    // DHURRSHAN'S PART: The whole screen scrolls on smaller devices.
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Your Subjects",
                    style = MaterialTheme.typography.headlineMedium
                )

                Text(
                    text = "Organise your revision by subject and topic.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    editingSubject = null
                    name = ""
                    description = ""
                    nameError = null
                    showForm = true
                }
            ) {
                Text("Add Subject")
            }
        }

        // DHURRSHAN'S PART: Search by subject name as the user types.
        item {
            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                label = { Text("Search subjects") },
                placeholder = { Text("Enter a subject name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    if (searchText.isNotEmpty()) {
                        TextButton(
                            onClick = { searchText = "" }
                        ) {
                            Text("Clear")
                        }
                    }
                }
            )
        }

        // DHURRSHAN'S PART: Dropdown provides a second input type.
        item {
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = { showSortMenu = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (sortAscending) {
                            "Sort: Name A–Z ▾"
                        } else {
                            "Sort: Name Z–A ▾"
                        }
                    )
                }

                DropdownMenu(
                    expanded = showSortMenu,
                    onDismissRequest = { showSortMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Name A–Z") },
                        onClick = {
                            sortAscending = true
                            showSortMenu = false
                        }
                    )

                    DropdownMenuItem(
                        text = { Text("Name Z–A") },
                        onClick = {
                            sortAscending = false
                            showSortMenu = false
                        }
                    )
                }
            }
        }

        if (subjects.isEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Start your study collection",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text(
                            text = "Tap Add Subject above, then add topics " +
                                    "inside your new subject.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        } else {
            item {
                Text(
                    text = if (searchText.isBlank()) {
                        if (subjects.size == 1) {
                            "1 saved subject"
                        } else {
                            "${subjects.size} saved subjects"
                        }
                    } else {
                        "Showing ${displayedSubjects.size} of " +
                                "${subjects.size} subjects"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // A search with no results is different from an empty database.
            if (displayedSubjects.isEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "No matching subjects",
                                style = MaterialTheme.typography.titleMedium
                            )

                            Text(
                                text = "Try another name or clear your search.",
                                style = MaterialTheme.typography.bodyMedium
                            )

                            TextButton(onClick = { searchText = "" }) {
                                Text("Clear Search")
                            }
                        }
                    }
                }
            }

            // Each item contains text, a badge and subject navigation.
            items(
                items = displayedSubjects,
                key = { subject -> subject.id }
            ) { subject ->
                Card(
                    onClick = { onOpenSubject(subject.id) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = subject.name,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            Badge(
                                containerColor =
                                    MaterialTheme.colorScheme.secondaryContainer,
                                contentColor =
                                    MaterialTheme.colorScheme.onSecondaryContainer
                            ) {
                                Text(
                                    text = "Subject",
                                    modifier = Modifier.padding(
                                        horizontal = 6.dp,
                                        vertical = 2.dp
                                    )
                                )
                            }
                        }

                        Text(
                            text = subject.description.ifBlank {
                                "Tap to explore your topics."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )

                        Row {
                            TextButton(
                                onClick = { onOpenSubject(subject.id) }
                            ) {
                                Text("Topics")
                            }

                            TextButton(
                                onClick = {
                                    editingSubject = subject
                                    name = subject.name
                                    description = subject.description
                                    nameError = null
                                    showForm = true
                                }
                            ) {
                                Text("Edit")
                            }

                            TextButton(
                                onClick = { subjectToDelete = subject },
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Text("Delete")
                            }
                        }
                    }
                }
            }
        }
    }

    // DHURRSHAN'S PART: One form handles adding and editing.
    if (showForm) {
        AlertDialog(
            onDismissRequest = { showForm = false },
            title = {
                Text(
                    if (editingSubject == null) {
                        "Add Subject"
                    } else {
                        "Edit Subject"
                    }
                )
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            nameError = null
                        },
                        label = { Text("Subject name") },
                        placeholder = { Text("Example: Cloud Architecture") },
                        isError = nameError != null,
                        supportingText = {
                            nameError?.let { Text(it) }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description (optional)") },
                        placeholder = {
                            Text("What will you study in this subject?")
                        },
                        minLines = 2,
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val cleanName = name.trim()
                        val currentSubject = editingSubject

                        // Check ALL saved subjects, including hidden search results.
                        val duplicateName = subjects.any { savedSubject ->
                            savedSubject.id != currentSubject?.id &&
                                    savedSubject.name.trim().equals(
                                        cleanName,
                                        ignoreCase = true
                                    )
                        }

                        when {
                            cleanName.isEmpty() -> {
                                nameError = "Please enter a subject name."
                            }

                            duplicateName -> {
                                nameError =
                                    "A subject with this name already exists."
                            }

                            else -> {
                                if (currentSubject == null) {
                                    onAddSubject(
                                        cleanName,
                                        description.trim()
                                    )
                                } else {
                                    onUpdateSubject(
                                        currentSubject.copy(
                                            name = cleanName,
                                            description = description.trim()
                                        )
                                    )
                                }

                                // Reveal the saved subject if a search was active.
                                searchText = ""
                                showForm = false
                            }
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showForm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // DHURRSHAN'S PART: Confirm before deleting a subject and its topics.
    subjectToDelete?.let { subject ->
        AlertDialog(
            onDismissRequest = { subjectToDelete = null },
            title = { Text("Delete subject?") },
            text = {
                Text(
                    "Delete \"${subject.name}\" and its topics? " +
                            "This action cannot be undone."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteSubject(subject)
                        subjectToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { subjectToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}