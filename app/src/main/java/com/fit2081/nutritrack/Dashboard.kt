package com.fit2081.nutritrack

import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_SEND
import androidx.compose.foundation.Image
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
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
import com.fit2081.nutritrack.Questionnaire
import com.fit2081.nutritrack.QuestionnaireViewModel.QuestionnaireViewModel
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
//function to  fetch out the HIEFAScore of the user
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
                val csv_value = line.split(",")
                val csvUserId = csv_value[1].trim()
                    if (csvUserId == userId) {
                        val sex = csv_value[2].trim()
                        return if (sex == "Male") {
                            csv_value[3].trim() // Male_HIEFA_Score
                        } else if (sex == "Female") {
                            csv_value[4].trim() // Female_HIEFA_Score
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

@Composable
fun HomePage(navController: NavHostController) {
    val context = LocalContext.current
    val sharedPrefs = context.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
    val user = sharedPrefs.getString("user_id", "")
    val userHIEFAScore = getUserHIEFAScore(context, user.toString())?.toDoubleOrNull() ?: 0.0
    val scoreint = userHIEFAScore.toInt()
    // making up a value which stores the cuurent picture of the arrow to show depending on the values of the score
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
                    //context.startActivity(Intent(context, QuestionnaireScreen::class.java))
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
                        onClick = { navController.navigate("Insights") },
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
                    /**
                     *
                     * we change the colour of the score depending on the range defined
                     *
                     */
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
// Helper function that reads the CSV file and returns the nutrient score\
// Help was taken from Chatgpt to draft the following function
fun getNutrientScore(context: Context, userId: String, nutrient: String): String {
    context.assets.open("accounts.csv").bufferedReader().useLines { lines ->
        var isHeader = true
        lines.forEach { line ->
            if (isHeader) {
                // Skipping the header row
                isHeader = false
                return@forEach
            }
            val csv_value = line.split(",")
            // Check for matching user (User_ID is at index 1)
            if (csv_value[1].trim() == userId) {
                val sex = csv_value[2].trim().lowercase()//  handles exception if data is not normalised to letters being typed in different case 
                return when (nutrient.lowercase()) {
                    "vegetables" -> if (sex == "male") csv_value[8].trim() else csv_value[9].trim()
                    "fruit" -> if (sex == "male") csv_value[19].trim() else csv_value[20].trim()
                    "grains and cereals" -> if (sex == "male") csv_value[29].trim() else csv_value[30].trim()
                    "whole grains" -> if (sex == "male") csv_value[33].trim() else csv_value[34].trim()
                    "meat and alternatives" -> if (sex == "male") csv_value[36].trim() else csv_value[37].trim()
                    "dairy and alternatives" -> if (sex == "male") csv_value[40].trim() else csv_value[41].trim()
                    "sodium" -> if (sex == "male") csv_value[43].trim() else csv_value[44].trim()
                    "alcohol" -> if (sex == "male") csv_value[46].trim() else csv_value[47].trim()
                    "water" -> if (sex == "male") csv_value[49].trim() else csv_value[50].trim()
                    "sugar" -> if (sex == "male") csv_value[54].trim() else csv_value[55].trim()
                    "saturated fats" -> if (sex == "male") csv_value[57].trim() else csv_value[58].trim()
                    "unsaturated fats" -> if (sex == "male") csv_value[60].trim() else csv_value[61].trim()
                    "discretionary foods" -> if (sex == "male") csv_value[5].trim() else csv_value[6].trim()
                    else -> ""
                }
            }
        }
    }
    return ""
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Insights(navController: NavHostController) {
    val context = LocalContext.current
    val sharedPrefs = context.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
    val userId = sharedPrefs.getString("user_id", "") ?: ""

    // List of nutrients to display
    val nutrientList = listOf(
        "vegetables",
        "fruit",
        "grains and cereals",
        "whole grains",
        "meat and alternatives",
        "dairy and alternatives",
        "sodium",
        "alcohol",
        "water",
        "sugar",
        "saturated fats",
        "unsaturated fats",
        "discretionary foods"
    )

    // Following are the colors definded for the progress bars
    val progressColor = Color(0xFF137A44)
    val trackColor = Color(0xFFD7E6D6)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Insights",fontWeight = FontWeight.ExtraBold, fontSize = 20.sp) },
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
            Text("Your Nutrient Scores",fontWeight = FontWeight.Bold, fontSize = 30.sp)
            Spacer(modifier = Modifier.height(16.dp))
            /**
             *
             * help taken from chatGpt for debugging purposes
             * help taken from chat Gpt for scaling values purposes
             * LinearProgress bar code taken from lecture and applieds and modified accordingly
             */
            nutrientList.forEach { nutrient ->
                val scoreStr = getNutrientScore(context, userId, nutrient)
                // Debug: printing the fetched score
                //println("Nutrient: $nutrient, userID, scoreStr: $scoreStr")
                val rawScore = scoreStr.toFloatOrNull() ?: 0f
                //println("rawscore: $rawScore")
                val (displayScore, fraction) = if (nutrient.lowercase() == "water" || nutrient.lowercase() == "alcohol") {
                    val ds = rawScore
                    ds to (ds / 5f)   // by doing this we are dividing the rawscore for water and alcogol by 5f
                } else {
                    rawScore to (rawScore / 10f)  // fraction = rawScore/10 (0f..1f)
                }

                // Displaying nutrient label on left and score on right
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    //got this idea from chat GPT for the first character to be upper code
                    Text(
                        text = nutrient.replaceFirstChar { it.uppercaseChar() },
                        style = MaterialTheme.typography.bodyLarge,
                        fontSize = 14.sp
                    )
                    Column (modifier = Modifier
                        .weight(1f)
                        .padding(5.dp),
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.Center){
                        LinearProgressIndicator(
                            progress = fraction,
                            modifier = Modifier
                                .width(150.dp)
                                .height(12.dp)
                                .padding(vertical = 2.dp),
                            color = progressColor,
                            trackColor = trackColor
                        )}
                    Box(modifier = Modifier.width(50.dp),contentAlignment = Alignment.CenterStart) {
                        Text(
                            text = if (nutrient.lowercase() == "water" || nutrient.lowercase() == "alcohol" ||nutrient.lowercase() == "unsaturated fats" ||nutrient.lowercase() == "saturated fats")
                                "$displayScore/5"
                            else
                                "$displayScore/10",
                            modifier = Modifier.align(Alignment.CenterStart),
                            style = MaterialTheme.typography.bodyMedium,
                            fontSize = 12.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            /**
             * 
             * Displaying Total Food Score
             * 
             */
            Spacer(modifier = Modifier.height(16.dp))
            val totalScoreStr = getUserHIEFAScore(context, userId)
            val totalScore = totalScoreStr?.toFloatOrNull() ?: 0f
            val totalFraction = totalScore / 100f

            Text("Total Food Quality Score", style = MaterialTheme.typography.bodyLarge, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LinearProgressIndicator(
                    progress = totalFraction,
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp),
                    color = progressColor,
                    trackColor = trackColor
                )
                Text("${totalScore.toInt()}/100", style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(modifier = Modifier.height(8.dp))
            /**
             * SHARE WITH SOMEONE BUTTON
             *
             *
             */
            var shareText by remember { mutableStateOf("") }
            //Navigation Code Taken from Applieds and Lectures and modified accordingly also made use of the Andorid Developer Documentation
            Button(
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC1FF72)),
                shape = RoundedCornerShape(12.dp),
                onClick = {
                    shareText = "Hey my Total Food Quality Score is ${totalScore.toString()}"
                    val shareIntent = Intent(ACTION_SEND)
                    shareIntent.type = "text/plain"
                    shareIntent.putExtra(Intent.EXTRA_TEXT, shareText)
                    context.startActivity(Intent.createChooser(shareIntent, "Share via"))

                }
            ){
                Row {
                    Icon(Icons.Filled.Share, contentDescription = "Share", tint = Color.Black)
                    Spacer(modifier = Modifier.width(2.dp))
                Text("Share With Someone", fontSize = 15.sp, color = Color.Black)
            }}
            Button(
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC1FF72)),
                shape = RoundedCornerShape(12.dp),
                onClick ={navController.navigate("Nutricoach")}){
                Text("Improve My Diet",fontSize = 15.sp, color = Color.Black)
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
//following code is taken from the lecture/Applied and modified accordingly
@Composable
fun MyNavHost(innerpadding: PaddingValues, navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "Home" // Fixed startDestination to "Home"
    ) {
        composable("Home") {
            HomePage(navController = navController)
        }
        composable("Insights") {
            Insights(navController = navController)
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
//the following code base is taken from the Week 4 Lecture/Applied and Modified to Align accordingly to the Requirements
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
