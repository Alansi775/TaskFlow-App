package com.mohammed.taskflow.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mohammed.taskflow.ui.theme.TaskFlowTheme
import com.mohammed.taskflow.viewmodel.AuthViewModel

@Composable
fun AuthScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToSignUp: () -> Unit, // New callback for navigating to sign-up
    isSignUp: Boolean = false, // New parameter to determine if it's signup mode
    authViewModel: AuthViewModel = viewModel()
) {
    val email = authViewModel.email
    val password = authViewModel.password
    val isLoading = authViewModel.isLoading
    val errorMessage = authViewModel.errorMessage

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = if (isSignUp) "Create Account" else "Welcome Back!", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { authViewModel.email = it },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { authViewModel.password = it },
            label = { Text("Password") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        errorMessage?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        Button(
            onClick = {
                if (isSignUp) {
                    authViewModel.signUp(onLoginSuccess)
                } else {
                    authViewModel.login(onLoginSuccess)
                }
            },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isSignUp) "Sign Up" else "Login")
        }
        Spacer(modifier = Modifier.height(8.dp))

        // Switch between Login and Sign Up views
        TextButton(
            onClick = {
                // When in login mode, navigate to signup route
                // When in signup mode, you'd typically want to go back to login,
                // but the current setup uses separate routes.
                // For 'isSignUp = true', this button might not be needed or would navigate back.
                if (!isSignUp) {
                    onNavigateToSignUp()
                } else {
                    // If you wanted a "Go to Login" button in signup, you'd need another callback
                    // or navigate directly back here. For now, we only navigate to sign up.
                    // This might be redundant if the main activity controls all navigation.
                }
            },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isSignUp) "Already have an account? Login" else "Don't have an account? Sign Up")
        }

        if (isLoading) {
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AuthScreenLoginPreview() {
    TaskFlowTheme {
        AuthScreen(onLoginSuccess = {}, onNavigateToSignUp = {}, isSignUp = false)
    }
}

@Preview(showBackground = true)
@Composable
fun AuthScreenSignUpPreview() {
    TaskFlowTheme {
        AuthScreen(onLoginSuccess = {}, onNavigateToSignUp = {}, isSignUp = true)
    }
}