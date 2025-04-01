package com.fit2081.nutritrack

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Image
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
import com.fit2081.nutritrack.ui.theme.NutritrackTheme
import kotlin.math.floor
class Dashboard : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController: NavHostController = rememberNavController()
            NutritrackTheme {
                // Removed extra call to HomePage() here.
                Scaffold(
                    containerColor = Color(0xFFF8F5F5),
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
                val csvUserId = tokens[1].trim()
                    if (csvUserId == userId) {
                        val sex = tokens[2].trim()
                        return if (sex == "Male") {
                            tokens[3].trim() // Male_HIEFA_Score
                        } else if (sex == "Female") {
                            tokens[4].trim() // Female_HIEFA_Score
                        } else {
                            null
                        }
                    }
                }
            }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null
}

fun getCurrentUserNutrientScore(context: Context, nutrient: String): String {
    // Retrieve the logged-in user's ID from SharedPreferences
    val sharedPrefs = context.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
    val userId = sharedPrefs.getString("user_id", "") ?: ""

    // Open the CSV file from assets and read line by line
    context.assets.open("accounts.csv").bufferedReader().useLines { lines ->
        var isHeader = true
        lines.forEach { line ->
            if (isHeader) {
                isHeader = false  // Skip the header row
                return@forEach
            }
            val tokens = line.split(",")
            // Check if this row belongs to the current user (User_ID is at index 1)
            if (tokens[1].trim() == userId) {
                val sex = tokens[2].trim().lowercase() // expecting "male" or "female"
                return when (nutrient.lowercase()) {
                    "vegetables" -> if (sex == "male") tokens[8].trim() else tokens[9].trim()
                    "fruit" -> if (sex == "male") tokens[19].trim() else tokens[20].trim()
                    "grains and cereals" -> if (sex == "male") tokens[29].trim() else tokens[30].trim()
                    "whole grains" -> if (sex == "male") tokens[33].trim() else tokens[34].trim()
                    "meat and alternatives" -> if (sex == "male") tokens[36].trim() else tokens[37].trim()
                    "dairy and alternatives" -> if (sex == "male") tokens[40].trim() else tokens[41].trim()
                    "sodium" -> if (sex == "male") tokens[43].trim() else tokens[44].trim()
                    "alcohol" -> if (sex == "male") tokens[46].trim() else tokens[47].trim()
                    "water" -> if (sex == "male") tokens[49].trim() else tokens[50].trim()
                    "sugar" -> if (sex == "male") tokens[54].trim() else tokens[55].trim()
                    "saturated fats" -> if (sex == "male") tokens[57].trim() else tokens[58].trim()
                    "unsaturated fats" -> if (sex == "male") tokens[60].trim() else tokens[61].trim()
                    else -> ""
                }
            }
        }
    }
    return ""
}
@Composable
fun HomePage() {
    val context = LocalContext.current
    val sharedPrefs = context.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
    val user = sharedPrefs.getString("user_id", "")
    val userHIEFAScore = getUserHIEFAScore(context, user.toString())?.toDoubleOrNull() ?: 0.0
    val scoreint = userHIEFAScore.toInt()
    // making up a value which stores the cuurent picture of the arrow to show
    val arrow = if (scoreint>= 80) {
        R.drawable.greenarrow
    }
    else if (scoreint in 50 until 70) {
        R.drawable.yellowdash
    }
    else {
        R.drawable.redcross
    }
    Scaffold(containerColor = Color.White) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Hello,", fontSize = 25.sp, fontWeight = FontWeight.Medium, color = Color.Gray)
            Text("$user", fontSize = 40.sp, color = Color.Black, fontWeight = FontWeight.ExtraBold)
            Text("You've already filled in your Questionnaire but you can still change your details here")
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Absolute.Right,
                verticalAlignment = Alignment.CenterVertically) {
            Button(
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC1FF72),),
                onClick = {
                    context.startActivity(Intent(context, Questionnaire::class.java))
                }
            ) {
                Text("Edit Details", color = Color.Black)
            }}
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
                        painter = painterResource(id = arrow),
                        contentDescription = "Arrow",
                        modifier = Modifier.size(70.dp)
                    )
                }
                Column(
                    modifier = Modifier.height(70.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Your Food Quality Score",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Column(modifier = Modifier.
                    height(70.dp)
                    .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center)
                {
                    val color = if (scoreint >= 80) {
                        Color.Green
                    } else if (scoreint in 50 until 70) {
                        Color(0xFFFFDE59)
                    } else {
                        Color(0xFFFF5757)
                    }
                    Text(
                        "$scoreint/100", color = color, fontSize = 30.sp, fontWeight = FontWeight.Bold
                    )
                }
            }
            Text("What is The Food Quality Score?", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                "Your Food Quality Score provides a snapshot of how well your eating patterns align with established food guidelines, helping you identify both strengths and opportunities for improvement in your diet.",
                fontSize = 12.sp, fontWeight = FontWeight.Medium, lineHeight = 17.sp
            )
        }
    }
}

