package com.fit2081.nutritrack

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.fit2081.nutritrack.AuthViewModel
import com.fit2081.nutritrack.LoginScreen
import com.fit2081.nutritrack.QuestionnaireScreen
import com.fit2081.nutritrack.QuestionnaireViewModel.QuestionnaireViewModel
import com.fit2081.nutritrack.data.AppDatabase
import com.fit2081.nutritrack.data.DAO.PatientDAO
import com.fit2081.nutritrack.data.DAO.PatientHealthRecordsDAO
import com.fit2081.nutritrack.data.Entity.Patient
import com.fit2081.nutritrack.data.Entity.PatientHealthRecords
import com.fit2081.nutritrack.data.Repo.AuthRepository
import com.fit2081.nutritrack.data.Repo.IntakeRepository
import com.fit2081.nutritrack.navigation.Screen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize database and repositories
        val db = AppDatabase.getDatabase(this)
        seedDatabaseOnce(db)
        val authRepo = AuthRepository(db.patientDAO())
        val intakeRepo = IntakeRepository(db.foodIntakeDAO())

        // Manual DI of ViewModels
        val authViewModel = AuthViewModel(authRepo)
        val questionnaireViewModel = QuestionnaireViewModel(intakeRepo)

        setContent {
            MaterialTheme {
                val navController = rememberNavController()
                Scaffold(modifier = Modifier.fillMaxSize()) {
                    MainNavHost(
                        navController = navController,
                        authViewModel = authViewModel,
                        questionnaireViewModel = questionnaireViewModel
                    )
                }
            }
        }
    }

        /**
         * Seeds Patient and PatientHealthRecords tables if empty by reading accounts.csv from assets
         */
        private fun seedDatabaseOnce(db: AppDatabase) {
            lifecycleScope.launch(Dispatchers.IO) {
                val patientDao = db.patientDAO()
                val recordDao = db.patientHealthRecordsDAO()

                val count = patientDao.getAllUserIds()
                if (count.isEmpty()) {
                    // Read CSV and insert
                    assets.open("accounts.csv").bufferedReader().useLines { lines ->
                        lines.drop(1).forEach { line ->
                            val cols = line.split(",").map { it.trim() }
                            if (cols.size >= 3) {
                                val phone = cols[0]
                                val userId = cols[1]
                                val sex = cols[2]
                                // Insert Patient
                                patientDao.insert(
                                    com.fit2081.nutritrack.data.Entity.Patient(
                                        userId = userId,
                                        phoneNumber = phone,
                                        password = "",
                                        name = "",
                                        sex = sex,
                                        heifaScore = 0
                                    )
                                )
                                // Insert health record with default values or parse further cols as needed
                                recordDao.insert(
                                    com.fit2081.nutritrack.data.Entity.PatientHealthRecords(
                                        userId = userId,
                                        sex = sex,
                                        heifaTotalScoreMale = cols.getOrNull(3)?.toFloatOrNull()
                                            ?: 0f,
                                        heifaTotalScoreFemale = cols.getOrNull(4)?.toFloatOrNull()
                                            ?: 0f,
                                        discretionaryHeifaScoreMale = cols.getOrNull(5)
                                            ?.toFloatOrNull() ?: 0f,
                                        discretionaryHeifaScoreFemale = cols.getOrNull(6)
                                            ?.toFloatOrNull() ?: 0f,
                                        discretionaryServeSize = cols.getOrNull(7)?.toFloatOrNull()
                                            ?: 0f,
                                        vegetablesHeifaScoreMale = cols.getOrNull(8)
                                            ?.toFloatOrNull() ?: 0f,
                                        vegetablesHeifaScoreFemale = cols.getOrNull(9)
                                            ?.toFloatOrNull() ?: 0f,
                                        vegetablesWithLegumesAllocatedServeSize = cols.getOrNull(10)
                                            ?.toFloatOrNull() ?: 0f,
                                        legumesAllocatedVegetables = cols.getOrNull(11)
                                            ?.toFloatOrNull() ?: 0f,
                                        vegetablesVariationsScore = cols.getOrNull(12)
                                            ?.toFloatOrNull() ?: 0f,
                                        vegetablesCruciferous = cols.getOrNull(13)?.toFloatOrNull()
                                            ?: 0f,
                                        vegetablesTuberAndBulb = cols.getOrNull(14)?.toFloatOrNull()
                                            ?: 0f,
                                        vegetablesOther = cols.getOrNull(15)?.toFloatOrNull() ?: 0f,
                                        legumes = cols.getOrNull(16)?.toFloatOrNull() ?: 0f,
                                        vegetablesGreen = cols.getOrNull(17)?.toFloatOrNull() ?: 0f,
                                        vegetablesRedAndOrange = cols.getOrNull(18)?.toFloatOrNull()
                                            ?: 0f,
                                        fruitHeifaScoreMale = cols.getOrNull(19)?.toFloatOrNull()
                                            ?: 0f,
                                        fruitHeifaScoreFemale = cols.getOrNull(20)?.toFloatOrNull()
                                            ?: 0f,
                                        fruitServeSize = cols.getOrNull(21)?.toFloatOrNull() ?: 0f,
                                        fruitVariationsScore = cols.getOrNull(22)?.toFloatOrNull()
                                            ?: 0f,
                                        fruitPome = cols.getOrNull(23)?.toFloatOrNull() ?: 0f,
                                        fruitTropicalAndSubtropical = cols.getOrNull(24)
                                            ?.toFloatOrNull() ?: 0f,
                                        fruitBerry = cols.getOrNull(25)?.toFloatOrNull() ?: 0f,
                                        fruitStone = cols.getOrNull(26)?.toFloatOrNull() ?: 0f,
                                        fruitCitrus = cols.getOrNull(27)?.toFloatOrNull() ?: 0f,
                                        fruitOther = cols.getOrNull(28)?.toFloatOrNull() ?: 0f,
                                        grainsAndCerealsHeifaScoreMale = cols.getOrNull(29)
                                            ?.toFloatOrNull() ?: 0f,
                                        grainsAndCerealsHeifaScoreFemale = cols.getOrNull(30)
                                            ?.toFloatOrNull() ?: 0f,
                                        grainsAndCerealsServeSize = cols.getOrNull(31)
                                            ?.toFloatOrNull() ?: 0f,
                                        grainsAndCerealsNonWholeGrains = cols.getOrNull(32)
                                            ?.toFloatOrNull() ?: 0f,
                                        wholeGrainsHeifaScoreMale = cols.getOrNull(33)
                                            ?.toFloatOrNull() ?: 0f,
                                        wholeGrainsHeifaScoreFemale = cols.getOrNull(34)
                                            ?.toFloatOrNull() ?: 0f,
                                        wholeGrainsServeSize = cols.getOrNull(35)?.toFloatOrNull()
                                            ?: 0f,
                                        meatAndAlternativesHeifaScoreMale = cols.getOrNull(36)
                                            ?.toFloatOrNull() ?: 0f,
                                        meatAndAlternativesHeifaScoreFemale = cols.getOrNull(37)
                                            ?.toFloatOrNull() ?: 0f,
                                        meatAndAlternativesWithLegumesAllocatedServeSize = cols.getOrNull(
                                            38
                                        )?.toFloatOrNull() ?: 0f,
                                        legumesAllocatedMeatAndAlternatives = cols.getOrNull(39)
                                            ?.toFloatOrNull() ?: 0f,
                                        dairyAndAlternativesHeifaScoreMale = cols.getOrNull(40)
                                            ?.toFloatOrNull() ?: 0f,
                                        dairyAndAlternativesHeifaScoreFemale = cols.getOrNull(41)
                                            ?.toFloatOrNull() ?: 0f,
                                        dairyAndAlternativesServeSize = cols.getOrNull(42)
                                            ?.toFloatOrNull() ?: 0f,
                                        sodiumHeifaScoreMale = cols.getOrNull(43)?.toFloatOrNull()
                                            ?: 0f,
                                        sodiumHeifaScoreFemale = cols.getOrNull(44)?.toFloatOrNull()
                                            ?: 0f,
                                        sodiumMgMilligrams = cols.getOrNull(45)?.toFloatOrNull()
                                            ?: 0f,
                                        alcoholHeifaScoreMale = cols.getOrNull(46)?.toFloatOrNull()
                                            ?: 0f,
                                        alcoholHeifaScoreFemale = cols.getOrNull(47)
                                            ?.toFloatOrNull() ?: 0f,
                                        alcoholStandardDrinks = cols.getOrNull(48)?.toFloatOrNull()
                                            ?: 0f,
                                        waterHeifaScoreMale = cols.getOrNull(49)?.toFloatOrNull()
                                            ?: 0f,
                                        waterHeifaScoreFemale = cols.getOrNull(50)?.toFloatOrNull()
                                            ?: 0f,
                                        water = cols.getOrNull(51)?.toFloatOrNull() ?: 0f,
                                        waterTotalMl = cols.getOrNull(52)?.toFloatOrNull() ?: 0f,
                                        beverageTotalMl = cols.getOrNull(53)?.toFloatOrNull() ?: 0f,
                                        sugarHeifaScoreMale = cols.getOrNull(54)?.toFloatOrNull()
                                            ?: 0f,
                                        sugarHeifaScoreFemale = cols.getOrNull(55)?.toFloatOrNull()
                                            ?: 0f,
                                        sugar = cols.getOrNull(56)?.toFloatOrNull() ?: 0f,
                                        saturatedFatHeifaScoreMale = cols.getOrNull(57)
                                            ?.toFloatOrNull() ?: 0f,
                                        saturatedFatHeifaScoreFemale = cols.getOrNull(58)
                                            ?.toFloatOrNull() ?: 0f,
                                        saturatedFat = cols.getOrNull(59)?.toFloatOrNull() ?: 0f,
                                        unsaturatedFatHeifaScoreMale = cols.getOrNull(60)
                                            ?.toFloatOrNull() ?: 0f,
                                        unsaturatedFatHeifaScoreFemale = cols.getOrNull(61)
                                            ?.toFloatOrNull() ?: 0f,
                                        unsaturatedFatServeSize = cols.getOrNull(62)
                                            ?.toFloatOrNull() ?: 0f
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }

@SuppressLint("ContextCastToActivity")
@Composable
fun MainNavHost(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    questionnaireViewModel: QuestionnaireViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Welcome.route
    ) {
        composable(Screen.Welcome.route) {
            val context = LocalContext.current as MainActivity
            WelcomeScreen(navController = navController, context = context)
        }
        composable(Screen.Login.route) {
            LoginScreen(vm = authViewModel) { userId ->
                val dest = if (isQuestionnaireDone(userId))
                    Screen.Dashboard.createRoute(userId)
                else
                    Screen.Questionnaire.createRoute(userId)
                navController.navigate(dest) {
                    popUpTo(Screen.Welcome.route) { inclusive = true }
                }
            }
        }
        composable(Screen.Questionnaire.route) { backStack ->
            val userId = backStack.arguments?.getString("userId") ?: return@composable
            QuestionnaireScreen(
                patientId = userId,
                navController = navController,
                vm = questionnaireViewModel
            )
        }
        composable(Screen.Dashboard.route) { backStack ->
            val userId = backStack.arguments?.getString("userId") ?: return@composable
            //DashboardScreen(
                //  userId = userId,
                //onCoachClick = { navController.navigate(Screen.Coach.createRoute(userId)) },
                //onSettingsClick = { navController.navigate(Screen.Settings.createRoute(userId)) })
        }
        composable(Screen.Coach.route) { backStack ->
            val userId = backStack.arguments?.getString("userId") ?: return@composable
            //CoachScreen(userId)
        }
        composable(Screen.Settings.route) { backStack ->
            val userId = backStack.arguments?.getString("userId") ?: return@composable
            //SettingsScreen(userId)
        }
    }
}

@Composable
fun WelcomeScreen(
    navController: NavHostController,
    context: Context,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color(0xFFC1FF72)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.main_logo),
                contentDescription = "Main logo",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.5f),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "This app provides general health and nutrition information for educational purposes only. It is not intended as " +
                        "medical advice, diagnosis, or treatment. Always consult a qualified healthcare professional " +
                        "before making any changes to your diet, exercise, or health regimen. " +
                        "Use this app at your own risk. " +
                        "If you’d like to an Accredited Practicing Dietitian (APD), please visit:",
                color = Color.Black,
                textAlign = TextAlign.Center,
                fontSize = 12.sp
            )
            Text(
                text = "https://www.monash.edu/medicine/scs/nu",
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(40.dp))
            Button(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF8F5F5)),
                onClick = { navController.navigate(Screen.Login.route) }
            ) {
                Text("Login", color = Color.Black)
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Designed By Bhowmik Dham (34337229)",
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                color = Color.Black
            )
        }
    }
}

fun isQuestionnaireDone(userId: String): Boolean {
    // TODO: read shared preferences or DB flag
    return false
}
