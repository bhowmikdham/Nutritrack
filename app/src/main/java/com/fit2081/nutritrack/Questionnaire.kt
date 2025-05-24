package com.fit2081.nutritrack

import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.fit2081.nutritrack.R
import com.fit2081.nutritrack.QuestionnaireViewModel.FoodintakeState
import com.fit2081.nutritrack.QuestionnaireViewModel.QuestionnaireViewModel
import com.fit2081.nutritrack.data.AppDatabase
import com.fit2081.nutritrack.data.Repo.IntakeRepository
import com.fit2081.nutritrack.navigation.Screen
import java.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.fit2081.nutritrack.data.AuthManager.currentUserId
import com.fit2081.nutritrack.data.AuthManager


/**
 * Activity hosting the Questionnaire flow.
 */
class Questionnaire : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Manual DI: database -> repo -> viewModel
        val db = AppDatabase.getDatabase(this)
        val intakeRepo = IntakeRepository(db.foodIntakeDAO())
        val vm = QuestionnaireViewModel(intakeRepo)

        // ← instead of intent extra, read from AuthManager
        val patientId = currentUserId() ?: run {
            startActivity(Intent(this, Login::class.java))
            finish()
            return
        }

        setContent {
            MaterialTheme {
                val navController = rememberNavController()
                QuestionnaireScreen(
                    patientId = patientId,
                    navController = navController,
                    vm = vm,
                    onComplete = TODO()
                )
            }
        }
    }
}

    /**
 * Main UI for the questionnaire: food choices, persona, and timings.
 */
@Composable
fun QuestionnaireScreen(
    patientId: String,
    navController: NavHostController,
    vm: QuestionnaireViewModel,
    onComplete:() -> Unit
) {
        val context = LocalContext.current
        LaunchedEffect(patientId) { vm.loadResponse(patientId) }
        val state by vm.state.collectAsState()

        var showCancel by remember { mutableStateOf(false) }
        var showInfo by remember { mutableStateOf<Pair<String, Boolean>>("" to false) }
        var showSummary by remember { mutableStateOf(false) }

        // Discard confirmation dialog
        if (showCancel) {
            AlertDialog(
                onDismissRequest = { showCancel = false },
                title = { Text("Discard?", fontWeight = FontWeight.Bold) },
                text = { Text("Lose all changes?") },
                confirmButton = {
                    TextButton(onClick = {
                        showCancel = false
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Welcome.route) { inclusive = true }
                        }
                    }) {
                        Text("Yes")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCancel = false }) {
                        Text("No")
                    }
                }
            )
        }

        // Summary & submit dialog
        if (showSummary) {
            AlertDialog(
                onDismissRequest = { showSummary = false },
                title = { Text("Review Your Entries", fontWeight = FontWeight.Bold) },
                text = { SummaryContent(state) },
                confirmButton = {
                    TextButton(onClick = {
                        showSummary = false
                        vm.saveResponse(patientId)
                        onComplete() // Call the completion callback
                    }) {
                        Text("Confirm")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSummary = false }) {
                        Text("Edit")
                    }
                }
            )
        }
    // Questionnaire form
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item { HeaderRow(onCancel = { showCancel = true }) }
        item { FoodGrid(state, vm::onFoodToggle) }
        item { PersonaButtons(state.persona, onInfo = { showInfo = it to true }, onSelect = vm::onPersonaChange) }
        item { TimePickerSection(vm, state) }
        item {
            Button(
                onClick = {
                    if (!isFormValid(state)) Toast.makeText(context, "Complete all fields", Toast.LENGTH_SHORT).show()
                    else showSummary = true
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF137A44))
            ) {
                Text("Review & Continue", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ------------------ Helpers & Sub-composables ------------------
@Composable
private fun HeaderRow(onCancel: () -> Unit) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.ArrowBack, contentDescription = "Back", Modifier.clickable(onClick = onCancel))
        Text("Questionnaire", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(24.dp))
    }
}

