package com.fit2081.nutritrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.fit2081.nutritrack.ui.theme.NutritrackTheme

class NutricoachActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NutritrackTheme {
                // 1) Create a NavController to pass down
                val navController = rememberNavController()

                // 2) Scaffold (no inner NavHost here—just call your screen)
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    NutricoachScreen(
                        navController = navController,
                        modifier      = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun NutricoachScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    // Replace with your real NutriCoach UI
    Column(
        modifier            = modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text      = "Welcome to NutriCoach!",
            fontSize  = 24.sp,
            fontWeight= FontWeight.Bold
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text     = "Here you’ll get personalized diet tips.",
            fontSize = 16.sp
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick    = { /* e.g. navController.navigate("Insights") */ },
            shape      = RoundedCornerShape(8.dp),
            colors     = ButtonDefaults.buttonColors(containerColor = Color(0xFFC1FF72))
        ) {
            Text("Get Started", fontSize = 16.sp,color=Color.Black)
        }
    }
}
