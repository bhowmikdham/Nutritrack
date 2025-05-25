package com.fit2081.nutritrack

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fit2081.nutritrack.data.Repo.AuthRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
Registration ViewModel Implementation

Comprehensive user registration logic with secure validation and state management
Handles multi-step registration process with clinician-pre-seeded data validation
Implements reactive state management for responsive UI updates

Registration Security Model:
- Clinician pre-seeds user IDs and phone numbers in system
- Users can only register with pre-validated credentials
- Prevents unauthorized account creation
- Validates ID and phone number combinations
- Ensures users cannot register twice

State Management Features:
- Reactive UI state through StateFlow
- Loading states for user feedback
- Comprehensive error handling and messaging
- Success state management for navigation
- Form validation with real-time feedback

Architecture Pattern:
- MVVM with Repository pattern for data access
- Coroutine-based asynchronous operations
- Reactive streams for UI state updates
- Proper error handling and recovery

Credit: ViewModel implementation follows Android Architecture Components guidelines
Reference: https://developer.android.com/topic/libraries/architecture/viewmodel
 */
class RegisterViewModel(
    private val repo: AuthRepository
) : ViewModel() {

    /**
    UI State Management with StateFlow

    Reactive state management for registration process
    Each state flow represents a specific aspect of the registration UI
    asStateFlow() provides read-only access for UI consumption
    MutableStateFlow enables controlled state updates from ViewModel

    State Flow Benefits:
    - Automatic UI recomposition when state changes
    - Type-safe state access from UI components
    - Built-in lifecycle awareness
    - Proper memory management and cleanup
     */
    // --- UI State Flows ---
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    /**
    Registration Success State Management

    Nullable Boolean state for registration attempt tracking
    Three-state system: null (not attempted), true (success), false (failure)
    Enables precise UI state management and navigation logic
     */
    /** null = not attempted yet, true = registered successfully */
    private val _registerSuccess = MutableStateFlow<Boolean?>(null)
    val registerSuccess: StateFlow<Boolean?> = _registerSuccess.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    /**
    Pre-Seeded User IDs Management

    Reactive loading of clinician-created user IDs
    Provides dropdown options for secure user selection
    Automatic updates when new users are added by clinicians
    Integration with repository for real-time data access
     */
    // --- Load dropdown user IDs (pre-seeded) ---
    private val _userIds = MutableStateFlow<List<String>>(emptyList())
    val userIds: StateFlow<List<String>> = _userIds.asStateFlow()

    /**
    User ID Loading Initialization

    Automatic loading of available user IDs on ViewModel creation
    Reactive data flow ensures UI updates when data changes
    Coroutine-based asynchronous loading for responsive UI
    Proper lifecycle management through viewModelScope
     */
    init {
        viewModelScope.launch {
            repo.allUserIds.collect { ids ->
                _userIds.value = ids
            }
        }
    }

    /**
    Registration Process Implementation

    Comprehensive multi-step registration validation and execution
    Implements secure registration flow with multiple validation checkpoints
    Provides detailed error messaging for user guidance and debugging

    Registration Steps:
    1. Validate user ID and phone number exist in pre-seeded data
    2. Verify user has not already completed self-registration
    3. Validate password requirements and confirmation matching
    4. Execute account activation through repository
    5. Provide success/failure feedback to UI

    Security Validations:
    - ID must exist in clinician-seeded database
    - Phone number must match pre-seeded contact information
    - User cannot register multiple times
    - Password must meet security requirements
    - Password confirmation must match exactly

    Error Handling:
    - Specific error messages for each validation failure
    - User-friendly error descriptions
    - Recovery guidance for common issues
    - Proper state cleanup on errors

    Credit: Security validation follows authentication best practices
    Reference: https://developer.android.com/training/articles/keystore
     */
    fun register(
        userId: String,
        phone: String,
        name: String,
        password: String,
        confirmPassword: String
    ) {
        viewModelScope.launch {
            /**
            UI State Reset for Registration Attempt

            Clear previous state before starting new registration
            Provides clean slate for validation and error handling
            Ensures proper loading state display during processing
             */
            // reset UI state
            _isLoading.value = true
            _registerSuccess.value = null
            _errorMessage.value = null

            /**
            Step 1: Pre-Seeded Data Validation

            Validates that user ID and phone number exist in clinician-seeded data
            Prevents unauthorized registration attempts
            Ensures only approved users can complete registration
            Provides specific error messaging for validation failures
             */
            // 1) ID & phone must exist
            if (!repo.isUserRegistered(userId) || !repo.isPhoneRegistered(phone)) {
                _errorMessage.value = "Invalid user ID or phone number"
                _isLoading.value = false
                return@launch
            }

            /**
            Step 2: Duplicate Registration Prevention

            Checks if user has already completed self-registration process
            Prevents multiple registration attempts for same user
            Maintains data integrity and security
            Provides clear feedback for already-registered users
             */
            // 2) not already registered
            if (repo.isSelfRegistered(userId)) {
                _errorMessage.value = "User already registered"
                _isLoading.value = false
                return@launch
            }

            /**
            Step 3: Password Validation and Confirmation

            Validates password meets minimum requirements
            Ensures password and confirmation match exactly
            Prevents common password entry errors
            Provides clear feedback for password issues

            Password Requirements:
            - Non-blank password required
            - Password and confirmation must match exactly
            - Future: Could implement complexity requirements
             */
            // 3) passwords must match
            if (password != confirmPassword) {
                _errorMessage.value = "Passwords do not match"
                _isLoading.value = false
                return@launch
            }

            // 4) Validate password length
            if (password.length < 6) {
                _errorMessage.value = "Password must be at least 6 characters long"
                _isLoading.value = false
                return@launch
            }

            // 5) Check for at least one uppercase letter
            if (!password.any { it.isUpperCase() }) {
                _errorMessage.value = "Password must contain at least 1 uppercase letter"
                _isLoading.value = false
                return@launch
            }

            // 6) Check for at least one special character
            val specialCharacters = "!@#$%^&*()_+-=[]{}|;:,.<>?"
            if (!password.any { it in specialCharacters }) {
                _errorMessage.value = "Password must contain at least 1 special character (!@#$%^&*()_+-=[]{}|;:,.<>?)"
                _isLoading.value = false
                return@launch
            }

            /**
            Step 4: Account Activation Execution

            Executes actual registration through repository layer
            Updates user record with completed profile information
            Handles database transaction success/failure
            Provides appropriate feedback based on operation result

            Registration Process:
            - Update user record with name and password
            - Mark account as self-registered
            - Enable login functionality
            - Maintain audit trail of registration
             */
            // 7) perform the update
            val ok = repo.registerUser(userId, phone, name, password)
            if (ok) _registerSuccess.value = true
            else   _errorMessage.value   = "Registration failed"

            _isLoading.value = false
        }
    }

    /**
    Registration State Reset Utility

    Utility function for resetting registration UI state
    Useful for retry scenarios and error recovery
    Enables clean state management during user interactions
    Provides fresh start for new registration attempts

    Use Cases:
    - User-initiated retry after errors
    - Navigation-based state cleanup
    - Testing and debugging scenarios
    - Form reset functionality
     */
    /** Reset the registration UI state (e.g. on retry) */
    fun resetRegisterState() {
        _isLoading.value       = false
        _registerSuccess.value = null
        _errorMessage.value    = null
    }
}