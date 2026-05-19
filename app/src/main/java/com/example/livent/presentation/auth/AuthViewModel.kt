package com.example.livent.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.livent.domain.model.AppSession
import com.example.livent.domain.model.UserRole
import com.example.livent.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successSession: AppSession.Authenticated? = null,
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            authRepository.signIn(email, password)
                .onSuccess { session ->
                    if (session is AppSession.Authenticated) {
                        _uiState.value = AuthUiState(successSession = session)
                    } else {
                        _uiState.value = AuthUiState(errorMessage = "No se pudo iniciar sesión.")
                    }
                }
                .onFailure { e ->
                    _uiState.value = AuthUiState(errorMessage = e.message ?: "Error al iniciar sesión.")
                }
        }
    }

    fun signUp(
        email: String,
        password: String,
        role: UserRole,
        displayName: String?,
    ) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            authRepository.signUp(email, password, role, displayName)
                .onSuccess { session ->
                    if (session is AppSession.Authenticated) {
                        _uiState.value = AuthUiState(successSession = session)
                    } else {
                        _uiState.value = AuthUiState(
                            errorMessage = "Registro completado. Confirma tu email o inicia sesión.",
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.value = AuthUiState(errorMessage = e.message ?: "Error al registrarse.")
                }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun consumeSuccess() {
        _uiState.value = _uiState.value.copy(successSession = null)
    }
}
