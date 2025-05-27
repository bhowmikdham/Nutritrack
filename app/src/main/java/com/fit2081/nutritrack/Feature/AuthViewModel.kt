package com.fit2081.nutritrack.Feature
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fit2081.nutritrack.data.AuthManager
import com.fit2081.nutritrack.data.Repo.AuthRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Authentication ViewModel
 *
 * Manages authentication state and login flow for the application
 * Handles user validation, session persistence, and form state management
 *
 */
class AuthViewModel(
    private val repo: AuthRepository
) : ViewModel() {

    // --- UI State Flows ---
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _loginSuccess = MutableStateFlow<Boolean?>(null) // null = not attempted yet
    val loginSuccess: StateFlow<Boolean?> = _loginSuccess.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isNotRegistered = MutableStateFlow(false)
    val isNotRegistered: StateFlow<Boolean> = _isNotRegistered.asStateFlow()

    private val _hasCompletedFoodIntake = MutableStateFlow<Boolean?>(null)
    val hasCompletedFoodIntake: StateFlow<Boolean?> = _hasCompletedFoodIntake.asStateFlow()

    // --- Load dropdown user IDs ---
    private val _userIds = MutableStateFlow<List<String>>(emptyList())
    val userIds: StateFlow<List<String>> = _userIds.asStateFlow()

    // Add form state to ViewModel to survive rotation
    private val _selectedUserId = MutableStateFlow("")
    val selectedUserId: StateFlow<String> = _selectedUserId.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _showPassword = MutableStateFlow(false)
    val showPassword: StateFlow<Boolean> = _showPassword.asStateFlow()

    private val _dropdownExpanded = MutableStateFlow(false)
    val dropdownExpanded: StateFlow<Boolean> = _dropdownExpanded.asStateFlow()

    /**
     * Initialization Block
     *
     * Automatically loads available user IDs from repository on ViewModel creation
     * Maintains reactive connection to user data changes
     * help of GenAi was taken
     */
    init {
        viewModelScope.launch {
            repo.allUserIds.collect { ids ->
                _userIds.value = ids
            }
        }
    }

    // Form state management functions
    fun updateSelectedUserId(userId: String) {
        _selectedUserId.value = userId
    }

    fun updatePassword(password: String) {
        _password.value = password
    }

    fun updateShowPassword(show: Boolean) {
        _showPassword.value = show
    }

    fun updateDropdownExpanded(expanded: Boolean) {
        _dropdownExpanded.value = expanded
    }

    /**
     * Comprehensive Login Validation Flow
     *
     * Orchestrates the entire authentication process in sequential steps:
     * 1. User existence verification
     * 2. Registration completion check
     * 3. Password authentication
     * 4. Session establishment
     */
    fun validateAndLogin(userId: String = _selectedUserId.value, password: String = _password.value) {
        viewModelScope.launch {
            // Reset everything
            _isLoading.value = true
            _loginSuccess.value = null
            _errorMessage.value = null
            _isNotRegistered.value = false
            _hasCompletedFoodIntake.value = null

            try {
                // 1) user exists?
                if (!repo.isUserRegistered(userId)) {
                    _errorMessage.value = "User ID not found"
                    return@launch
                }

                // 2) registration complete?
                if (!repo.isSelfRegistered(userId)) {
                    _isNotRegistered.value = true
                    return@launch
                }

                // 3) password check
                val patient = repo.getByPasswordAndUserID(userId, password)
                if (patient == null) {
                    _errorMessage.value = "Incorrect password"
                    return@launch
                }

                // 4) success — persist session
                AuthManager.signIn(userId)
                _loginSuccess.value = true

            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Unknown error"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
       State Reset Functions

       Provides different levels of state clearing for various UI scenarios
       Allows selective reset of authentication vs form data
     */
    fun resetLoginState() {
        _isLoading.value = false
        _loginSuccess.value = null
        _errorMessage.value = null
        _isNotRegistered.value = false
        _hasCompletedFoodIntake.value = null
        _selectedUserId.value = ""
        _password.value = ""
        _showPassword.value = false
        _dropdownExpanded.value = false
    }

    /** Clear only authentication state, keep form data */
    fun clearAuthState() {
        _isLoading.value = false
        _loginSuccess.value = null
        _errorMessage.value = null
        _isNotRegistered.value = false
        _hasCompletedFoodIntake.value = null
    }
}