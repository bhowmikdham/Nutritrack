// Updated InsightsScreen with LazyColumn and aligned progress bars
package com.fit2081.nutritrack

import android.content.Intent
import android.content.Intent.ACTION_SEND
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.navigation.NavHostController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fit2081.nutritrack.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    navController: NavHostController,
    viewModel: InsightsViewModel = viewModel(
        factory = InsightsViewModelFactory(LocalContext.current)
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Handle not logged in error
    uiState.error?.let {
        LaunchedEffect(Unit) {
            navController.navigate("login") { popUpTo(0) }
        }
        return
    }

    // Show loading
    if (uiState.isLoading) {
        Box(
            Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    // Display scores using LazyColumn
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Insights", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp) },
                colors = centerAlignedTopAppBarColors(containerColor = Color(0xFFC1FF72))
            )
        },
        containerColor = Color.White
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Title
            item {
                Text(
                    "Your Nutrient Scores",
                    fontWeight = FontWeight.Bold,
                    fontSize = 30.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // Nutrient scores
            items(uiState.nutrients) { nutrient ->
                NutrientScoreRow(nutrient)
            }

            // Total score section
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Total Food Quality Score",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LinearProgressIndicator(
                        progress = uiState.totalScore / 100f,
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp),
                        color = Color(0xFF137A44),
                        trackColor = Color(0xFFD7E6D6)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        "${uiState.totalScore}/100",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Buttons section
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Share button
                    Button(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC1FF72)),
                        onClick = {
                            val shareIntent = Intent(ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, "My Food Quality Score is ${uiState.totalScore}/100")
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share via"))
                        }
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = "Share")
                        Spacer(Modifier.width(4.dp))
                        Text("Share")
                    }

                    // Improve diet button
                    Button(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC1FF72)),
                        onClick = { navController.navigate("NutriCoach") }
                    ) {
                        Text("Improve My Diet")
                    }
                }
            }

            // Add some bottom padding
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun NutrientScoreRow(nutrient: NutrientUiModel) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Label (fixed width for alignment)
        Text(
            text = nutrient.label,
            fontSize = 14.sp,
            modifier = Modifier.width(140.dp)
        )

        // Progress bar and score (aligned to the right)
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LinearProgressIndicator(
                progress = nutrient.score / nutrient.maxScore,
                modifier = Modifier
                    .width(150.dp)
                    .height(12.dp),
                color = getColorForScore(nutrient.score, nutrient.maxScore),
                trackColor = Color(0xFFD7E6D6)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${nutrient.score.toInt()}/${nutrient.maxScore.toInt()}",
                fontSize = 12.sp,
                modifier = Modifier.width(35.dp),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun getColorForScore(score: Float, maxScore: Float): Color {
    val percentage = (score / maxScore) * 100
    return when {
        percentage >= 80 -> Color(0xFF137A44) // Green
        percentage >= 50 -> Color(0xFFFFDE59) // Yellow
        else -> Color(0xFFFF5757) // Red
    }
}