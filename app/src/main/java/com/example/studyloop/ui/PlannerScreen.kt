package com.example.studyloop.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.studyloop.data.RevisionSessionEntity
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlannerScreen(
    sessions: List<RevisionSessionEntity> = emptyList(),
    onAddSession: (String, String, String) -> Unit = { _, _, _ -> },
    onDeleteSession: (RevisionSessionEntity) -> Unit = {}
) {
    // Stores the information entered in the revision form.
    var topicText by remember { mutableStateOf("") }
    var dateText by remember { mutableStateOf("") }
    var descriptionText by remember { mutableStateOf("") }

    // Controls validation, date selection and deletion confirmation.
    var topicError by remember { mutableStateOf<String?>(null) }
    var dateError by remember { mutableStateOf<String?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    var sessionToDelete by remember {
        mutableStateOf<RevisionSessionEntity?>(null)
    }

    // Stores the date selected through the calendar.
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            top = 16.dp,
            end = 16.dp,
            bottom = 32.dp
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "Revision Planner",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Schedule topics that you want to review.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Allows the learner to enter the topic being revised.
        item {
            OutlinedTextField(
                value = topicText,
                onValueChange = {
                    topicText = it
                    topicError = null
                },
                label = { Text("Topic to review") },
                placeholder = { Text("e.g. AWS Features") },
                isError = topicError != null,
                supportingText = {
                    topicError?.let {
                        Text(it)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        // Opens the date picker instead of accepting an unclear date.
        item {
            OutlinedTextField(
                value = dateText,
                onValueChange = {},
                readOnly = true,
                label = { Text("Revision date") },
                placeholder = { Text("Select a date") },
                isError = dateError != null,
                supportingText = {
                    dateError?.let {
                        Text(it)
                    }
                },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            dateError = null
                            showDatePicker = true
                        }
                    ) {
                        // Emoji avoids requiring an additional icon library.
                        Text(
                            text = "📅",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Stores optional instructions about what should be revised.
        item {
            OutlinedTextField(
                value = descriptionText,
                onValueChange = {
                    descriptionText = it
                },
                label = {
                    Text("Revision details or notes")
                },
                placeholder = {
                    Text("e.g. Review EC2, S3, Lambda and RDS")
                },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4
            )
        }

        // Validates the form before saving it to Room.
        item {
            Button(
                onClick = {
                    topicError =
                        if (topicText.isBlank()) {
                            "Please enter a topic."
                        } else {
                            null
                        }

                    dateError =
                        if (dateText.isBlank()) {
                            "Please select a revision date."
                        } else {
                            null
                        }

                    if (
                        topicError == null &&
                        dateError == null
                    ) {
                        onAddSession(
                            topicText.trim(),
                            dateText,
                            descriptionText.trim()
                        )

                        topicText = ""
                        dateText = ""
                        descriptionText = ""
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Revision Schedule")
            }
        }

        item {
            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Scheduled Revisions",
                style = MaterialTheme.typography.titleMedium
            )
        }

        // Displays an empty state when no schedules exist.
        if (sessions.isEmpty()) {
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
                            text = "No revision sessions scheduled",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Select a topic and date to create your first revision schedule.",
                            style = MaterialTheme.typography.bodyMedium,
                            color =
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            // Displays revision schedules saved in the Room database.
            items(
                items = sessions,
                key = { it.id }
            ) { session ->
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement =
                                Arrangement.SpaceBetween,
                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = session.title,
                                    style =
                                        MaterialTheme.typography.titleMedium
                                )

                                Spacer(
                                    modifier = Modifier.height(4.dp)
                                )

                                Text(
                                    text = "Review on ${session.dueDate}",
                                    style =
                                        MaterialTheme.typography.labelMedium,
                                    color =
                                        MaterialTheme.colorScheme.primary
                                )
                            }

                            TextButton(
                                onClick = {
                                    sessionToDelete = session
                                }
                            ) {
                                Text(
                                    text = "Delete",
                                    color =
                                        MaterialTheme.colorScheme.error
                                )
                            }
                        }

                        if (session.description.isNotBlank()) {
                            Spacer(
                                modifier = Modifier.height(8.dp)
                            )

                            Text(
                                text = session.description,
                                style =
                                    MaterialTheme.typography.bodyMedium,
                                color =
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    // Allows the learner to select an exact revision date.
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = {
                showDatePicker = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            dateText = formatRevisionDate(it)
                        }

                        dateError = null
                        showDatePicker = false
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDatePicker = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(
                state = datePickerState
            )
        }
    }

    // Requests confirmation before permanently deleting a schedule.
    sessionToDelete?.let { session ->
        AlertDialog(
            onDismissRequest = {
                sessionToDelete = null
            },
            title = {
                Text("Delete Revision Schedule?")
            },
            text = {
                Text(
                    "The schedule for \"${session.title}\" will be permanently removed."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteSession(session)
                        sessionToDelete = null
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
                        sessionToDelete = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

// Converts the selected date into the format used by the dashboard.
private fun formatRevisionDate(dateMillis: Long): String {
    return SimpleDateFormat(
        "dd MMM yyyy",
        Locale.getDefault()
    ).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }.format(Date(dateMillis))
}