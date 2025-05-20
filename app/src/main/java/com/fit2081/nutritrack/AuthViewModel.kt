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

sealed class LoginResult {
    object Success : LoginResult()
    object IncompleteRegistration : LoginResult()
    object InvalidCredentials : LoginResult()
}

class AuthViewModel(
    private val repo: AuthRepository
) : ViewModel() {
    private val _userIds = MutableStateFlow<List<String>>(emptyList())
    val userIds = _userIds.asStateFlow()

    var userId by mutableStateOf("")
        private set
    var phone by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set
    var userIdError by mutableStateOf(false)
        private set
    var phoneError by mutableStateOf(false)
        private set
    var registrationIncomplete by mutableStateOf(false)
        private set

    private val _loginResult = MutableSharedFlow<LoginResult>()
    val loginResult: SharedFlow<LoginResult> = _loginResult.asSharedFlow()

    init {
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

    fun onLoginClicked() {
        viewModelScope.launch {
            userIdError = false
            phoneError = false
            registrationIncomplete = false

            val existsId = repo.isUserRegistered(userId)
            if (!existsId) {
                userIdError = true
                _loginResult.emit(LoginResult.InvalidCredentials)
                return@launch
            }

            val complete = repo.isSelfRegistered(userId)
            if (!complete) {
                registrationIncomplete = true
                _loginResult.emit(LoginResult.IncompleteRegistration)
                return@launch
            }

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
