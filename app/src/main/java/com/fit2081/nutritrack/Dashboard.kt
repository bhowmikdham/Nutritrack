// Dashboard.kt
package com.fit2081.nutritrack

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.navigation.NavHostController
import com.fit2081.nutritrack.data.AppDatabase
import com.fit2081.nutritrack.data.Repo.AuthRepository
import com.fit2081.nutritrack.data.AuthManager
import com.fit2081.nutritrack.navigation.Screen
import kotlinx.coroutines.flow.filterNotNull

@Composable
fun HomePage(navController: NavHostController) {
    val context = LocalContext.current

    // 1) Manual DI: DAOs and Repos
    val db       = remember { AppDatabase.getDatabase(context) }
    val phrDao   = remember { db.patientHealthRecordsDAO() }
    val authRepo = remember { AuthRepository(db.patientDAO()) }

    // 2) Get current user
    val userId = AuthManager.currentUserId() ?: return

    // 3) Stream the list of records, then pull out the first element
    val recordList by phrDao
        .getByUserId(userId)            // Flow<List<PatientHealthRecords?>>
        .filterNotNull()                // Flow<List<PatientHealthRecords>>
        .collectAsState(initial = emptyList())

    val rec = recordList.firstOrNull() ?: return

    // 4) Compute the total score based on sex
    val totalScore = if (rec.sex.equals("Male", ignoreCase = true))
        rec.heifaTotalScoreMale.toInt()
    else
        rec.heifaTotalScoreFemale.toInt()

    var userName by remember { mutableStateOf("") }
    LaunchedEffect(userId) {
        userName = authRepo.getUsername(userId)
    }

    // 6) UI
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Hello,", fontSize = 25.sp, fontWeight = FontWeight.Medium, color = Color.Gray)
        Text(userName, fontSize = 40.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(8.dp))
        Text("Here’s your latest Food Quality Score")
        Spacer(Modifier.height(8.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Button(
                shape  = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC1FF72)),
                onClick = {
                    navController.navigate(Screen.Questionnaire.createRoute(userId))
                }
            ) {
                Text("Edit Details", color = Color.Black)
            }
        }

        Spacer(Modifier.height(16.dp))
        Image(
            painter = painterResource(R.drawable.plate),
            contentDescription = "plate",
            modifier = Modifier
                .size(330.dp),
            alignment = Alignment.Center,
            contentScale= ContentScale.FillWidth,

        )
        Text("My Score", fontSize = 30.sp, fontWeight = FontWeight.Bold)
        Row(
            verticalAlignment= Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier= Modifier.fillMaxWidth()
        ) {
            val arrowRes = when {
                totalScore >= 80 -> R.drawable.greenarrow
                totalScore >= 50 -> R.drawable.yellowdash
                else             -> R.drawable.redcross
            }
            Image(
                painter           = painterResource(arrowRes),
                contentDescription = null,
                modifier          = Modifier.size(64.dp)
            )
            Text(
                "Your Food Quality Score",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold)
            Text(
                "$totalScore/100",
                fontSize   = 30.sp,
                fontWeight = FontWeight.Bold,
                color      = when {
                    totalScore >= 80 -> Color.Green
                    totalScore >= 50 -> Color(0xFFFFDE59)
                    else             -> Color(0xFFFF5757)
                }
            )

        }
        Text("What is The Food Quality Score?", fontSize = 15.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(
            "Your Food Quality Score provides a snapshot of how well your eating patterns align with established guidelines.",
            fontSize   = 14.sp,
            lineHeight = 20.sp
        )
    }
}
