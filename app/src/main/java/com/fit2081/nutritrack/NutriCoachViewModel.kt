package com.fit2081.nutritrack

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.fit2081.nutritrack.data.Entity.CoachTips
import com.fit2081.nutritrack.data.Repo.CoachTipsRepository
import com.fit2081.nutritrack.data.model.Fruit
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class NutriCoachViewModel(
    private val repository: CoachTipsRepository,
    private val userId: String,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Keys for saved state
    private companion object {
        const val KEY_CURRENT_FRUIT = "current_fruit"
        const val KEY_CURRENT_TIP = "current_ai_tip"
        const val KEY_LAST_SEARCH = "last_search"
    }

    // UI State with saved state restoration
    private val _uiState = MutableStateFlow(
        NutriCoachUiState(
            currentFruit = savedStateHandle.get<Fruit>(KEY_CURRENT_FRUIT),
            currentAITip = savedStateHandle.get<String>(KEY_CURRENT_TIP) ?: "",
            lastSearchQuery = savedStateHandle.get<String>(KEY_LAST_SEARCH) ?: ""
        )
    )
    val uiState: StateFlow<NutriCoachUiState> = _uiState.asStateFlow()

    // User tips from database
    val userTips: StateFlow<List<CoachTips>> = repository.getTipsByPatient(userId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Save state when it changes
    init {
        viewModelScope.launch {
            _uiState.collect { state ->
                savedStateHandle[KEY_CURRENT_FRUIT] = state.currentFruit
                savedStateHandle[KEY_CURRENT_TIP] = state.currentAITip
                savedStateHandle[KEY_LAST_SEARCH] = state.lastSearchQuery
            }
        }
    }

    fun searchFruit(fruitName: String) {
        if (fruitName.isBlank()) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoadingFruit = true,
                    fruitError = null,
                    lastSearchQuery = fruitName
                )
            }

            repository.getFruitInfo(fruitName.trim()).fold(
                onSuccess = { fruit ->
                    _uiState.update {
                        it.copy(
                            currentFruit = fruit,
                            isLoadingFruit = false
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            fruitError = "Could not find fruit: ${error.message}",
                            isLoadingFruit = false
                        )
                    }
                }
            )
        }
    }

    fun generateMotivationalTip(userScore: Int, patientData: String = "") {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingAI = true, aiError = null) }

            // Use the comprehensive tip generation instead of the basic one
            repository.generateComprehensiveMotivationalTip(
                userId = userId,
                targetArea = "general" // You can make this dynamic based on user's lowest scores
            ).fold(
                onSuccess = { tip ->
                    _uiState.update {
                        it.copy(
                            currentAITip = tip,
                            isLoadingAI = false
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            aiError = "Could not generate tip: ${error.message}",
                            isLoadingAI = false
                        )
                    }
                }
            )
        }
    }

    fun toggleShowAllTips() {
        _uiState.update { it.copy(showAllTips = !it.showAllTips) }
    }

    fun clearCurrentFruit() {
        _uiState.update { it.copy(currentFruit = null, lastSearchQuery = "") }
    }

    fun clearCurrentTip() {
        _uiState.update { it.copy(currentAITip = "") }
    }
}

data class NutriCoachUiState(
    val currentFruit: Fruit? = null,
    val isLoadingFruit: Boolean = false,
    val fruitError: String? = null,
    val currentAITip: String = "",
    val isLoadingAI: Boolean = false,
    val aiError: String? = null,
    val showAllTips: Boolean = false,
    val lastSearchQuery: String = ""
)

// Updated ViewModel Factory with SavedStateHandle
class NutriCoachViewModelFactory(
    private val repository: CoachTipsRepository,
    private val userId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        throw UnsupportedOperationException("Use the companion object factory instead")
    }

    companion object {
        fun create(repository: CoachTipsRepository, userId: String) = viewModelFactory {
            initializer {
                NutriCoachViewModel(
                    repository = repository,
                    userId = userId,
                    savedStateHandle = createSavedStateHandle()
                )
            }
        }
    }
}
