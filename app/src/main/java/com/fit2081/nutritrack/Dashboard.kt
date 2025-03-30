package com.fit2081.nutritrack

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Image
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.fit2081.nutritrack.ui.theme.NutritrackTheme

class Dashboard : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController: NavHostController = rememberNavController()
            NutritrackTheme {
                // Removed extra call to HomePage() here.
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = { BottomMenu(navController) }
                ) { innerPadding ->
                    Column {
                        MyNavHost(innerPadding, navController)
                    }
                }
            }
        }
    }
}

fun getUserHIEFAScore(context: Context, userId: String): String? {
    try {
        context.assets.open("accounts.csv").bufferedReader().useLines { lines ->
            var isFirstLine = true
            lines.forEach { line ->
                // Skip the header row
                if (isFirstLine) {
                    isFirstLine = false
                    return@forEach
                }
                val tokens = line.split(",")
                // Check for enough columns (at least 5: phone, id, sex, male score, female score)
                if (tokens.size >= 5) {
                    val csvUserId = tokens[1].trim()
                    if (csvUserId == userId) {
                        val sex = tokens[2].trim()
                        return if (sex == "male") {
                            tokens[3].trim() // Male_HIEFA_Score
                        } else if (sex == "female") {
                            tokens[4].trim() // Female_HIEFA_Score
                        } else {
                            null
                        }
                    }
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null
}

@Preview(showBackground = true)
@Composable
fun HomePage() {
    val context = LocalContext.current
    val sharedPrefs = context.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
    val user = sharedPrefs.getString("user_id", "")
    val userHIEFAScore = getUserHIEFAScore(context, user.toString())
    Scaffold(containerColor = Color.White) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Hello,", fontSize = 25.sp, fontWeight = FontWeight.Medium, color = Color.Gray)
            Text("$user", fontSize = 40.sp, color = Color.Black, fontWeight = FontWeight.ExtraBold)
            Image(
                painter = painterResource(id = R.drawable.plate),
                contentDescription = "plate",
                modifier = Modifier.size(360.dp),
                alignment = Alignment.Center,
                contentScale = ContentScale.FillWidth
            )
            Row {
                Text("My Score", fontSize = 30.sp, fontWeight = FontWeight.Bold)
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Button(
                        modifier = Modifier.height(35.dp),
                        onClick = { context.startActivity(Intent(context, Login::class.java)) },
                        colors = ButtonDefaults.buttonColors(Color.White)
                    ) {
                        Text("See all Scores", color = Color.Gray)
                    }
                }
            }
            Row {
                Column {
                    Image(
                        painter = painterResource(id = R.drawable.greenarrow),
                        contentDescription = "Green Arrow",
                        modifier = Modifier.size(100.dp)
                    )
                }
                Column(
                    modifier = Modifier.height(100.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Your Food Quality Score",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Text("What is The Food Quality Score?", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                "Your Food Quality Score provides a snapshot of how well your eating patterns align with established food guidelines, helping you identify both strengths and opportunities for improvement in your diet.",
                fontSize = 12.sp, fontWeight = FontWeight.Medium, lineHeight = 17.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                "This personalized measurement considers various food groups including vegetables, fruits, whole grains, and proteins to give you practical insights for making healthier food choices.",
                fontSize = 12.sp, fontWeight = FontWeight.Medium, lineHeight = 17.sp
            )
        }
    }
}

@Composable
fun Insights() {
    Text("Insights")
}

@Composable
fun Nutricoach() {
    Text("Nutricoach")
}

@Composable
fun Settings() {
    Text("Settings")
}

@Composable
fun MyNavHost(innerpadding: PaddingValues, navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "Home" // Fixed startDestination to "Home"
    ) {
        composable("Home") {
            HomePage()
        }
        composable("Insights") {
            Insights()
        }
        composable("Nutricoach") {
            Nutricoach()
        }
        composable("Settings") {
            Settings()
        }
    }
}

@Composable
fun BottomMenu(navController: NavHostController) {
    var selected_item by remember { mutableStateOf(0) }
    val items = listOf("Home", "Insights", "Nutricoach", "Settings")

    NavigationBar (containerColor = Color(0xFFC1FF72)) {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                icon = {
                    when (item) {
                        "Home" -> Icon(Icons.Filled.Home, contentDescription = "Home")
                        "Insights" -> Icon(Icons.Filled.Home, contentDescription = "Home")
                        "Nutricoach" -> Icon(Icons.Filled.Home, contentDescription = "Home")
                        "Settings" -> Icon(Icons.Filled.Home, contentDescription = "Home")

                    }
                },
                label = { Text(item) },
                selected = selected_item == index,
                onClick = {
                    selected_item = index
                    navController.navigate(item)
                }
            )
        }
    }
}
