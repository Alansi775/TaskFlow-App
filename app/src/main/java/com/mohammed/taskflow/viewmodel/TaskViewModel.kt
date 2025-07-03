package com.mohammed.taskflow.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mohammed.taskflow.model.Task
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class TaskViewModel : ViewModel() {

    // Firebase instances
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val userId = auth.currentUser?.uid // Get current user's ID

    // All states as MutableStateFlow and exposed as StateFlow for consistency and observation
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    // Dialog states
    private val _showAddTaskDialog = MutableStateFlow(false)
    val showAddTaskDialog: StateFlow<Boolean> = _showAddTaskDialog.asStateFlow()

    private val _editingTask = MutableStateFlow<Task?>(null)
    val editingTask: StateFlow<Task?> = _editingTask.asStateFlow()

    init {
        // Ensure user is authenticated before listening to tasks
        if (userId != null) {
            listenToTasks() // Start listening for real-time updates
        } else {
            _errorMessage.value = "User not authenticated. Please log in."
        }
    }

    // ---- Task CRUD Operations ---------

    // Real-time listener for tasks
    private fun listenToTasks() {
        _isLoading.value = true
        userId?.let { uid ->
            firestore.collection("users").document(uid).collection("tasks")
                .orderBy("timestamp") // Order tasks by creation time
                .addSnapshotListener { snapshot, e ->
                    _isLoading.value = false // Stop loading once snapshot is received
                    if (e != null) {
                        _errorMessage.value = "Error listening for tasks: ${e.message}"
                        _tasks.value = emptyList() // Clear tasks on error
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val taskList = snapshot.documents.mapNotNull { doc ->
                            doc.toObject(Task::class.java)?.copy(id = doc.id) // Map to Task object, include ID
                        }
                        _tasks.value = taskList // Update the StateFlow
                        _errorMessage.value = null // Clear error if data loads successfully
                    } else {
                        _tasks.value = emptyList() // No data
                    }
                }
        } ?: run {
            _isLoading.value = false
            _errorMessage.value = "User not logged in."
        }
    }

    // Add a new task with title and description parameters
    fun addTask(title: String, description: String) {
        if (title.isBlank()) {
            _errorMessage.value = "Task title cannot be empty."
            return
        }

        _isLoading.value = true
        _errorMessage.value = null // Clear previous errors

        userId?.let { uid ->
            val newTask = Task(
                title = title.trim(),
                description = description.trim(),
                isCompleted = false,
                timestamp = System.currentTimeMillis() // Set current time
            )

            firestore.collection("users").document(uid).collection("tasks")
                .add(newTask)
                .addOnSuccessListener {
                    _isLoading.value = false
                    // No need to manually update _tasks here, listener will pick it up
                    resetDialogState() // Reset dialog fields after adding
                }
                .addOnFailureListener { e ->
                    _isLoading.value = false
                    _errorMessage.value = "Error adding task: ${e.message}"
                }
        } ?: run {
            _isLoading.value = false
            _errorMessage.value = "User not authenticated for adding task."
        }
    }

    // Toggle task completion status
    fun toggleTaskCompletion(task: Task) {
        _isLoading.value = true
        _errorMessage.value = null

        userId?.let { uid ->
            val newCompletionStatus = !task.isCompleted // Calculate new status
            firestore.collection("users").document(uid).collection("tasks")
                .document(task.id)
                .update("isCompleted", newCompletionStatus) // Update Firestore
                .addOnSuccessListener {
                    _isLoading.value = false
                    // OPTIMISTIC UI UPDATE: Immediately update the local list to reflect changes
                    // This makes the UI feel more responsive, even before Firestore listener fires.
                    val updatedTasks = _tasks.value.map {
                        if (it.id == task.id) {
                            it.copy(isCompleted = newCompletionStatus)
                        } else {
                            it
                        }
                    }
                    _tasks.value = updatedTasks // Update the StateFlow to trigger UI recomposition
                }
                .addOnFailureListener { e ->
                    _isLoading.value = false
                    _errorMessage.value = "Error updating task status: ${e.message}"
                }
        } ?: run {
            _isLoading.value = false
            _errorMessage.value = "User not authenticated for updating task."
        }
    }

    // Set task for editing and open dialog
    fun setTaskToEdit(task: Task?) {
        _editingTask.value = task
        _showAddTaskDialog.value = true
    }

    // Set dialog visibility
    fun setShowAddTaskDialog(show: Boolean) {
        _showAddTaskDialog.value = show
        if (!show) {
            _editingTask.value = null // Clear editing task when dialog is dismissed
        }
    }

    // Update an existing task
    fun updateTask(updatedTask: Task) {
        if (updatedTask.title.isBlank()) {
            _errorMessage.value = "Task title cannot be empty."
            return
        }

        _isLoading.value = true
        _errorMessage.value = null

        userId?.let { uid ->
            firestore.collection("users").document(uid).collection("tasks")
                .document(updatedTask.id)
                .set(updatedTask) // Use set to update the entire document
                .addOnSuccessListener {
                    _isLoading.value = false
                    // No need to manually update _tasks here, listener will pick it up
                    resetDialogState() // Reset dialog fields after updating
                }
                .addOnFailureListener { e ->
                    _isLoading.value = false
                    _errorMessage.value = "Error updating task: ${e.message}"
                }
        } ?: run {
            _isLoading.value = false
            _errorMessage.value = "User not authenticated for updating task."
        }
    }

    // Delete a task
    fun deleteTask(taskId: String) {
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                userId?.let { uid ->
                    firestore.collection("users").document(uid).collection("tasks")
                        .document(taskId)
                        .delete()
                        .await() // Use await() for coroutine style
                    _isLoading.value = false
                    // No need to manually update _tasks here, listener will pick it up
                } ?: run {
                    _isLoading.value = false
                    _errorMessage.value = "User not authenticated for deleting task."
                }
            } catch (e: Exception) {
                _isLoading.value = false
                _errorMessage.value = "Error deleting task: ${e.message}"
            }
        }
    }

    // Reset dialog state after add/edit operation
    private fun resetDialogState() {
        _showAddTaskDialog.value = false
        _editingTask.value = null
        _errorMessage.value = null // Clear error when dialog state resets
    }

    // Function to handle logout (Ensure this is in your AuthViewModel as well!)
    // This logout specifically clears TaskViewModel's state.
    fun logout() {
        auth.signOut() // Sign out from Firebase Auth
        _tasks.value = emptyList() // Clear tasks
        _isLoading.value = false
        _errorMessage.value = null
        resetDialogState() // Reset any dialog-related states
    }
}