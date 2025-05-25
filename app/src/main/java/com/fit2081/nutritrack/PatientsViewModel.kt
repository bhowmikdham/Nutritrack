// --- InsightsViewModel.kt ---
package com.fit2081.nutritrack

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.fit2081.nutritrack.data.AppDatabase
import com.fit2081.nutritrack.data.AuthManager
import com.fit2081.nutritrack.data.Entity.PatientHealthRecords
import com.fit2081.nutritrack.data.Repo.HealthRecordsRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Represents a single nutrient score for display.
 * CREDIT: GIVEN TO GEN AI (CHAT GPT) IN HELPING FOR ADAPTATION OF THIS FILE AND IMPLEMENTATION OF CERTAIN CODE SNIPPETS
 */
data class NutrientUiModel(
    val label: String,
    val score: Float,
    val maxScore: Float
)

/**
 * UI state for InsightsScreen.
 * - isLoading: whether data is loading
 * - nutrients: list of nutrient scores
 * - totalScore: computed total
 * - error: optional error message
 */
data class InsightsUiState(
    val isLoading: Boolean = true,
    val nutrients: List<NutrientUiModel> = emptyList(),
    val totalScore: Int = 0,
    val error: String? = null
)

class InsightsViewModel(context: Context) : ViewModel() {
    private val repo = HealthRecordsRepository(
        AppDatabase.getDatabase(context).patientHealthRecordsDAO()
    )

    private val _uiState = MutableStateFlow(InsightsUiState())
    val uiState: StateFlow<InsightsUiState> = _uiState.asStateFlow()

    init {
        loadRecord()
    }

    private fun loadRecord() {
        val userId = AuthManager.currentUserId()
        if (userId == null) {
            _uiState.value = InsightsUiState(
                isLoading = false,
                error = "Not logged in"
            )
            return
        }

        viewModelScope.launch {
            repo.recordFor(userId)
                .mapNotNull { it.firstOrNull() }
                .onStart { _uiState.value = InsightsUiState(isLoading = true) }
                .catch { e ->
                    _uiState.value = InsightsUiState(
                        isLoading = false,
                        error = e.message
                    )
                }
                .collect { rec ->
                    // compute nutrients list based on sex
                    val sex = rec.sex
                    val nutrients = listOf(
                        NutrientUiModel(
                            "Vegetables",
                            if (sex == "Male") rec.vegetablesHeifaScoreMale else rec.vegetablesHeifaScoreFemale,
                            10f
                        ),
                        NutrientUiModel(
                            "Fruit",
                            if (sex == "Male") rec.fruitHeifaScoreMale else rec.fruitHeifaScoreFemale,
                            10f
                        ),
                        NutrientUiModel(
                            "Grains & Cereals",
                            if (sex == "Male") rec.grainsAndCerealsHeifaScoreMale else rec.grainsAndCerealsHeifaScoreFemale,
                            10f
                        ),
                        NutrientUiModel(
                            "Whole Grains",
                            if (sex == "Male") rec.wholeGrainsHeifaScoreMale else rec.wholeGrainsHeifaScoreFemale,
                            10f
                        ),
                        NutrientUiModel(
                            "Meat & Alternatives",
                            if (sex == "Male") rec.meatAndAlternativesHeifaScoreMale else rec.meatAndAlternativesHeifaScoreFemale,
                            10f
                        ),
                        NutrientUiModel(
                            "Dairy & Alternatives",
                            if (sex == "Male") rec.dairyAndAlternativesHeifaScoreMale else rec.dairyAndAlternativesHeifaScoreFemale,
                            10f
                        ),
                        NutrientUiModel(
                            "Sodium",
                            if (sex == "Male") rec.sodiumHeifaScoreMale else rec.sodiumHeifaScoreFemale,
                            10f
                        ),
                        NutrientUiModel(
                            "Alcohol",
                            if (sex == "Male") rec.alcoholHeifaScoreMale else rec.alcoholHeifaScoreFemale,
                            5f
                        ),
                        NutrientUiModel(
                            "Water",
                            if (sex == "Male") rec.waterHeifaScoreMale else rec.waterHeifaScoreFemale,
                            5f
                        ),
                        NutrientUiModel(
                            "Sugar",
                            if (sex == "Male") rec.sugarHeifaScoreMale else rec.sugarHeifaScoreFemale,
                            10f
                        ),
                        NutrientUiModel(
                            "Saturated Fats",
                            if (sex == "Male") rec.saturatedFatHeifaScoreMale else rec.saturatedFatHeifaScoreFemale,
                            10f
                        ),
                        NutrientUiModel(
                            "Unsaturated Fats",
                            if (sex == "Male") rec.unsaturatedFatHeifaScoreMale else rec.unsaturatedFatHeifaScoreFemale,
                            10f
                        ),
                        NutrientUiModel(
                            "Discretionary Foods",
                            if (sex == "Male") rec.discretionaryHeifaScoreMale else rec.discretionaryHeifaScoreFemale,
                            10f
                        )
                    )
                    // compute total score
                    val total = if (sex == "Male") rec.heifaTotalScoreMale else rec.heifaTotalScoreFemale

                    _uiState.value = InsightsUiState(
                        isLoading = false,
                        nutrients = nutrients,
                        totalScore = total.toInt()
                    )
                }
        }
    }
}

/**
 * Factory to create InsightsViewModel with Context.
 * HELP FROM GENAI WAS TAKEN FOR PRODUCTION OF THIS
 */
class InsightsViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InsightsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return InsightsViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class ${modelClass.name}")
    }
}
