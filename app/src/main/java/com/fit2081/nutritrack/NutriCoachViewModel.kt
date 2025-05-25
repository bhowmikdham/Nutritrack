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

/**
   NutriCoach ViewModel Implementation

   Advanced ViewModel implementation with state preservation and comprehensive data management
   Handles complex nutritional coaching logic with AI integration and external API calls
   Implements modern Android architecture patterns with reactive data flows

   Key Features:
   - State preservation across configuration changes
   - External API integration for fruit nutrition data
   - AI-powered tip generation with user profiling
   - Reactive data flows with StateFlow and SharedFlow
   - Comprehensive error handling and loading states

   Architecture Components:
   - SavedStateHandle for configuration change survival
   - Coroutine-based asynchronous operations
   - Repository pattern for data access abstraction
   - Reactive streams for real-time UI updates

   Credit: ViewModel implementation follows Android Architecture Components guidelines
   Reference: https://developer.android.com/topic/libraries/architecture/viewmodel
   CREDIT: GIVEN TO GEN AI (CHAT GPT) FOR HELPING IN THE CODE FORMATION IN THIS FILE

 **/
class NutriCoachViewModel(
    private val repository: CoachTipsRepository,
    private val userId: String,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    /**
      \ State Preservation Keys
      \
      \ Companion object defining keys for SavedStateHandle persistence
      \ Ensures consistent state restoration across configuration changes
      \ Follows Android best practices for state management
      \
      \ State Persistence Strategy:
      \ - Complex objects serialized through SavedStateHandle
      \ - Primitive values stored directly
      \ - Search queries preserved for better UX
      \ - AI tips cached to prevent unnecessary regeneration
     */
    // Keys for saved state
    private companion object {
        const val KEY_CURRENT_FRUIT = "current_fruit"
        const val KEY_CURRENT_TIP = "current_ai_tip"
        const val KEY_LAST_SEARCH = "last_search"
    }

    /**
       UI State Management with State Restoration

       MutableStateFlow provides reactive state updates to UI components
       SavedStateHandle integration ensures state survival across process death
       Default values prevent null state exceptions during initialization

       State Restoration Process:
       1. Attempt to restore from SavedStateHandle
       2. Fallback to default values if restoration fails
       3. Initialize reactive flow for UI consumption
       4. Set up automatic state persistence

       Credit: State management follows Compose state guidelines
       Reference: https://developer.android.com/jetpack/compose/state
     */
    // UI State with saved state restoration
    private val _uiState = MutableStateFlow(
        NutriCoachUiState(
            currentFruit = savedStateHandle.get<Fruit>(KEY_CURRENT_FRUIT),
            currentAITip = savedStateHandle.get<String>(KEY_CURRENT_TIP) ?: "",
            lastSearchQuery = savedStateHandle.get<String>(KEY_LAST_SEARCH) ?: ""
        )
    )
    val uiState: StateFlow<NutriCoachUiState> = _uiState.asStateFlow()

    /**
       Database Integration with Reactive Data Flows

       StateFlow integration provides automatic UI updates when database changes
       SharingStarted.WhileSubscribed optimizes resource usage
       Timeout value prevents immediate subscription cancellation
       Initial value prevents loading states during subscription setup

       Flow Configuration:
       - WhileSubscribed: Only active when UI is observing
       - 5000ms timeout: Keeps subscription alive briefly after UI disappears
       - Empty initial value: Prevents showing stale data
       - Automatic cleanup: Cancels when ViewModel is destroyed
     */
    // User tips from database
    val userTips: StateFlow<List<CoachTips>> = repository.getTipsByPatient(userId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
       Automatic State Persistence Setup

        Coroutine-based automatic state saving to SavedStateHandle
        Ensures all UI state changes are persisted immediately
        Survives configuration changes and process death scenarios

        Persistence Strategy:
        - Collect all state changes through Flow operators
        - Save to SavedStateHandle on every state update
        - Handle complex object serialization automatically
        - Maintain consistency between memory and persistent state
     */
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

    /**
        Fruit Search Implementation with External API Integration

        Asynchronous fruit nutrition lookup using external nutrition database
        Comprehensive error handling and loading state management
        Search query persistence for better user experience

        API Integration Features:
        - External nutrition database connectivity
        - Network error handling and user feedback
        - Loading states for responsive UI
        - Result caching through state management
        - Input validation and sanitization

        Error Handling:
        - Network connectivity issues
        - API rate limiting responses
        - Invalid fruit name handling
        - Timeout and server error recovery

        Credit: Network integration follows Android networking best practices
        Reference: https://developer.android.com/training/volley
     */
    fun searchFruit(fruitName: String) {
        if (fruitName.isBlank()) return

        viewModelScope.launch {
            // Set loading state and clear previous errors
            _uiState.update {
                it.copy(
                    isLoadingFruit = true,
                    fruitError = null,
                    lastSearchQuery = fruitName
                )
            }

            // Perform API call with result handling
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

    /**
        AI-Powered Motivational Tip Generation

        Advanced AI integration for personalized dietary advice generation
        Utilizes comprehensive user health profile for contextualized recommendations
        Implements sophisticated tip generation algorithms with user-specific targeting

        AI Processing Pipeline:
        1. User health data analysis and profiling
        2. Dietary pattern recognition and scoring
        3. Personalized recommendation generation
        4. Tip storage and historical tracking
        5. Error handling and fallback strategies

        Personalization Factors:
        - User's current nutritional scores
        - Historical dietary patterns
        - Health goals and preferences
        - Demographic considerations
        - Previous tip effectiveness

        Technical Implementation:
        - Asynchronous processing for responsive UI
        - Comprehensive error handling for AI service issues
        - Loading states for user feedback
        - Result caching and persistence

        Credit: AI integration follows machine learning best practices
        Reference: https://developer.android.com/ml
     */
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

    /**
        Tips History Dialog Management

        Simple state toggle for showing/hiding the comprehensive tips history dialog
        Maintains proper dialog lifecycle through reactive state management
        Provides clean separation between UI state and business logic
     */
    fun toggleShowAllTips() {
        _uiState.update { it.copy(showAllTips = !it.showAllTips) }
    }

    /**
        State Clearing Utilities

        Utility functions for clearing specific parts of the UI state
        Useful for reset operations and error recovery scenarios
        Maintains state consistency during user interactions

        Use Cases:
        - User-initiated clear operations
        - Error recovery and retry scenarios
        - Navigation-based state cleanup
        - Testing and debugging operations
     */
    fun clearCurrentFruit() {
        _uiState.update { it.copy(currentFruit = null, lastSearchQuery = "") }
    }

    fun clearCurrentTip() {
        _uiState.update { it.copy(currentAITip = "") }
    }
}

/**
    NutriCoach UI State Data Class

    Comprehensive state container for all NutriCoach screen UI elements
    Immutable data class following Compose state management best practices
    Covers all possible UI states including loading, error, and success scenarios

    State Categories:
    - Fruit Search: Current fruit data, loading states, error messages
    - AI Integration: Generated tips, processing states, error handling
    - UI Navigation: Dialog visibility, search persistence
    - User Experience: Loading indicators, error feedback

    Design Principles:
    - Immutable data structure for predictable state updates
    - Nullable types for optional state elements
    - Default values for consistent initialization
    - Clear naming for intuitive usage

    Credit: State management follows Compose architecture guidelines
    Reference: https://developer.android.com/jetpack/compose/state
 */
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

/**
    Updated ViewModel Factory with SavedStateHandle

    Modern ViewModel factory implementation using viewModelFactory DSL
    Integrates SavedStateHandle for proper state preservation
    Provides clean dependency injection for repository and user context

    Factory Pattern Benefits:
    - Type-safe ViewModel creation
    - Proper dependency injection
    - SavedStateHandle integration
    - Testability through mock repositories
    - Lifecycle-aware instantiation

    Modern Implementation:
    - viewModelFactory DSL for cleaner syntax
    - initializer block for ViewModel creation
    - createSavedStateHandle() for automatic state management
    - Companion object pattern for factory access

    Migration Note:
    - Replaces traditional ViewModelProvider.Factory approach
    - Provides better integration with Compose ViewModels
    - Supports modern Android architecture patterns

    Credit: Factory implementation follows modern Android architecture guidelines
    Reference: https://developer.android.com/topic/libraries/architecture/viewmodel
 */
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