package com.fit2081.nutritrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.fit2081.nutritrack.LoginScreen
import com.fit2081.nutritrack.ui.theme.NutritrackTheme
import com.fit2081.nutritrack.navigation.Screen
import com.fit2081.nutritrack.Dashboard
import com.fit2081.nutritrack.Questionnaire.QuestionnaireActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NutritrackTheme {
                val navController = rememberNavController()
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Welcome.route,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(Screen.Welcome.route) {
                            WelcomeScreen(navController)
                        }
                        composable(Screen.Login.route) {
                            LoginScreen(onSuccess = { userId ->
                                // After successful login, decide destination
                                val prefs = getSharedPreferences("prefs_$userId", MODE_PRIVATE)
                                val completed = prefs.getBoolean("QuestionnaireCompleted", false)
                                val dest = if (completed) {
                                    Screen.Dashboard.createRoute(userId)
                                } else {
                                    Screen.Questionnaire.createRoute(userId)
                                }
                                navController.navigate(dest) {
                                    popUpTo(Screen.Welcome.route) { inclusive = true }
                                }
                            })
                        }
                        composable(Screen.Questionnaire.route) { backStack ->
                            val id = backStack.arguments?.getString("userId") ?: return@composable
                            //QuestionnaireActivity.start(this@MainActivity, id)
                        }
                        composable(Screen.Dashboard.route) { backStack ->
                            val id = backStack.arguments?.getString("userId") ?: return@composable
                            //Dashboard(userId = id)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WelcomeScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color(0xFFC1FF72)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.main_logo),
                contentDescription = "Main logo",
                modifier = Modifier.size(400.dp),
                contentScale = ContentScale.FillBounds
            )
            Text(
                text = "This app provides general health and nutrition information for educational purposes only. It is not intended as " +
                        "medical advice, diagnosis, or treatment. Always consult a qualified healthcare professional " +
                        "before making any changes to your diet, exercise, or health regimen. " +
                        "Use this app at your own risk. " +
                        "If you’d like to an Accredited Practicing Dietitian (APD)," +
                        " please visit the Monash Nutrition/Dietetics Clinic (discounted rates for students):",
                color = Color(0xFF000000),
                textAlign = TextAlign.Center,
                fontSize = 12.sp
            )
            Text(
                text = "https://www.monash.edu/medicine/scs/nu",
                fontWeight = FontWeight.Bold,
                color = Color(0xFF000000)
            )
            Spacer(modifier = Modifier.height(40.dp))
            Button(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF8F5F5)),
                onClick = {
                    navController.navigate(Screen.Login.route)
                }
            ) {
                Text("Login", color = Color(0xFF000000))
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Designed By Bhowmik Dham(34337229)",
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                color = Color(0xFF000000)
            )
        }
    }
}
