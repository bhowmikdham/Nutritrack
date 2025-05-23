package com.fit2081.nutritrack

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fit2081.nutritrack.data.Repo.AuthRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val repo: AuthRepository
) : ViewModel() {

    // --- UI State Flows ---
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    /** null = not attempted yet, true = registered successfully */
    private val _registerSuccess = MutableStateFlow<Boolean?>(null)
    val registerSuccess: StateFlow<Boolean?> = _registerSuccess.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // --- Load dropdown user IDs (pre-seeded) ---
    private val _userIds = MutableStateFlow<List<String>>(emptyList())
    val userIds: StateFlow<List<String>> = _userIds.asStateFlow()

    init {
        viewModelScope.launch {
            repo.allUserIds.collect { ids ->
                _userIds.value = ids
            }
        }
    }

    /**
     * Kick off registration:
     * 1) Verify ID & phone match an existing stub
     * 2) Ensure not already self-registered
     * 3) Check password & confirmation match
     * 4) Write name/password via repo.registerUser(...)
     */
    fun register(
        userId: String,
        phone: String,
        name: String,
        password: String,
        confirmPassword: String
    ) {
        viewModelScope.launch {
            // reset UI state
            _isLoading.value = true
            _registerSuccess.value = null
            _errorMessage.value = null

            // 1) ID & phone must exist
            if (!repo.isUserRegistered(userId) || !repo.isPhoneRegistered(phone)) {
                _errorMessage.value = "Invalid user ID or phone number"
                _isLoading.value = false
                return@launch
            }

            // 2) not already registered
            if (repo.isSelfRegistered(userId)) {
                _errorMessage.value = "User already registered"
                _isLoading.value = false
                return@launch
            }

            // 3) passwords must match
            if (password.isBlank() || password != confirmPassword) {
                _errorMessage.value = "Passwords do not match"
                _isLoading.value = false
                return@launch
            }

            // 4) perform the update
            val ok = repo.registerUser(userId, phone, name, password)
            if (ok) _registerSuccess.value = true
            else   _errorMessage.value   = "Registration failed"

            _isLoading.value = false
        }
    }

    /** Reset the registration UI state (e.g. on retry) */
    fun resetRegisterState() {
        _isLoading.value       = false
        _registerSuccess.value = null
        _errorMessage.value    = null
    }
}
