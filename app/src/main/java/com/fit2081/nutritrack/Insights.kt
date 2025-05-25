// Updated InsightsScreen with Card-based UI
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
import androidx.compose.ui.draw.clip
import androidx.navigation.NavHostController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fit2081.nutritrack.navigation.Screen

/**
   Insights Screen - Nutrition Score Display

   Displays user's comprehensive nutrition scores with visual progress indicators
   Provides sharing functionality and navigation to improvement recommendations
   Uses callback pattern for navigation to maintain separation of concerns
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    navController: NavHostController,
    onNavigateToNutricoach: (() -> Unit)? = null,
    viewModel: InsightsViewModel = viewModel(
        factory = InsightsViewModelFactory(LocalContext.current)
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    /**
       Authentication State Handling

       Redirects to login if user is not authenticated
       Ensures secure access to personal nutrition data
     */
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

    // Display scores using LazyColumn with cards
    Scaffold(
        containerColor = Color(0xFFF5F5F5)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Title
            item {
                Text(
                    "Your Nutrient Scores",
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Nutrient score cards
            items(uiState.nutrients) { nutrient ->
                NutrientScoreCard(nutrient)
            }

            /**
               Total Score Summary Card

               Displays overall nutrition score with prominent visual design
               Provides at-a-glance health assessment for the user
               Uses large typography and progress indicators for impact
             */
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFC1FF72).copy(alpha = 0.3f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Total Food Quality Score",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color(0xFF137A44)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "${uiState.totalScore}",
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF137A44)
                            )
                            Text(
                                "/ 100",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF137A44).copy(alpha = 0.7f)
                            )
                        }

                        LinearProgressIndicator(
                            progress = uiState.totalScore / 100f,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .clip(RoundedCornerShape(6.dp)),
                            color = Color(0xFF137A44),
                            trackColor = Color(0xFFD7E6D6)
                        )
                    }
                }
            }

            /**
               Action Buttons Section

               Provides sharing functionality and navigation to nutrition coaching
               Uses callback pattern for navigation to maintain separation of concerns
               Sharing integrates with Android's native sharing capabilities
              */
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    /**
                       Share Button Implementation

                       Creates Android share intent with nutrition score data
                       Allows users to share their progress across platforms
                     */
                    Button(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF137A44)),
                        contentPadding = PaddingValues(vertical = 16.dp),
                        onClick = {
                            val shareIntent = Intent(ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, "My Food Quality Score is ${uiState.totalScore}/100")
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share via"))
                        }
                    ) {
                        Icon(
                            Icons.Filled.Share,
                            contentDescription = "Share",
                            tint = Color.White
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Share", color = Color.White, fontWeight = FontWeight.Medium)
                    }

                    /**
                       Nutrition Coaching Navigation

                       Uses callback pattern to navigate to coaching features
                       Maintains loose coupling between components
                     */
                    Button(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC1FF72)),
                        contentPadding = PaddingValues(vertical = 16.dp),
                        onClick = {
                            onNavigateToNutricoach?.invoke() ?: run {
                                println("No navigation callback provided")
                            }
                        }
                    ) {
                        Text(
                            "Improve My Diet",
                            color = Color(0xFF137A44),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Bottom spacing
            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

/**
   Individual Nutrient Score Card

   Displays individual nutrient scores with progress visualization
   Uses color coding to indicate performance levels (good/warning/poor)
   Provides detailed breakdown of specific nutrient achievements
  */
@Composable
private fun NutrientScoreCard(nutrient: NutrientUiModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header row with nutrient name and score
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = nutrient.label,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF333333)
                )

                Text(
                    text = "${nutrient.score.toInt()}/${nutrient.maxScore.toInt()}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = getColorForScore(nutrient.score, nutrient.maxScore)
                )
            }

            // Progress bar
            LinearProgressIndicator(
                progress = nutrient.score / nutrient.maxScore,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = getColorForScore(nutrient.score, nutrient.maxScore),
                trackColor = Color(0xFFE0E0E0)
            )

            // Optional: Add percentage text
            Text(
                text = "${((nutrient.score / nutrient.maxScore) * 100).toInt()}%",
                fontSize = 12.sp,
                color = Color(0xFF666666),
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

/**
 Score Color Calculation
   Determines appropriate color for nutrient scores based on performance thresholds
   Provides visual feedback for health assessment using traffic light system:
   - Green (80%+): Excellent nutrition performance
   - Yellow (50-79%): Moderate performance, room for improvement
   - Red (<50%): Poor performance, needs attention

 */
@Composable
private fun getColorForScore(score: Float, maxScore: Float): Color {
    val percentage = (score / maxScore) * 100
    return when {
        percentage >= 80 -> Color(0xFF137A44) // Green
        percentage >= 50 -> Color(0xFFFFDE59) // Yellow
        else -> Color(0xFFFF5757) // Red
    }
}