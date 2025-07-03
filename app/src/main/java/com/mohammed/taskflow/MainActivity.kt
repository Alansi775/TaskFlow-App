package com.mohammed.taskflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel // Import AuthViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.mohammed.taskflow.screens.AuthScreen
import com.mohammed.taskflow.screens.TaskListScreen
import com.mohammed.taskflow.ui.theme.TaskFlowTheme
import com.mohammed.taskflow.util.Routes // Make sure this is imported
import com.mohammed.taskflow.viewmodel.AuthViewModel // Make sure this is imported

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TaskFlowTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    TaskFlowApp(navController = navController)
                }
            }
        }
    }
}

@Composable
fun TaskFlowApp(navController: NavHostController) {
    val authViewModel: AuthViewModel = viewModel() // Get AuthViewModel instance
    val firebaseAuth = FirebaseAuth.getInstance() // Use this for initial check if user is logged in

    // Determine start destination based on login status
    val startDestination = if (firebaseAuth.currentUser != null && authViewModel.isUserLoggedIn()) {
        // Double-check with ViewModel for robustness
        Routes.TASK_LIST
    } else {
        Routes.LOGIN
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.LOGIN) {
            AuthScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.TASK_LIST) {
                        popUpTo(Routes.LOGIN) { inclusive = true } // Remove login from back stack
                    }
                },
                onNavigateToSignUp = {
                    navController.navigate(Routes.SIGN_UP) // Navigate to the separate SIGN_UP route
                },
                isSignUp = false, // Explicitly set for login mode
                authViewModel = authViewModel // Pass the AuthViewModel instance
            )
        }
        composable(Routes.SIGN_UP) {
            AuthScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.TASK_LIST) {
                        popUpTo(Routes.LOGIN) { inclusive = true } // Remove signup from back stack, go to task list
                    }
                },
                onNavigateToSignUp = {
                    // If in SIGN_UP and this button is clicked, it usually means navigate back to LOGIN
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.SIGN_UP) { inclusive = true }
                    }
                },
                isSignUp = true, // Explicitly set for signup mode
                authViewModel = authViewModel // Pass the AuthViewModel instance
            )
        }
        composable(Routes.TASK_LIST) {
            TaskListScreen(
                onLogout = {
                    // This logout should trigger logout in AuthViewModel too
                    authViewModel.logout() // Make sure AuthViewModel's logout is called
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.TASK_LIST) { inclusive = true } // Clear task list from back stack
                    }
                }
            )
        }
    }
}