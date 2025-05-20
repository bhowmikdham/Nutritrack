package com.fit2081.nutritrack.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.fit2081.nutritrack.Questionnaire.QuestionnaireScreen
import com.fit2081.nutritrack.Dashboard
import com.fit2081.nutritrack.Nutricoach
import com.fit2081.nutritrack.Settings
import com.fit2081.nutritrack.WelcomeScreen
import com.fit2081.nutritrack.LoginScreen

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
    NavHost(navController = navController, startDestination = Screen.Login.route, modifier = modifier) {
        composable(Screen.Welcome.route) {
            WelcomeScreen(navController)
        }
        composable(Screen.Login.route) {
            LoginScreen(onSuccess = { id -> navController.navigate(Screen.Dashboard.createRoute(id)) })
        }
        composable(Screen.Questionnaire.route) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("userId") ?: return@composable
            QuestionnaireScreen(patientId = id, navController=navController)
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
