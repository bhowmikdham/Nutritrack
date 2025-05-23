package com.fit2081.nutritrack.navigation

import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.fit2081.nutritrack.*
import com.fit2081.nutritrack.QuestionnaireViewModel.QuestionnaireViewModel
import com.fit2081.nutritrack.data.AppDatabase
import com.fit2081.nutritrack.data.Repo.AuthRepository
import com.fit2081.nutritrack.data.Repo.IntakeRepository
import com.fit2081.nutritrack.data.AuthManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/** Sealed routes with helpers for parameterized navigation */
sealed class Screen(val route: String) {
    object Welcome       : Screen("welcome")
    object Login         : Screen("login")
    object Questionnaire : Screen("questionnaire/{userId}") {
        fun createRoute(id: String) = "questionnaire/$id"
    }
    object Dashboard     : Screen("dashboard/{userId}") {
        fun createRoute(id: String) = "dashboard/$id"
    }
}

/**
 * Top-level NavGraph for the app,
 * with dynamic startDestination based on login and quiz state
 */
@Composable
fun NavGraph(
    navController: NavHostController
) {
    val context = LocalContext.current

    // Manual DI of DB → Repos → ViewModels
    val db                     = remember { AppDatabase.getDatabase(context) }
    val authRepo               = remember { AuthRepository(db.patientDAO()) }
    val intakeRepo             = remember { IntakeRepository(db.foodIntakeDAO()) }
    val authViewModel          = remember { AuthViewModel(authRepo) }
    val questionnaireViewModel = remember { QuestionnaireViewModel(intakeRepo) }

    // Determine current user and quiz completion
    val currentUser = AuthManager.currentUserId()
    val hasRecordFlow: Flow<Boolean> = if (currentUser != null) {
        intakeRepo.hasRecord(currentUser)
    } else {
        flowOf(false)
    }
    val quizCompleted by hasRecordFlow.collectAsState(initial = false)

    // Choose start destination dynamically
    val startDest = when {
        currentUser == null     -> Screen.Welcome.route
        quizCompleted            -> Screen.Dashboard.createRoute(currentUser)
        else                     -> Screen.Questionnaire.createRoute(currentUser)
    }

    NavHost(
        navController    = navController,
        startDestination = startDest
    ) {
        // ---------- Welcome ----------
        composable(Screen.Welcome.route) {
            WelcomeScreen(navController = navController, context = context)
        }

        // ---------- Login ----------
        composable(Screen.Login.route) {
            LoginScreen(
                vm = authViewModel,
                onNavigate = { userId ->
                    // Navigate to questionnaire after login
                    navController.navigate(Screen.Questionnaire.createRoute(userId)) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                },
                onRegister = {
                    context.startActivity(Intent(context, RegisterActivity::class.java))
                }
            )
        }

        // ---------- Questionnaire ----------
        composable(Screen.Questionnaire.route) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: return@composable
            QuestionnaireScreen(
                patientId     = userId,
                navController = navController,
                vm            = questionnaireViewModel
            )
        }

        // ---------- Dashboard with Bottom Tabs ----------
        composable(Screen.Dashboard.route) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: return@composable
            val bottomNavController = rememberNavController()

            Scaffold(
                containerColor = Color(0xFFF8F5F5),
                bottomBar      = { BottomMenu(bottomNavController) }
            ) { innerPadding ->
                Box(Modifier.padding(innerPadding)) {
                    NavHost(
                        navController    = bottomNavController,
                        startDestination = "Home"
                    ) {
                        composable("Home")       { HomePage(bottomNavController) }
                        composable("Insights")   { InsightsScreen(bottomNavController) }
                        composable("Nutricoach") { NutricoachScreen(bottomNavController) }
                        composable("Settings")   { SettingsScreen(bottomNavController) }
                    }
                }
            }
        }
    }
}

/** Bottom navigation bar for Dashboard */
@Composable
fun BottomMenu(
    navController: NavHostController
) {
    var selectedIndex by remember { mutableStateOf(0) }
    val items = listOf("Home", "Insights", "Nutricoach", "Settings")

    NavigationBar(containerColor = Color(0xFFC1FF72)) {
        items.forEachIndexed { idx, title ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = when (title) {
                            "Home"       -> Icons.Filled.Home
                            "Insights"   -> Icons.Filled.Search
                            "Nutricoach" -> Icons.Filled.AccountBox
                            else           -> Icons.Filled.Settings
                        },
                        contentDescription = title
                    )
                },
                label    = { Text(title, fontSize = 12.sp) },
                selected = selectedIndex == idx,
                onClick  = {
                    selectedIndex = idx
                    navController.navigate(title) {
                        popUpTo(navController.graph.startDestinationId)
                    }
                }
            )
        }
    }
}
