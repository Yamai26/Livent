package com.example.livent.presentation.publisher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.livent.domain.model.AppSession
import com.example.livent.domain.repository.AuthRepository
import com.example.livent.domain.repository.EventRepository
import com.example.livent.domain.repository.PaymentRepository
import com.example.livent.domain.repository.ProfileRepository
import com.stripe.android.paymentsheet.PaymentSheetResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class PaymentKind {
    PREMIUM,
    BOOST,
}

data class PaymentUiState(
    val isLoading: Boolean = false,
    val pendingClientSecret: String? = null,
    val successMessage: String? = null,
    val errorMessage: String? = null,
)

@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val paymentRepository: PaymentRepository,
    private val profileRepository: ProfileRepository,
    private val eventRepository: EventRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaymentUiState())
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()

    private val _paymentCompleted = MutableSharedFlow<PaymentKind>(extraBufferCapacity = 1)
    val paymentCompleted: SharedFlow<PaymentKind> = _paymentCompleted.asSharedFlow()

    private var activeKind: PaymentKind? = null

    fun startPremiumPayment() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true, errorMessage = null, successMessage = null)
            }
            activeKind = PaymentKind.PREMIUM
            paymentRepository.createPremiumPayment()
                .onSuccess { data ->
                    _uiState.update {
                        it.copy(isLoading = false, pendingClientSecret = data.clientSecret)
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = e.message ?: "No se pudo iniciar el pago Premium.",
                        )
                    }
                }
        }
    }

    fun startBoostPayment(eventId: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true, errorMessage = null, successMessage = null)
            }
            activeKind = PaymentKind.BOOST
            paymentRepository.createBoostPayment(eventId)
                .onSuccess { data ->
                    _uiState.update {
                        it.copy(isLoading = false, pendingClientSecret = data.clientSecret)
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = e.message ?: "No se pudo iniciar el pago Boost.",
                        )
                    }
                }
        }
    }

    fun onClientSecretConsumed() {
        _uiState.update { it.copy(pendingClientSecret = null) }
    }

    fun onPaymentSheetResult(result: PaymentSheetResult) {
        val kind = activeKind
        when (result) {
            is PaymentSheetResult.Completed -> {
                viewModelScope.launch {
                    refreshAfterPayment(kind)
                    val message = when (kind) {
                        PaymentKind.PREMIUM ->
                            "¡Premium activado! Ya puedes publicar varios eventos activos."
                        PaymentKind.BOOST ->
                            "¡Evento destacado! Aparecerá en el carrusel de exploración."
                        null -> "Pago completado."
                    }
                    _uiState.update {
                        it.copy(successMessage = message, errorMessage = null)
                    }
                    kind?.let { _paymentCompleted.tryEmit(it) }
                }
            }
            is PaymentSheetResult.Canceled -> {
                _uiState.update {
                    it.copy(errorMessage = null, successMessage = null)
                }
            }
            is PaymentSheetResult.Failed -> {
                _uiState.update {
                    it.copy(
                        errorMessage = result.error.localizedMessage
                            ?: "El pago no se completó.",
                    )
                }
            }
        }
        activeKind = null
    }

    private suspend fun refreshAfterPayment(kind: PaymentKind?) {
        val session = authRepository.currentSession()
        if (session is AppSession.Authenticated) {
            profileRepository.refreshProfile(session.userId)
        }
        if (kind == PaymentKind.BOOST) {
            eventRepository.getMyEvents()
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun clearSuccess() {
        _uiState.update { it.copy(successMessage = null) }
    }
}