@Composable
private fun FoodGrid(state: FoodintakeState, onToggle: (String, Boolean) -> Unit) {
    Column {
        Text("Select foods you can eat:", fontWeight = FontWeight.Bold)
        listOf(
            "fruits", "vegetables", "grains",
            "redMeat", "seafood", "poultry",
            "fish", "eggs", "nutsSeeds"
        ).chunked(3).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                row.forEach { key ->
                    val label = key.replaceFirstChar { it.uppercase() }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Checkbox(
                            checked = when (key) {
                                "fruits" -> state.fruits
                                "vegetables" -> state.vegetables
                                "grains" -> state.grains
                                "redMeat" -> state.redMeat
                                "seafood" -> state.seafood
                                "poultry" -> state.poultry
                                "fish" -> state.fish
                                "eggs" -> state.eggs
                                "nutsSeeds" -> state.nutsSeeds
                                else -> false
                            },
                            onCheckedChange = { onToggle(key, it) }
                        )
                        Text(label, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun PersonaButtons(
    selected: String,
    onInfo: (String) -> Unit,
    onSelect: (String) -> Unit
) {
    Text("Your Persona:", fontWeight = FontWeight.Bold)
    personaDescriptions.keys.chunked(3).forEach { row ->
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            row.forEach { p ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(id = personaImages[p]!!),
                        contentDescription = p,
                        modifier = Modifier
                            .size(48.dp)
                            .clickable { onInfo(p) }
                    )
                    RadioButton(selected = selected == p, onClick = { onSelect(p) })
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun TimePickerSection(vm: QuestionnaireViewModel, state: FoodintakeState) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    fun showPicker(field: String, initial: String) {
        val parts = initial.split(':')
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: calendar.get(Calendar.HOUR_OF_DAY)
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: calendar.get(Calendar.MINUTE)
        TimePickerDialog(context, { _, h, m -> vm.onTimeChange(field, "%02d:%02d".format(h, m)) }, hour, minute, true).show()
    }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        listOf(
            "biggest_meal_time" to state.biggestMealTime,
            "sleep_time" to state.sleepTime,
            "wake_time" to state.wakeTime
        ).forEach { (field, value) ->
            OutlinedButton(
                onClick = { showPicker(field, value) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.DateRange, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                val label = when(field) {
                    "biggest_meal_time" -> "Select Biggest Meal Time"
                    "sleep_time" -> "Select Sleep Time"
                    else -> "Select Wake Time"
                }
                Text(if (value.isBlank()) label else value)
            }
        }
    }
}

@Composable
private fun SummaryContent(state: FoodintakeState) {
    Column {
        Text("Categories:", fontWeight = FontWeight.Bold)
        listOf(
            "Fruits" to state.fruits,
            "Vegetables" to state.vegetables,
            "Grains" to state.grains,
            "Red Meat" to state.redMeat,
            "Seafood" to state.seafood,
            "Poultry" to state.poultry,
            "Fish" to state.fish,
            "Eggs" to state.eggs,
            "Nuts & Seeds" to state.nutsSeeds
        ).forEach { (label, sel) -> if (sel) Text(label) }
        Spacer(Modifier.height(8.dp))
        Text("Persona:", fontWeight = FontWeight.Bold); Text(state.persona)
        Spacer(Modifier.height(8.dp))
        Text("Timings:", fontWeight = FontWeight.Bold)
        Text("Biggest Meal: ${state.biggestMealTime}")
        Text("Sleep: ${state.sleepTime}")
        Text("Wake: ${state.wakeTime}")
    }
}

private fun isFormValid(state: FoodintakeState): Boolean {
    val anyFood = listOf(
        state.fruits, state.vegetables, state.grains,
        state.redMeat, state.seafood, state.poultry,
        state.fish, state.eggs, state.nutsSeeds
    ).any { it }
    return anyFood && state.persona.isNotEmpty() &&
            state.biggestMealTime.isNotEmpty() && state.sleepTime.isNotEmpty() && state.wakeTime.isNotEmpty()
}

private val personaDescriptions = mapOf(
    "Health Devotee" to "I’m passionate about healthy eating…",
    "Mindful Eater" to "Mindful eating helps you focus…",
    "Wellness Striver" to "I aspire to be healthy…",
    "Balanced Seeker" to "I try to live balanced…",
    "Health Procrastinator" to "I’m contemplating healthy eating…",
    "Food Carefree" to "I’m not bothered about healthy eating…"
)

private val personaImages = mapOf(
    "Health Devotee" to R.drawable.persona_1,
    "Mindful Eater" to R.drawable.persona_2,
    "Wellness Striver" to R.drawable.persona_3,
    "Balanced Seeker" to R.drawable.persona_4,
    "Health Procrastinator" to R.drawable.persona_5,
    "Food Carefree" to R.drawable.persona_6
)