@Composable
// Helper function that reads the CSV file and returns the nutrient score
fun getNutrientScore(context: Context, userId: String, nutrient: String): String {
    context.assets.open("accounts.csv").bufferedReader().useLines { lines ->
        var isHeader = true
        lines.forEach { line ->
            if (isHeader) {
                // Skip the header row
                isHeader = false
                return@forEach
            }
            val tokens = line.split(",")
            // Check for matching user (User_ID is at index 1)
            if (tokens[1].trim() == userId) {
                val sex = tokens[2].trim().lowercase() // "male" or "female"
                return when (nutrient.lowercase()) {
                    "vegetables" -> if (sex == "male") tokens[8].trim() else tokens[9].trim()
                    "fruit" -> if (sex == "male") tokens[19].trim() else tokens[20].trim()
                    "grains and cereals" -> if (sex == "male") tokens[29].trim() else tokens[30].trim()
                    "whole grains" -> if (sex == "male") tokens[33].trim() else tokens[34].trim()
                    "meat and alternatives" -> if (sex == "male") tokens[36].trim() else tokens[37].trim()
                    "dairy and alternatives" -> if (sex == "male") tokens[40].trim() else tokens[41].trim()
                    "sodium" -> if (sex == "male") tokens[43].trim() else tokens[44].trim()
                    "alcohol" -> if (sex == "male") tokens[46].trim() else tokens[47].trim()
                    "water" -> if (sex == "male") tokens[49].trim() else tokens[50].trim()
                    "sugar" -> if (sex == "male") tokens[54].trim() else tokens[55].trim()
                    "saturated fats" -> if (sex == "male") tokens[57].trim() else tokens[58].trim()
                    "unsaturated fats" -> if (sex == "male") tokens[60].trim() else tokens[61].trim()
                    else -> ""
                }
            }
        }
    }
    return ""
}
@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun Insights() {
    val context = LocalContext.current
    val sharedPrefs = context.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
    val userId = sharedPrefs.getString("user_id", "") ?: ""

    // List of nutrients (must match your CSV column mapping in getNutrientScore)
    val nutrientList = listOf(
        "vegetables",
        "fruit",
        "grains and cereals",
        "whole grains",
        "meat and alternatives",
        "dairy and alternatives",
        "water",
        "unsaturated fats",
        "sodium",
        "sugar",
        "alcohol",
        "discretionary foods" // If you have a CSV column for this
    )

    // Purple shades for the progress bars
    val progressColor = Color(0xFF9C27B0)       // Active bar color
    val trackColor = Color(0xFFE1BEE7)         // Background track color

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Insights") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFFC1FF72)
                )
            )
        },
        containerColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Text("Your Nutrient Scores", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))

            // Display each nutrient row
            nutrientList.forEach { nutrient ->
                // 1) Fetch the score from CSV (0..100)
                val scoreStr = getNutrientScore(context, userId, nutrient)
                val rawScore = scoreStr.toFloatOrNull() ?: 0f

                // 2) Convert 0..100 to 0..10 for display, if desired
                //    (so 100 -> 10/10, 50 -> 5/10, etc.)
                val outOf10 = rawScore / 10f

                // 3) Convert to fraction for progress bar (0f..1f)
                val fraction = outOf10 / 10f // same as rawScore / 100f

                // Row: Nutrient label on left, "X/10" on right
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = nutrient.replaceFirstChar { it.uppercaseChar() },
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "${outOf10.toInt()}/10",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // Progress bar below the row
                LinearProgressIndicator(
                    progress = {fraction},
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .padding(vertical = 4.dp),
                    color = progressColor,
                    trackColor = trackColor
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Finally, show the total score out of 100
            Spacer(modifier = Modifier.height(16.dp))
            val totalScoreStr = getUserHIEFAScore(context, userId)
            val totalScore = totalScoreStr?.toFloatOrNull() ?: 0f
            val totalFraction = totalScore / 100f

            Text("Total Food Quality Score", style = MaterialTheme.typography.bodyLarge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Progress bar for total
                LinearProgressIndicator(
                    progress = totalFraction,
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .padding(vertical = 4.dp, horizontal = 8.dp),
                    color = progressColor,
                    trackColor = trackColor
                )
                Text("${totalScore.toInt()}/100", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
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
                        "Insights" -> Icon(Icons.Filled.Search, contentDescription = "Home")
                        "Nutricoach" -> Icon(Icons.Filled.AccountBox, contentDescription = "Home")
                        "Settings" -> Icon(Icons.Filled.Settings, contentDescription = "Home")

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
