package com.fit2081.nutritrack.navigation

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.fit2081.nutritrack.*
import com.fit2081.nutritrack.Feature.AuthViewModel
import com.fit2081.nutritrack.Feature.RegisterViewModel
import com.fit2081.nutritrack.Feature.QuestionnaireViewModel
import com.fit2081.nutritrack.Feature.QuestionnaireViewModelFactory
import com.fit2081.nutritrack.data.AppDatabase
import com.fit2081.nutritrack.data.Repo.AuthRepository
import com.fit2081.nutritrack.data.Repo.IntakeRepository

sealed class Screen(val route: String) {
    object Welcome           : Screen("welcome")
    object Login             : Screen("login")
    object Register          : Screen("register")
    object ForgotPassword    : Screen("forgot_password")
    object ClinicianLogin    : Screen("clinician_login")
    object ClinicianDashboard: Screen("clinician_dashboard")
    object Questionnaire     : Screen("questionnaire/{userId}") {
        fun createRoute(id: String) = "questionnaire/$id"
    }
    object Dashboard         : Screen("dashboard/{userId}") {
        fun createRoute(id: String) = "dashboard/$id"
    }
}

@Composable
fun NavGraph(
    navController: NavHostController
) {
    val context = LocalContext.current

    NavHost(
        navController = navController,
        startDestination = Screen.Welcome.route
    ) {
        // Welcome Screen
        composable(Screen.Welcome.route) {
            WelcomeScreen(navController = navController, context = context)
        }

        // Login Screen
        composable(Screen.Login.route) {
            val db = remember { AppDatabase.getDatabase(context) }
            val authRepo = remember { AuthRepository(db.patientDAO()) }
            val authViewModel: AuthViewModel = viewModel(
                factory = AuthViewModelFactory(authRepo)
            )

            LoginScreen(
                vm = authViewModel,
                onNavigate = { userId ->
                    val prefs = context.getSharedPreferences("prefs_$userId", Context.MODE_PRIVATE)
                    val completed = prefs.getBoolean("QuestionnaireCompleted", false)
                    val dest = if (completed) {
                        Screen.Dashboard.createRoute(userId)
                    } else {
                        Screen.Questionnaire.createRoute(userId)
                    }
                    navController.navigate(dest) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                },
                onRegister = {
                    navController.navigate(Screen.Register.route)
                },
                navController = navController 
            )
        }

        // Register Screen
        composable(Screen.Register.route) {
            val db = remember { AppDatabase.getDatabase(context) }
            val authRepo = remember { AuthRepository(db.patientDAO()) }
            val registerViewModel: RegisterViewModel = viewModel(
                factory = RegisterViewModelFactory(authRepo)
            )

            RegisterScreen(
                vm = registerViewModel,
                onRegistered = { userId ->
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        // Forgot Password Screen
        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(navController = navController)
        }

        // Clinician Login Screen
        composable(Screen.ClinicianLogin.route) {
            ClinicianLoginScreen(navController = navController)
        }

        // Clinician Dashboard Screen
        composable(Screen.ClinicianDashboard.route) {
            ClinicianDashboardScreen(navController = navController)
        }

        // Questionnaire Screen
        composable(Screen.Questionnaire.route) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: return@composable
            val db = remember { AppDatabase.getDatabase(context) }
            val intakeRepo = remember { IntakeRepository(db.foodIntakeDAO()) }
            val questionnaireViewModel: QuestionnaireViewModel = viewModel(
                factory = QuestionnaireViewModelFactory(intakeRepo)
            )

            QuestionnaireScreen(
                patientId = userId,
                navController = navController,
                vm = questionnaireViewModel,
                onComplete = {
                    // Set the completion flag
                    val prefs = context.getSharedPreferences("prefs_$userId", Context.MODE_PRIVATE)
                    prefs.edit().putBoolean("QuestionnaireCompleted", true).apply()

                    // Navigate to dashboard
                    navController.navigate(Screen.Dashboard.createRoute(userId)) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                }
            )
        }

        // Dashboard with Bottom Navigation
        composable(Screen.Dashboard.route) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: return@composable
            DashboardWithBottomNav(userId, navController)
        }
    }
}

@Composable
fun DashboardWithBottomNav(
    userId: String,
    mainNavController: NavHostController
) {
    val bottomNavController = rememberNavController()
    var selectedIndex by remember { mutableStateOf(0) }

    Scaffold(
        containerColor = Color(0xFFF8F5F5),
        bottomBar = {
            NavigationBar(containerColor = Color(0xFFC1FF72)) {
                val items = listOf("Home", "Insights", "Nutricoach", "Settings")
                items.forEachIndexed { idx, title ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = when (title) {
                                    "Home"       -> Icons.Filled.Home
                                    "Insights"   -> Icons.Filled.Search
                                    "Nutricoach" -> Icons.Filled.AccountBox
                                    else         -> Icons.Filled.Settings
                                },
                                contentDescription = title
                            )
                        },
                        label = { Text(title, fontSize = 12.sp) },
                        selected = selectedIndex == idx,
                        onClick = {
                            selectedIndex = idx
                            bottomNavController.navigate(title) {
                                popUpTo("Home") { inclusive = (title == "Home") }
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(Modifier.padding(innerPadding)) {
            NavHost(
                navController = bottomNavController,
                startDestination = "Home"
            ) {
                composable("Home") {
                    HomePage(mainNavController)
                }
                composable("Insights") {
                    InsightsScreen(
                        navController = mainNavController,
                        onNavigateToNutricoach = {
                            bottomNavController.navigate("Nutricoach") {
                                // optional: clear backstack or launch single top
                                popUpTo("Home") { inclusive = false }
                                launchSingleTop = true
                            }
                        }
                    )
                }
                composable("Nutricoach") {
                    NutricoachScreen(mainNavController)
                }
                composable("Settings") {
                    SettingsScreen(mainNavController)
                }
            }
        }
    }
}