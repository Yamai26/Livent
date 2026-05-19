package com.example.livent.presentation.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.livent.domain.model.AppSession
import com.example.livent.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    val sessionState: StateFlow<AppSession> = authRepository.observeSession()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AppSession.Guest,
        )

    private val _guestExploreUnlocked = MutableStateFlow(false)
    val guestExploreUnlocked: StateFlow<Boolean> = _guestExploreUnlocked.asStateFlow()

    private val _isInitializing = MutableStateFlow(true)
    val isInitializing: StateFlow<Boolean> = _isInitializing.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.refreshSessionOnStart()
            _isInitializing.value = false
        }
    }

    fun unlockGuestExplore() {
        _guestExploreUnlocked.value = true
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            _guestExploreUnlocked.value = false
        }
    }
}
