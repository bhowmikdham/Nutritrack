package com.fit2081.nutritrack

import android.content.Intent
import android.content.Intent.ACTION_SEND
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.TopAppBarDefaults.centerAlignedTopAppBarColors
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavHostController
import com.fit2081.nutritrack.data.AppDatabase
import com.fit2081.nutritrack.data.Repo.HealthRecordsRepository
import com.fit2081.nutritrack.data.AuthManager
import com.fit2081.nutritrack.data.AuthManager.currentUserId
import kotlinx.coroutines.flow.filterNotNull

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(navController: NavHostController) {
    val context = LocalContext.current

    // 1) Manual DI: get your HealthRecordsRepository
    val db         = remember { AppDatabase.getDatabase(context) }
    val healthRepo = remember { HealthRecordsRepository(db.patientHealthRecordsDAO()) }

    // 2) Fetch current userId
    val userId = AuthManager.currentUserId()
    if (userId == null) {
        // No one logged in, navigate back to login
        LaunchedEffect(Unit) {
            navController.navigate("login") { popUpTo(0) }
        }
        return
    }

    // 3) Collect the full health record (scores + sex)
    val record by healthRepo
        .recordFor(userId)
        .filterNotNull()
        .collectAsState(initial = null)

    // 4) Show loading while we wait for data
    if (record == null) {
        Box(
            Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    // 5) Once loaded, destructure
    val rec = record!!
    val sex = rec.sex

    // 6) Build the list of (label, score, max) tuples
    val nutrients = listOf(
        Triple("Vegetables",           if (sex == "Male") rec.vegetablesHeifaScoreMale   else rec.vegetablesHeifaScoreFemale,   10f),
        Triple("Fruit",                if (sex == "Male") rec.fruitHeifaScoreMale         else rec.fruitHeifaScoreFemale,         10f),
        Triple("Grains & Cereals",     if (sex == "Male") rec.grainsAndCerealsHeifaScoreMale else rec.grainsAndCerealsHeifaScoreFemale, 10f),
        Triple("Whole Grains",         if (sex == "Male") rec.wholeGrainsHeifaScoreMale    else rec.wholeGrainsHeifaScoreFemale,    10f),
        Triple("Meat & Alternatives",  if (sex == "Male") rec.meatAndAlternativesHeifaScoreMale else rec.meatAndAlternativesHeifaScoreFemale, 10f),
        Triple("Dairy & Alternatives", if (sex == "Male") rec.dairyAndAlternativesHeifaScoreMale else rec.dairyAndAlternativesHeifaScoreFemale, 10f),
        Triple("Sodium",               if (sex == "Male") rec.sodiumHeifaScoreMale       else rec.sodiumHeifaScoreFemale,       10f),
        Triple("Alcohol",              if (sex == "Male") rec.alcoholHeifaScoreMale      else rec.alcoholHeifaScoreFemale,      5f),
        Triple("Water",                if (sex == "Male") rec.waterHeifaScoreMale        else rec.waterHeifaScoreFemale,        5f),
        Triple("Sugar",                if (sex == "Male") rec.sugarHeifaScoreMale        else rec.sugarHeifaScoreFemale,        10f),
        Triple("Saturated Fats",       if (sex == "Male") rec.saturatedFatHeifaScoreMale else rec.saturatedFatHeifaScoreFemale, 10f),
        Triple("Unsaturated Fats",     if (sex == "Male") rec.unsaturatedFatHeifaScoreMale else rec.unsaturatedFatHeifaScoreFemale, 10f),
        Triple("Discretionary Foods",  if (sex == "Male") rec.discretionaryHeifaScoreMale  else rec.discretionaryHeifaScoreFemale,  10f)
    )

    // 7) Total score
    val totalScore = if (sex == "Male") rec.heifaTotalScoreMale.toInt() else rec.heifaTotalScoreFemale.toInt()

    // 8) UI
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Insights",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize   = 20.sp
                    )
                },
                colors = centerAlignedTopAppBarColors(
                    containerColor = Color(0xFFC1FF72)
                )
            )
        },
        containerColor = Color.White
    ) { innerPadding ->
        Column(
            Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Text("Your Nutrient Scores", fontWeight = FontWeight.Bold, fontSize = 30.sp)
            Spacer(Modifier.height(16.dp))

            val progressColor = Color(0xFF137A44)
            val trackColor    = Color(0xFFD7E6D6)

            nutrients.forEach { (label, score, max) ->
                val fraction = score / max
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment   = Alignment.CenterVertically
                ) {
                    Text(label, fontSize = 14.sp)
                    LinearProgressIndicator(
                        progress   = fraction,
                        modifier   = Modifier
                            .width(150.dp)
                            .height(12.dp),
                        color      = progressColor,
                        trackColor = trackColor
                    )
                    Text(
                        "${score.toInt()}/${max.toInt()}",
                        fontSize = 12.sp
                    )
                }
                Spacer(Modifier.height(12.dp))
            }

            Spacer(Modifier.height(16.dp))

            Text("Total Food Quality Score", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment   = Alignment.CenterVertically
            ) {
                LinearProgressIndicator(
                    progress   = totalScore / 100f,
                    modifier   = Modifier
                        .weight(1f)
                        .height(8.dp),
                    color      = progressColor,
                    trackColor = trackColor
                )
                Text("$totalScore/100")
            }

            Spacer(Modifier.height(8.dp))

            // Share button
            var shareText by remember { mutableStateOf("") }
            Button(
                shape  = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC1FF72)),
                onClick = {
                    shareText = "My Food Quality Score is $totalScore/100"
                    val shareIntent = Intent(ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, shareText)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Share via"))
                }
            ) {
                Icon(Icons.Filled.Share, contentDescription = "Share")
                Spacer(Modifier.width(4.dp))
                Text("Share With Someone")
            }

            Spacer(Modifier.height(8.dp))

            Button(
                shape  = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC1FF72)),
                onClick = { navController.navigate("Nutricoach") }
            ) {
                Text("Improve My Diet")
            }
        }
    }
}
