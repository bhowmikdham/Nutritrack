package com.fit2081.nutritrack

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fit2081.nutritrack.data.Repo.AuthRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Represents the possible outcomes of a login attempt.
 */
sealed class LoginResult {
    object Success : LoginResult()
    object IncompleteRegistration : LoginResult()
    object InvalidCredentials : LoginResult()
}

/**
 * ViewModel for handling authentication logic.
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repo: AuthRepository
) : ViewModel() {
    // Holds the list of valid user IDs for dropdown suggestions
    private val _userIds = MutableStateFlow<List<String>>(emptyList())
    val userIds = _userIds.asStateFlow()

    // Input fields
    var userId by mutableStateOf("")
        private set
    var phone by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set

    // Validation flags
    var userIdError by mutableStateOf(false)
        private set
    var phoneError by mutableStateOf(false)
        private set
    var registrationIncomplete by mutableStateOf(false)
        private set

    // Emits the result of login attempts
    private val _loginResult = MutableSharedFlow<LoginResult>()
    val loginResult: SharedFlow<LoginResult> = _loginResult.asSharedFlow()

    init {
        // Load all user IDs from repository
        viewModelScope.launch {
            repo.allUserIds.collect { ids ->
                _userIds.value = ids
            }
        }
    }

    fun onUserIdChange(id: String) {
        userId = id
        userIdError = false
        registrationIncomplete = false
    }

    fun onPhoneChange(p: String) {
        phone = p
        phoneError = false
    }

    fun onPasswordChange(pw: String) {
        password = pw
    }

    /**
     * Executes when the user clicks the Continue button.
     */
    fun onLoginClicked() {
        viewModelScope.launch {
            // Reset error states
            userIdError = false
            phoneError = false
            registrationIncomplete = false

            // 1) Verify the user ID exists
            val existsId = repo.isUserRegistered(userId)
            if (!existsId) {
                userIdError = true
                _loginResult.emit(LoginResult.InvalidCredentials)
                return@launch
            }

            // 2) Check if self-registration (name/password) is complete
            val complete = repo.isSelfRegistered(userId)
            if (!complete) {
                registrationIncomplete = true
                _loginResult.emit(LoginResult.IncompleteRegistration)
                return@launch
            }

            // 3) Validate phone and password
            val valid = repo.login(userId, phone, password)
            if (!valid) {
                phoneError = true
                _loginResult.emit(LoginResult.InvalidCredentials)
            } else {
                _loginResult.emit(LoginResult.Success)
            }
        }
    }
}