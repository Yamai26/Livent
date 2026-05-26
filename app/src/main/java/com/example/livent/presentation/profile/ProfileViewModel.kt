package com.example.livent.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.livent.domain.model.AppSession
import com.example.livent.domain.repository.AuthRepository
import com.example.livent.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val isLoading: Boolean = false,
    val displayName: String? = null,
    val email: String? = null,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val session = authRepository.currentSession()) {
                is AppSession.Authenticated -> {
                    val profile = profileRepository.getProfile(session.userId)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            displayName = profile?.displayName,
                            email = profile?.email,
                        )
                    }
                }
                AppSession.Guest -> {
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        }
    }
}
