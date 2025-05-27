package com.fit2081.nutritrack.Feature

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.fit2081.nutritrack.data.Repo.AuthRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ForgotPasswordUiState(
    val isLoading: Boolean = false,
    val step: ForgotPasswordStep = ForgotPasswordStep.ENTER_DETAILS,
    val userId: String = "",
    val phoneNumber: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val resetSuccess: Boolean = false,
    val userExists: Boolean = false
)

enum class ForgotPasswordStep {
    ENTER_DETAILS,      // Enter User ID and Phone
    VERIFY_DETAILS,     // Verify the details match
    SET_NEW_PASSWORD,   // Set new password
    SUCCESS             // Password reset successful
}

/**
Forgot Password ViewModel

Manages the multi-step password reset flow with comprehensive validation
Handles user verification, password validation, and state management
Implements secure identity verification before allowing password changes
CREDIT: GIVEN TO GEN AI (CHAT GPT) FOR HELPING IN THE CODE FORMATION IN THIS FILE

 */
class ForgotPasswordViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    // Load available user IDs for dropdown
    private val _userIds = MutableStateFlow<List<String>>(emptyList())
    val userIds: StateFlow<List<String>> = _userIds.asStateFlow()

    /**
    Initialization and User ID Loading

    Automatically loads available user IDs from repository for dropdown selection
    Maintains reactive connection to user data changes
     */
    init {
        viewModelScope.launch {
            authRepository.allUserIds.collect { ids ->
                _userIds.value = ids
            }
        }
    }

    fun updateUserId(userId: String) {
        _uiState.update {
            it.copy(
                userId = userId.trim(),
                errorMessage = null
            )
        }
    }

    fun updatePhoneNumber(phone: String) {
        _uiState.update {
            it.copy(
                phoneNumber = phone.trim(),
                errorMessage = null
            )
        }
    }

    fun updateNewPassword(password: String) {
        _uiState.update {
            it.copy(
                newPassword = password,
                errorMessage = null
            )
        }
    }

    fun updateConfirmPassword(password: String) {
        _uiState.update {
            it.copy(
                confirmPassword = password,
                errorMessage = null
            )
        }
    }

    /**
    User Details Verification Process

    Comprehensive validation process that performs multi-layered security checks:
    1. User existence verification in the system
    2. Phone number registration status validation
    3. Correlation verification between user ID and phone number

    Only proceeds to password reset if all verification steps pass
     */
    fun verifyUserDetails() {
        val currentState = _uiState.value

        if (currentState.userId.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter your User ID") }
            return
        }

        if (currentState.phoneNumber.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter your phone number") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                // Check if user exists
                val userExists = authRepository.isUserRegistered(currentState.userId)
                if (!userExists) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "User ID not found in our records"
                        )
                    }
                    return@launch
                }

                // Check if phone number matches
                val phoneMatches = authRepository.isPhoneRegistered(currentState.phoneNumber)
                if (!phoneMatches) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Phone number doesn't match our records"
                        )
                    }
                    return@launch
                }

                // Verify that the user ID and phone belong to the same user
                val userProfile = authRepository.getUserProfile(currentState.userId)
                if (userProfile?.phoneNumber != currentState.phoneNumber) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "User ID and phone number don't match"
                        )
                    }
                    return@launch
                }

                // If verification successful, proceed to password reset
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        step = ForgotPasswordStep.SET_NEW_PASSWORD,
                        userExists = true,
                        successMessage = "Details verified! Please set your new password."
                    )
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error verifying details: ${e.message}"
                    )
                }
            }
        }
    }

    /**
    Password Reset Implementation

    Validates new password criteria and executes password update with security checks:
    - Password length requirements (minimum 6 characters)
    - Password confirmation matching
    - Secure update through repository layer
     */
    fun resetPassword() {
        val currentState = _uiState.value

        // Validate passwords
        if (currentState.newPassword.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter a new password") }
            return
        }

        if (currentState.newPassword.length < 6) {
            _uiState.update { it.copy(errorMessage = "Password must be at least 6 characters") }
            return
        }

        // Check for at least one uppercase letter
        if (!currentState.newPassword.any { it.isUpperCase() }) {
            _uiState.update { it.copy(errorMessage = "Password must contain at least 1 uppercase letter") }
            return
        }

        // Check for at least one special character
        val specialCharacters = "!@#$%^&*()_+-=[]{}|;:,.<>?"
        if (!currentState.newPassword.any { it in specialCharacters }) {
            _uiState.update { it.copy(errorMessage = "Password must contain at least 1 special character (!@#$%^&*()_+-=[]{}|;:,.<>?)" ) }
            return
        }

        if (currentState.newPassword != currentState.confirmPassword) {
            _uiState.update { it.copy(errorMessage = "Passwords don't match") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                // Reset password using the repository
                val success = authRepository.resetPassword(
                    currentState.userId,
                    currentState.phoneNumber,
                    currentState.newPassword
                )

                if (success) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            step = ForgotPasswordStep.SUCCESS,
                            resetSuccess = true,
                            successMessage = "Password reset successful! You can now login with your new password."
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Failed to reset password. Please try again."
                        )
                    }
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error resetting password: ${e.message}"
                    )
                }
            }
        }
    }

    /**
    Navigation and State Management

    Provides step navigation and state reset functionality
    Allows users to go back in the flow and clear temporary states
     */
    fun goToNextStep() {
        when (_uiState.value.step) {
            ForgotPasswordStep.ENTER_DETAILS -> verifyUserDetails()
            ForgotPasswordStep.SET_NEW_PASSWORD -> resetPassword()
            else -> { /* No action needed for other steps */ }
        }
    }

    fun goBack() {
        when (_uiState.value.step) {
            ForgotPasswordStep.SET_NEW_PASSWORD -> {
                _uiState.update {
                    it.copy(
                        step = ForgotPasswordStep.ENTER_DETAILS,
                        errorMessage = null,
                        successMessage = null
                    )
                }
            }
            else -> { /* Can't go back from first step */ }
        }
    }

    fun clearMessages() {
        _uiState.update {
            it.copy(
                errorMessage = null,
                successMessage = null
            )
        }
    }

    fun resetState() {
        _uiState.value = ForgotPasswordUiState()
    }
}

/**
ViewModel Factory for Dependency Injection

Provides proper dependency injection for ForgotPasswordViewModel
Ensures ViewModel receives required AuthRepository for user operations
 */
class ForgotPasswordViewModelFactory(
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ForgotPasswordViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ForgotPasswordViewModel(authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}