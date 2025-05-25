package com.fit2081.nutritrack

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.fit2081.nutritrack.data.AppDatabase
import com.fit2081.nutritrack.data.Entity.PatientHealthRecords
import com.fit2081.nutritrack.data.Repo.HealthRecordsRepository
import com.fit2081.nutritrack.service.GenAIService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ClinicianDashboardUiState(
    val isLoading: Boolean = true,
    val totalPatients: Int = 0,
    val maleCount: Int = 0,
    val femaleCount: Int = 0,
    val healthyCount: Int = 0,       // 80-100 score
    val atRiskCount: Int = 0,        // 50-79 score
    val unhealthyCount: Int = 0,     // 0-49 score
    val maleAverageScore: Int = 0,
    val femaleAverageScore: Int = 0,
    val overallAverageScore: Int = 0,
    val aiInsights: List<String> = emptyList(),
    val isGeneratingInsights: Boolean = false,
    val insightsError: String? = null,
    val error: String? = null
)

/**
    Clinician Dashboard ViewModel

    Manages patient health data aggregation and AI insight generation
    Processes health records for population-level analytics and reporting
 */
class ClinicianDashboardViewModel(
    context: Context
) : ViewModel() {

    private val repo = HealthRecordsRepository(
        AppDatabase.getDatabase(context).patientHealthRecordsDAO()
    )

    private val genAIService = GenAIService()

    private val _uiState = MutableStateFlow(ClinicianDashboardUiState())
    val uiState: StateFlow<ClinicianDashboardUiState> = _uiState.asStateFlow()

    /**
        Data Loading and Processing

        Loads all patient health records and processes them for dashboard display
        Calculates population statistics, gender distributions, and health metrics
     */
    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                repo.allRecords()
                    .map { records -> records.filterNotNull() }
                    .catch { e ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = e.message ?: "Failed to load data"
                            )
                        }
                    }
                    .collect { records ->
                        processRecords(records)
                    }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Unknown error"
                    )
                }
            }
        }
    }

    fun refreshData() {
        loadData()
    }

    /**
        Health Records Processing Engine

        Calculates comprehensive health statistics from patient data including:
        - Gender-specific score averages (male vs female HEIFA scores)
        - Health status distribution (healthy/at-risk/unhealthy categories)
        - Population demographics and overall health metrics
     */
    private fun processRecords(records: List<PatientHealthRecords>) {
        val maleRecords = records.filter { it.sex.equals("Male", ignoreCase = true) }
        val femaleRecords = records.filter { it.sex.equals("Female", ignoreCase = true) }

        // Calculate average scores
        val maleAvg = if (maleRecords.isNotEmpty()) {
            maleRecords.map { it.heifaTotalScoreMale.toDouble() }.average().toInt()
        } else 0

        val femaleAvg = if (femaleRecords.isNotEmpty()) {
            femaleRecords.map { it.heifaTotalScoreFemale.toDouble() }.average().toInt()
        } else 0

        val overallAvg = if (records.isNotEmpty()) {
            val allScores = maleRecords.map { it.heifaTotalScoreMale.toDouble() } +
                    femaleRecords.map { it.heifaTotalScoreFemale.toDouble() }
            allScores.average().toInt()
        } else 0

        // Calculate health status distribution
        val allScores = maleRecords.map { it.heifaTotalScoreMale.toInt() } +
                femaleRecords.map { it.heifaTotalScoreFemale.toInt() }

        val healthyCount = allScores.count { it >= 80 }
        val atRiskCount = allScores.count { it in 50..79 }
        val unhealthyCount = allScores.count { it < 50 }

        _uiState.update {
            it.copy(
                isLoading = false,
                totalPatients = records.size,
                maleCount = maleRecords.size,
                femaleCount = femaleRecords.size,
                healthyCount = healthyCount,
                atRiskCount = atRiskCount,
                unhealthyCount = unhealthyCount,
                maleAverageScore = maleAvg,
                femaleAverageScore = femaleAvg,
                overallAverageScore = overallAvg,
                error = null
            )
        }
    }

    /**
        AI Insights Generation System

        Integrates with GenAI service to generate intelligent analysis of population health data
        Creates detailed context from current dashboard statistics including:
        - Population demographics and distributions
        - Health status percentages and trends
        - Gender-specific health patterns
        - Risk assessment and intervention recommendations
     */
    fun generateInsights() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isGeneratingInsights = true,
                    insightsError = null
                )
            }

            val currentState = _uiState.value

            // Create additional data context for better insights
            val additionalData = buildString {
                append("Population Distribution:\n")
                append("- Total Patients: ${currentState.totalPatients}\n")
                append("- Male: ${currentState.maleCount}, Female: ${currentState.femaleCount}\n")
                append("- Health Status Distribution:\n")
                append("  * Healthy (80-100): ${currentState.healthyCount} patients\n")
                append("  * At Risk (50-79): ${currentState.atRiskCount} patients\n")
                append("  * Unhealthy (0-49): ${currentState.unhealthyCount} patients\n")
                append("- Gender Score Difference: ${kotlin.math.abs(currentState.maleAverageScore - currentState.femaleAverageScore)}\n")

                // Calculate percentages
                if (currentState.totalPatients > 0) {
                    val healthyPercentage = (currentState.healthyCount * 100) / currentState.totalPatients
                    val atRiskPercentage = (currentState.atRiskCount * 100) / currentState.totalPatients
                    val unhealthyPercentage = (currentState.unhealthyCount * 100) / currentState.totalPatients

                    append("- Health Status Percentages:\n")
                    append("  * Healthy: ${healthyPercentage}%\n")
                    append("  * At Risk: ${atRiskPercentage}%\n")
                    append("  * Unhealthy: ${unhealthyPercentage}%\n")
                }
            }

            genAIService.generateDataInsights(
                maleAvgScore = currentState.maleAverageScore.toFloat(),
                femaleAvgScore = currentState.femaleAverageScore.toFloat(),
                additionalData = additionalData
            ).fold(
                onSuccess = { insights ->
                    _uiState.update {
                        it.copy(
                            aiInsights = insights,
                            isGeneratingInsights = false
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            insightsError = error.message ?: "Failed to generate insights",
                            isGeneratingInsights = false
                        )
                    }
                }
            )
        }
    }
}

/**
    ViewModel Factory for Dependency Injection

    Provides proper dependency injection for ClinicianDashboardViewModel
    Ensures ViewModel receives required Context for database operations
 */
class ClinicianDashboardViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ClinicianDashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ClinicianDashboardViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class ${modelClass.name}")
    }
}