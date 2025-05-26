package xyz.yeems214.DuffinsBlog.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import xyz.yeems214.DuffinsBlog.data.model.User
import xyz.yeems214.DuffinsBlog.data.repository.AuthRepository

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()
    
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()
    
    init {
        checkLoginStatus()
    }
    
    fun login(username: String, password: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        
        viewModelScope.launch {
            authRepository.login(username, password)
                .onSuccess { authResponse ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        user = authResponse.user
                    )
                    _isLoggedIn.value = true
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Login failed"
                    )
                }
        }
    }
    
    fun register(username: String, email: String, password: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        
        viewModelScope.launch {
            authRepository.register(username, email, password)
                .onSuccess { authResponse ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        user = authResponse.user
                    )
                    _isLoggedIn.value = true
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Registration failed"
                    )
                }
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.value = AuthUiState()
            _isLoggedIn.value = false
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    private fun checkLoginStatus() {
        viewModelScope.launch {
            _isLoggedIn.value = authRepository.isLoggedIn()
        }
    }
}

data class AuthUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val error: String? = null
)
