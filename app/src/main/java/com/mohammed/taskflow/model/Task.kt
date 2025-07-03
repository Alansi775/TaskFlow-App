package com.mohammed.taskflow.model

import com.google.firebase.firestore.DocumentId

// Data class to represent a single Task item
data class Task(
    @DocumentId // This annotation tells Firebase to use this field for the document ID
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val isCompleted: Boolean = false,
    val timestamp: Long = System.currentTimeMillis() // Timestamp for creation/ordering
)