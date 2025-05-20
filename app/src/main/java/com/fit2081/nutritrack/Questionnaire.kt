package com.fit2081.nutritrack.Questionnaire

import android.app.TimePickerDialog
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.fit2081.nutritrack.R
import com.fit2081.nutritrack.Questionnaire.QuestionnaireActivity
import com.fit2081.nutritrack.Questionnaire.QuestionnaireScreen
import com.fit2081.nutritrack.QuestionnaireViewModel.FoodintakeState
import com.fit2081.nutritrack.QuestionnaireViewModel.QuestionnaireViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class QuestionnaireActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Hosted by Compose NavGraph
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Time Picker Helper
// ---------------------------------------------------------------------------------------------

@Composable
private fun TimePickerSection(
    vm: QuestionnaireViewModel,
    state: FoodintakeState
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    fun showPicker(field: String, initial: String) {
        val parts = initial.split(':')
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: calendar.get(Calendar.HOUR_OF_DAY)
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: calendar.get(Calendar.MINUTE)
        TimePickerDialog(context, { _, h, m ->
            vm.onTimeChange(field, String.format("%02d:%02d", h, m))
        }, hour, minute, true).show()
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedButton(
            onClick = { showPicker("biggest_meal_time", state.biggestMealTime) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(Icons.Default.DateRange, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(if (state.biggestMealTime.isBlank()) "Select Biggest Meal Time" else state.biggestMealTime)
        }
        OutlinedButton(
            onClick = { showPicker("sleep_time", state.sleepTime) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(Icons.Default.DateRange, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(if (state.sleepTime.isBlank()) "Select Sleep Time" else state.sleepTime)
        }
        OutlinedButton(
            onClick = { showPicker("wake_time", state.wakeTime) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(Icons.Default.DateRange, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(if (state.wakeTime.isBlank()) "Select Wake Time" else state.wakeTime)
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Main QuestionnaireScreen
// ---------------------------------------------------------------------------------------------


@Composable
fun QuestionnaireScreen(
    patientId: String,
    navController: NavHostController,
    vm: QuestionnaireViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    LaunchedEffect(patientId) { vm.loadResponse(patientId) }
    val state by vm.state.collectAsState()
    var showCancel by remember { mutableStateOf(false) }
    var showInfo by remember { mutableStateOf<Pair<String, Boolean>>("" to false) }
    var showSummary by remember { mutableStateOf(false) }

    // Cancel dialog
    if (showCancel) {
        AlertDialog(
            onDismissRequest = { showCancel = false },
            title = { Text("Discard?", fontWeight = FontWeight.Bold) },
            text = { Text("Lose all changes?") },
            confirmButton = { TextButton(onClick = { showCancel = false; navController.popBackStack() }) { Text("Yes") } },
            dismissButton = { TextButton(onClick = { showCancel = false }) { Text("No") } }
        )
    }

    // Persona info dialog
    if (showInfo.second) {
        val (persona, _) = showInfo
        val imgRes = personaImages[persona] ?: 0
        val desc = personaDescriptions[persona] ?: ""
        AlertDialog(
            onDismissRequest = { showInfo = persona to false },
            title = { Text(persona, fontWeight = FontWeight.Bold) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(id = imgRes),
                        contentDescription = persona,
                        modifier = Modifier.height(160.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(desc, textAlign = TextAlign.Center)
                }
            },
            confirmButton = { TextButton(onClick = { showInfo = persona to false }) { Text("OK") } }
        )
    }

    // Summary dialog
    if (showSummary) {
        AlertDialog(
            onDismissRequest = { showSummary = false },
            title = { Text("Review Your Entries", fontWeight = FontWeight.Bold) },
            text = { SummaryContent(state) },
            confirmButton = { TextButton(onClick = { showSummary = false; vm.saveResponse(patientId) }) { Text("Confirm") } },
            dismissButton = { TextButton(onClick = { showSummary = false }) { Text("Edit") } }
        )
    }

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

// ---------------------------------------------------------------------------------------------
// Helper Composables and Data
// ---------------------------------------------------------------------------------------------

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

@Composable
private fun HeaderRow(onCancel: () -> Unit) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.ArrowBack, contentDescription = "Back", modifier = Modifier.clickable(onClick = onCancel))
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
private fun PersonaButtons(selected: String, onInfo: (String) -> Unit, onSelect: (String) -> Unit) {
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
        Text("Persona:", fontWeight = FontWeight.Bold)
        Text(state.persona)
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
