package com.mohammed.taskflow.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mohammed.taskflow.ui.theme.TaskFlowTheme
import com.mohammed.taskflow.viewmodel.TaskViewModel

@Composable
fun AddEditTaskDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit, // Callback for title and description
    initialTitle: String = "",
    initialDescription: String = "",
    isEditing: Boolean = false,
    taskViewModel: TaskViewModel = viewModel() // Use TaskViewModel
) {
    // Local state for the dialog's text fields
    var title by remember { mutableStateOf(initialTitle) }
    var description by remember { mutableStateOf(initialDescription) }

    // Collect ViewModel states related to dialog (e.g., loading/error for confirmation button)
    val isLoading by taskViewModel.isLoading.collectAsState()
    val errorMessage by taskViewModel.errorMessage.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = if (isEditing) "Edit Task" else "Add New Task")
        },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                // Show error message if any
                errorMessage?.let { message ->
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(title.trim(), description.trim()) // Pass trimmed values back
                    }
                },
                enabled = !isLoading && title.isNotBlank() // Button enabled when not loading and title is not blank
            ) {
                Text(text = if (isEditing) "Save Changes" else "Add Task")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun AddTaskDialogPreview() {
    TaskFlowTheme {
        AddEditTaskDialog(
            onDismiss = {},
            onConfirm = { _, _ -> },
            isEditing = false
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EditTaskDialogPreview() {
    TaskFlowTheme {
        AddEditTaskDialog(
            onDismiss = {},
            onConfirm = { _, _ -> },
            initialTitle = "Sample Task",
            initialDescription = "Sample description",
            isEditing = true
        )
    }
}