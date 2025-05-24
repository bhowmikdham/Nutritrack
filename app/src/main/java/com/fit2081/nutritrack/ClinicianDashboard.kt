package com.fit2081.nutritrack

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.fit2081.nutritrack.data.AppDatabase
import com.fit2081.nutritrack.data.Entity.PatientHealthRecords
import com.fit2081.nutritrack.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClinicianDashboardScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel: ClinicianDashboardViewModel = viewModel(
        factory = ClinicianDashboardViewModelFactory(context)
    )

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Clinician Dashboard",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshData() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFFC1FF72)
                )
            )
        },
        containerColor = Color.White
    ) { innerPadding ->

        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Overview Cards
                item {
                    Text(
                        "Population Overview",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                item {
                    OverviewCardsRow(uiState)
                }

                // Gender Distribution
                item {
                    GenderDistributionCard(uiState)
                }

                // Health Status Distribution
                item {
                    HealthStatusCard(uiState)
                }

                // Average Scores by Gender
                item {
                    AverageScoresCard(uiState)
                }

                // AI Insights Section
                item {
                    AIInsightsCard(uiState, viewModel)
                }

                // Actions Section
                item {
                    ActionsCard(navController)
                }
            }
        }
    }
}

@Composable
private fun OverviewCardsRow(uiState: ClinicianDashboardUiState) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            listOf(
                "Total Patients" to uiState.totalPatients.toString(),
                "Male" to uiState.maleCount.toString(),
                "Female" to uiState.femaleCount.toString(),
                "Avg Score" to "${uiState.overallAverageScore}%"
            )
        ) { (label, value) ->
            Card(
                modifier = Modifier
                    .width(120.dp)
                    .height(100.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF5F5F5)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = value,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF137A44)
                    )
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
private fun GenderDistributionCard(uiState: ClinicianDashboardUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Gender Distribution",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Male percentage
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Male", fontSize = 14.sp)
                Text("${uiState.maleCount}", fontWeight = FontWeight.Medium)
            }
            LinearProgressIndicator(
                progress = if (uiState.totalPatients > 0) uiState.maleCount.toFloat() / uiState.totalPatients else 0f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .padding(vertical = 4.dp),
                color = Color(0xFF2196F3),
                trackColor = Color(0xFFE3F2FD)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Female percentage
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Female", fontSize = 14.sp)
                Text("${uiState.femaleCount}", fontWeight = FontWeight.Medium)
            }
            LinearProgressIndicator(
                progress = if (uiState.totalPatients > 0) uiState.femaleCount.toFloat() / uiState.totalPatients else 0f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .padding(vertical = 4.dp),
                color = Color(0xFFE91E63),
                trackColor = Color(0xFFFCE4EC)
            )
        }
    }
}

@Composable
private fun HealthStatusCard(uiState: ClinicianDashboardUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Health Status Distribution",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Healthy (80+)
            HealthStatusRow(
                label = "Healthy (80-100)",
                count = uiState.healthyCount,
                total = uiState.totalPatients,
                color = Color(0xFF4CAF50)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // At Risk (50-79)
            HealthStatusRow(
                label = "At Risk (50-79)",
                count = uiState.atRiskCount,
                total = uiState.totalPatients,
                color = Color(0xFFFF9800)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Unhealthy (0-49)
            HealthStatusRow(
                label = "Unhealthy (0-49)",
                count = uiState.unhealthyCount,
                total = uiState.totalPatients,
                color = Color(0xFFF44336)
            )
        }
    }
}

@Composable
private fun HealthStatusRow(
    label: String,
    count: Int,
    total: Int,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 14.sp)
        Text("$count", fontWeight = FontWeight.Medium)
    }
    LinearProgressIndicator(
        progress = if (total > 0) count.toFloat() / total else 0f,
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .padding(vertical = 4.dp),
        color = color,
        trackColor = color.copy(alpha = 0.2f)
    )
}

@Composable
private fun AverageScoresCard(uiState: ClinicianDashboardUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Average HEIFA Scores by Gender",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ScoreColumn(
                    label = "Male Average",
                    score = uiState.maleAverageScore,
                    color = Color(0xFF2196F3)
                )

                ScoreColumn(
                    label = "Female Average",
                    score = uiState.femaleAverageScore,
                    color = Color(0xFFE91E63)
                )

                ScoreColumn(
                    label = "Overall Average",
                    score = uiState.overallAverageScore,
                    color = Color(0xFF137A44)
                )
            }
        }
    }
}

@Composable
private fun ScoreColumn(
    label: String,
    score: Int,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "$score",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            color = Color.Gray
        )
    }
}

@Composable
private fun AIInsightsCard(
    uiState: ClinicianDashboardUiState,
    viewModel: ClinicianDashboardViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
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
                    "AI-Generated Insights",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Button(
                    onClick = { viewModel.generateInsights() },
                    enabled = !uiState.isGeneratingInsights,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6200EE)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (uiState.isGeneratingInsights) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White
                        )
                    } else {
                        Text("Generate", fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.aiInsights.isNotEmpty()) {
                uiState.aiInsights.forEachIndexed { index, insight ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        )
                    ) {
                        Text(
                            text = "${index + 1}. $insight",
                            modifier = Modifier.padding(12.dp),
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    }
                }
            } else if (uiState.insightsError != null) {
                Text(
                    text = "Error generating insights: ${uiState.insightsError}",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp
                )
            } else {
                Text(
                    text = "Click 'Generate' to get AI-powered insights about your patient population",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun ActionsCard(navController: NavHostController) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Quick Actions",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        navController.navigate(Screen.Welcome.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFC1FF72),
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Exit to Main")
                }

                OutlinedButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Back to Settings")
                }
            }
        }
    }
}