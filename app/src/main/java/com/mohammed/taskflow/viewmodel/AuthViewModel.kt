package com.mohammed.taskflow.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {

    // Firebase Authentication instance
    private val auth = FirebaseAuth.getInstance()

    // Observable states for UI (using Compose's mutableStateOf)
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var isLoading by mutableStateOf(false) // For showing loading indicator
        private set // Can only be set within the ViewModel
    var errorMessage by mutableStateOf<String?>(null) // For displaying error messages
        private set

    // Function to handle user login
    fun login(onSuccess: () -> Unit) {
        isLoading = true // Start loading
        errorMessage = null // Clear any previous error messages

        if (email.isBlank() || password.isBlank()) {
            errorMessage = "Email and password cannot be empty."
            isLoading = false
            return
        }

        viewModelScope.launch {
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                onSuccess() // Call success callback on successful login
                resetState() // Clear fields after success
            } catch (e: Exception) {
                errorMessage = e.message // Display Firebase error message
            } finally {
                isLoading = false // End loading
            }
        }
    }

    // Function to handle user sign-up
    fun signUp(onSuccess: () -> Unit) {
        isLoading = true // Start loading
        errorMessage = null // Clear any previous error messages

        if (email.isBlank() || password.isBlank()) {
            errorMessage = "Email and password cannot be empty."
            isLoading = false
            return
        }
        if (password.length < 6) {
            errorMessage = "Password must be at least 6 characters long."
            isLoading = false
            return
        }

        viewModelScope.launch {
            try {
                auth.createUserWithEmailAndPassword(email, password).await()
                onSuccess() // Call success callback on successful sign-up
                resetState() // Clear fields after success
            } catch (e: Exception) {
                errorMessage = e.message // Display Firebase error message
            } finally {
                isLoading = false // End loading
            }
        }
    }

    // Helper to reset fields after successful auth or on demand
    private fun resetState() {
        email = ""
        password = ""
        isLoading = false
        errorMessage = null
    }

    // Check if user is currently logged in
    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    // >>>>>>> THIS IS THE MISSING LOGOUT FUNCTION <<<<<<<
    fun logout() {
        auth.signOut()
        // Optionally clear ViewModel states here upon logout, though navigation will reset views
        email = ""
        password = ""
        isLoading = false
        errorMessage = null
    }
}