package com.fit2081.nutritrack

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.fit2081.nutritrack.data.AuthManager
import com.fit2081.nutritrack.data.Entity.Patient
import com.fit2081.nutritrack.data.Repo.AuthRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SettingsUiState(
    val isLoggedIn: Boolean = false,
    val currentUserId: String? = null,
    val userName: String = "",
    val phoneNumber: String = "",
    val isLoading: Boolean = true,
    val error: String? = null
)

class SettingsViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val currentUserId = AuthManager.currentUserId()

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
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Unknown error occurred"
                    )
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                AuthManager.signOut()
                _uiState.update {
                    SettingsUiState(
                        isLoggedIn = false,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = "Failed to logout: ${e.message}"
                    )
                }
            }
        }
    }

    fun refreshProfile() {
        loadUserProfile()
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

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