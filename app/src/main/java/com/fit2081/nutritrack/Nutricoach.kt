package com.fit2081.nutritrack

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.fit2081.nutritrack.data.AppDatabase
import com.fit2081.nutritrack.data.AuthManager
import com.fit2081.nutritrack.data.Entity.CoachTips
import com.fit2081.nutritrack.data.Entity.PatientHealthRecords
import com.fit2081.nutritrack.data.Repo.CoachTipsRepository
import com.fit2081.nutritrack.data.Repo.HealthRecordsRepository
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NutricoachScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val userId = AuthManager.currentUserId() ?: return

    // Setup repositories and ViewModels
    val db = remember { AppDatabase.getDatabase(context) }
    val nutriCoachRepo = remember {
        CoachTipsRepository(
            db.coachTipsDAO(),
            healthRecordsDao = db.patientHealthRecordsDAO(),
            foodIntakeDao = db.foodIntakeDAO()
        )
    }
    val healthRepo = remember { HealthRecordsRepository(db.patientHealthRecordsDAO()) }

    val viewModel: NutriCoachViewModel = viewModel(
        factory = NutriCoachViewModelFactory.create(nutriCoachRepo, userId)
    )

    // Get user's health record to check fruit score
    val healthRecord by healthRepo.recordFor(userId)
        .map { it.firstOrNull() }
        .filterNotNull()
        .collectAsState(initial = null)

    val uiState by viewModel.uiState.collectAsState()
    val userTips by viewModel.userTips.collectAsState()

    // Calculate fruit score
    val fruitScore = healthRecord?.let { record ->
        if (record.sex.equals("Male", ignoreCase = true)) {
            record.fruitHeifaScoreMale.toInt()
        } else {
            record.fruitHeifaScoreFemale.toInt()
        }
    } ?: 0

    // Show fruit section only if score is not optimal (< 10)
    val showFruitSection = fruitScore < 10

    // Main LazyColumn for the entire screen
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title
        item {
            Text(
                "NutriCoach",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        // Fruit Section or Optimal Score Image
        item {
            if (showFruitSection) {
                FruitInformationCard(
                    viewModel = viewModel,
                    uiState = uiState
                )
            } else {
                OptimalScoreCard()
            }
        }

        // AI Section
        item {
            AIAdviceCard(
                viewModel = viewModel,
                uiState = uiState,
                fruitScore = fruitScore,
                healthRecord = healthRecord
            )
        }

        // Show All Tips Button
        item {
            Button(
                onClick = { viewModel.toggleShowAllTips() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6200EE)
                ),
                enabled = userTips.isNotEmpty()
            ) {
                Text(
                    text = if (userTips.isEmpty()) {
                        "No Tips Yet"
                    } else {
                        "Show All Tips (${userTips.size})"
                    }
                )
            }
        }
    }

    // All Tips Dialog
    if (uiState.showAllTips && userTips.isNotEmpty()) {
        AllTipsDialog(
            tips = userTips,
            onDismiss = { viewModel.toggleShowAllTips() }
        )
    }
}

@Composable
private fun FruitInformationCard(
    viewModel: NutriCoachViewModel,
    uiState: NutriCoachUiState
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Fruit Information",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Search field
            var searchText by remember(uiState.lastSearchQuery) {
                mutableStateOf(uiState.lastSearchQuery)
            }

            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                label = { Text("Fruit Name") },
                placeholder = { Text("banana") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(
                        onClick = { viewModel.searchFruit(searchText) },
                        enabled = searchText.isNotBlank()
                    ) {
                        Icon(Icons.Default.Search, "Search")
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        if (searchText.isNotBlank()) {
                            viewModel.searchFruit(searchText)
                        }
                    }
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Results in a LazyColumn
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopCenter
            ) {
                when {
                    uiState.isLoadingFruit -> {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(top = 32.dp)
                        )
                    }
                    uiState.fruitError != null -> {
                        Text(
                            text = uiState.fruitError,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                    uiState.currentFruit != null -> {
                        val fruit = uiState.currentFruit
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            item {
                                Text(
                                    text = fruit.name.replaceFirstChar { it.uppercase() },
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }

                            val nutritionData = listOf(
                                "Family" to fruit.family,
                                "Calories" to "${fruit.nutritions.calories}",
                                "Fat" to "${fruit.nutritions.fat} g",
                                "Sugar" to "${fruit.nutritions.sugar} g",
                                "Carbohydrates" to "${fruit.nutritions.carbohydrates} g",
                                "Protein" to "${fruit.nutritions.protein} g"
                            )

                            items(nutritionData) { (label, value) ->
                                NutritionRow(label, value)
                            }
                        }
                    }
                    else -> {
                        Text(
                            text = "Search for a fruit to see its nutritional information",
                            color = Color.Gray,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OptimalScoreCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = "https://picsum.photos/400/300?random=${System.currentTimeMillis()}",
                contentDescription = "Healthy lifestyle",
                modifier = Modifier.fillMaxSize()
            )
            Card(
                modifier = Modifier.padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.9f)
                )
            ) {
                Text(
                    text = "Congratulations! Your fruit intake is optimal!",
                    modifier = Modifier.padding(16.dp),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun AIAdviceCard(
    viewModel: NutriCoachViewModel,
    uiState: NutriCoachUiState,
    fruitScore: Int,
    healthRecord: PatientHealthRecords?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Personalized Dietary Advice (AI)",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Generate button
            Button(
                onClick = {
                    viewModel.generateMotivationalTip(fruitScore, healthRecord.toString())
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6200EE)
                ),
                enabled = !uiState.isLoadingAI
            ) {
                if (uiState.isLoadingAI) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.White
                    )
                } else {
                    Text("Get Personalized Dietary Advice")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Display current tip
            Card(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        uiState.currentAITip.isNotEmpty() -> {
                            Text(
                                text = uiState.currentAITip,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        uiState.aiError != null -> {
                            Text(
                                text = uiState.aiError,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 14.sp
                            )
                        }
                        else -> {
                            Text(
                                text = "Click the button above to get personalized dietary advice based on your complete health profile",
                                color = Color.Gray,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AllTipsDialog(
    tips: List<CoachTips>,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Your Dietary Tips History",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${tips.size} tips",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(tips) { tip ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFF5F5F5)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Text(
                                    text = tip.tip,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Generated: ${formatTimestamp(tip.timestamp)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6200EE)
                    )
                ) {
                    Text("Done")
                }
            }
        }
    }
}

@Composable
private fun NutritionRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = Color.Gray,
            fontSize = 14.sp
        )
        Text(
            text = value,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp
        )
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}