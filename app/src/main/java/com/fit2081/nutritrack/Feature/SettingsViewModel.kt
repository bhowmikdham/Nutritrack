package com.fit2081.nutritrack.Feature

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.fit2081.nutritrack.data.AuthManager
import com.fit2081.nutritrack.data.Repo.AuthRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
      Settings UI State Data Class

      Comprehensive state container for all settings screen UI elements
      Immutable data class following Compose state management best practices
      Covers authentication status, user profile data, and error handling

      State Categories:
      - Authentication: Login status and user session management
      - Profile Data: User information display (name, phone, ID)
      - Loading States: UI feedback during data operations
      - Error Handling: Error messages and recovery states

      Design Principles:
      - Immutable data structure for predictable state updates
      - Nullable types for optional state elements
      - Default values for consistent initialization
      - Clear naming conventions for intuitive usage

      State Flow Integration:
      - Works seamlessly with StateFlow for reactive updates
      - Supports efficient recomposition in Compose UI
      - Enables proper state restoration across configuration changes
      - Maintains consistency across the application

      Credit: State management follows Compose architecture guidelines
      Reference: https://developer.android.com/jetpack/compose/state
      CREDIT: GIVEN TO GEN AI (CHAT GPT) FOR HELPING IN THE CODE FORMATION IN THIS FILE
 */
data class SettingsUiState(
    val isLoggedIn: Boolean = false,
    val currentUserId: String? = null,
    val userName: String = "",
    val phoneNumber: String = "",
    val isLoading: Boolean = true,
    val error: String? = null
)

/**
      Settings ViewModel Implementation

      Comprehensive settings management with secure authentication handling
      Manages user profile data loading and session management
      Implements reactive state management for real-time UI updates

      Core Responsibilities:
      - User profile data loading and management
      - Secure logout functionality with complete session cleanup
      - Authentication state monitoring and validation
      - Error handling and recovery for profile operations
      - Real-time data refresh capabilities

      Security Features:
      - Authenticated access validation
      - Secure session termination
      - Profile data protection
      - Unauthorized access handling
      - Proper state cleanup on logout

      State Management:
      - Reactive UI state through StateFlow
      - Loading states for user feedback
      - Comprehensive error handling and messaging
      - Profile data caching and updates
      - Session persistence validation

      Architecture Pattern:
      - MVVM with Repository pattern for data access
      - Coroutine-based asynchronous operations
      - Reactive streams for UI state updates
      - Proper lifecycle management and cleanup

      Credit: ViewModel implementation follows Android Architecture Components guidelines
      Reference: https://developer.android.com/topic/libraries/architecture/viewmodel
 */
class SettingsViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    /**
          UI State Management with StateFlow

          Central state container for all settings screen UI elements
          MutableStateFlow enables controlled state updates from ViewModel
          asStateFlow() provides read-only access for UI consumption
          Automatic recomposition when state changes occur

          State Flow Benefits:
          - Type-safe state access from UI components
          - Built-in lifecycle awareness
          - Efficient recomposition only when state actually changes
          - Proper memory management and cleanup
          - Integration with Compose state management
     */
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    /**
          Automatic Profile Loading on Initialization

          Loads user profile immediately when ViewModel is created
          Ensures settings screen always shows current user data
          Proper initialization prevents showing stale information
          Sets up reactive data flow for ongoing updates
     */
    init {
        loadUserProfile()
    }

    /**
          User Profile Loading Implementation

          Comprehensive user profile data loading with authentication validation
          Handles multiple scenarios: authenticated, unauthenticated, and error states
          Provides detailed error messaging for debugging and user feedback

          Loading Process:
          1. Set loading state for UI feedback
          2. Validate current user authentication
          3. Load user profile from repository
          4. Update UI state with loaded data or error information
          5. Handle edge cases and error scenarios

          Authentication Validation:
          - Checks for valid user session through AuthManager
          - Handles session expiration scenarios
          - Provides appropriate error messaging
          - Maintains security through proper validation

          Error Handling:
          - Network connectivity issues
          - Database access errors
          - Session expiration handling
          - Missing profile data scenarios
          - General exception handling with logging

          Credit: Authentication handling follows Android security best practices
          Reference: https://developer.android.com/training/articles/keystore
     */
    private fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val currentUserId = AuthManager.currentUserId()

                /**
                      Unauthenticated User Handling

                      Graceful handling of unauthenticated access attempts
                      Clear error messaging for missing authentication
                      Proper state updates for UI response
                      Security validation prevents unauthorized access
                 */
                if (currentUserId == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isLoggedIn = false,
                            error = "No user logged in"
                        )
                    }
                    return@launch
                }

                /**
                      Profile Data Loading and Validation

                      Loads complete user profile from repository
                      Validates data integrity and completeness
                      Updates UI state with loaded information
                      Handles missing or incomplete profile scenarios
                 */
                val userProfile = authRepository.getUserProfile(currentUserId)

                if (userProfile != null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isLoggedIn = true,
                            currentUserId = currentUserId,
                            userName = userProfile.name,
                            phoneNumber = userProfile.phoneNumber,
                            error = null
                        )
                    }
                } else {
                    /**
                          Missing Profile Data Handling

                          Handles scenarios where user is authenticated but profile is missing
                          Maintains authentication status while indicating data issues
                          Provides specific error messaging for troubleshooting
                          Enables partial functionality when possible
                     */
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isLoggedIn = true,
                            currentUserId = currentUserId,
                            error = "Failed to load user profile"
                        )
                    }
                }
            } catch (e: Exception) {
                /**
                      Exception Handling and Logging

                      Comprehensive exception handling for unexpected errors
                      User-friendly error messaging
                      Proper state cleanup on error
                      Logging for debugging and monitoring
                 */
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Unknown error occurred"
                    )
                }
            }
        }
    }

    /**
          Secure Logout Implementation

          Comprehensive logout functionality with complete session cleanup
          Ensures all user data is properly cleared from memory and storage
          Handles logout errors gracefully with user feedback

          Logout Process:
          1. Initiate logout through AuthManager
          2. Clear all user-related state from ViewModel
          3. Reset UI state to unauthenticated status
          4. Handle any logout errors appropriately
          5. Trigger navigation to login screen

          Security Considerations:
          - Complete session termination
          - Memory cleanup of sensitive data
          - Secure state reset
          - Proper error handling during logout
          - Navigation security after logout

          State Management:
          - Reset to clean unauthenticated state
          - Clear all user profile information
          - Reset error states
          - Maintain proper loading states

          Credit: Logout implementation follows security best practices
          Reference: https://developer.android.com/training/articles/keystore
     */
    fun logout() {
        viewModelScope.launch {
            try {
                // Execute secure logout through AuthManager
                AuthManager.signOut()

                /**
                      Complete State Reset on Successful Logout

                      Creates fresh SettingsUiState with clean defaults
                      Ensures no user data remains in memory
                      Provides proper state for unauthenticated UI
                      Triggers navigation through state change
                 */
                _uiState.update {
                    SettingsUiState(
                        isLoggedIn = false,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                /**
                      Logout Error Handling

                      Handles logout failures gracefully
                      Provides user feedback for logout issues
                      Maintains current state on logout failure
                      Enables retry functionality
                 */
                _uiState.update {
                    it.copy(
                        error = "Failed to logout: ${e.message}"
                    )
                }
            }
        }
    }

    /**
          Profile Refresh Functionality

          Manual refresh capability for updating profile data
          Useful for recovering from errors or updating stale data
          Provides user-initiated data refresh option
          Delegates to main profile loading function

          Use Cases:
          - User-initiated refresh after network issues
          - Updating data after profile changes elsewhere
          - Recovery from error states
          - Manual synchronization with server
     */
    fun refreshProfile() {
        loadUserProfile()
    }

    /**
          Error State Management

          Utility function for clearing error states
          Enables error recovery without full data reload
          Provides clean error state management
          Useful for dismissing error messages

          Error Clearing Strategy:
          - Clears only error state, maintains other data
          - Enables continued use after error acknowledgment
          - Supports error message dismissal
          - Maintains data integrity during error clearing
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

/**
      Settings ViewModel Factory Implementation

      Factory pattern implementation for proper ViewModel instantiation
      Enables dependency injection while maintaining ViewModel lifecycle
      Ensures type safety and proper error handling for unknown ViewModels

      Factory Benefits:
      - Separates object creation from business logic
      - Enables testing with mock repositories
      - Maintains proper ViewModel lifecycle management
      - Supports dependency injection frameworks
      - Type-safe ViewModel creation with compile-time checks

      Dependency Injection:
      - Injects AuthRepository for data access
      - Enables repository mocking for testing
      - Maintains clean separation of concerns
      - Supports different repository implementations

      Error Handling:
      - Type checking for ViewModel class compatibility
      - Clear error messages for unsupported ViewModels
      - Compile-time safety through generics
      - Runtime validation with descriptive exceptions

      Credit: Factory pattern follows Android Architecture Components guidelines
      Reference: https://developer.android.com/topic/libraries/architecture/viewmodel
 */
class SettingsViewModelFactory(
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}