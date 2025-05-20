package com.fit2081.nutritrack.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.fit2081.nutritrack.WelcomeScreen
import com.fit2081.nutritrack.LoginScreen
import com.fit2081.nutritrack.QuestionnaireScreen
import com.fit2081.nutritrack.QuestionnaireViewModel.QuestionnaireViewModel
import com.fit2081.nutritrack.AuthViewModel
import com.fit2081.nutritrack.data.AppDatabase
import com.fit2081.nutritrack.data.Repo.IntakeRepository
import com.fit2081.nutritrack.data.Repo.AuthRepository
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

sealed class Screen(val route: String) {
    object Welcome : Screen("welcome")
    object Login : Screen("login")
    object Questionnaire : Screen("questionnaire/{userId}") {
        fun createRoute(id: String) = "questionnaire/$id"
    }
    object Dashboard : Screen("dashboard/{userId}") {
        fun createRoute(id: String) = "dashboard/$id"
    }
    object Coach : Screen("coach/{userId}") {
        fun createRoute(id: String) = "coach/$id"
    }
    object Settings : Screen("settings")
}

@Composable
fun NavGraph(modifier: Modifier = Modifier) {
    val navController: NavHostController = rememberNavController()
    val context = LocalContext.current
    // Manual DI: Create your repos and viewmodels here
    val db = remember { AppDatabase.getDatabase(context) }
    val authRepo = remember { AuthRepository(db.patientDAO()) }
    val intakeRepo = remember { IntakeRepository(db.foodIntakeDAO()) }
    val authViewModel = remember { AuthViewModel(authRepo) }
    val questionnaireViewModel = remember { QuestionnaireViewModel(intakeRepo) }

    NavHost(navController = navController, startDestination = Screen.Welcome.route, modifier = modifier) {
        composable(Screen.Welcome.route) {
            WelcomeScreen(
                navController,
                context = TODO(),
                modifier = TODO()
            )
        }
        composable(Screen.Login.route) {
            LoginScreen(vm = authViewModel, onSuccess = { id ->
                navController.navigate(Screen.Dashboard.createRoute(id))
            })
        }
        composable(Screen.Questionnaire.route) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("userId") ?: return@composable
            QuestionnaireScreen(patientId = id, navController = navController, vm = questionnaireViewModel)
        }
        composable(Screen.Dashboard.route) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("userId") ?: return@composable
            //DashboardScreen(patientId = id)
        }
        composable(Screen.Coach.route) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("userId") ?: return@composable
            //CoachScreen(patientId = id)
        }
        composable(Screen.Settings.route) {
            //SettingsScreen()
        }
    }
}
