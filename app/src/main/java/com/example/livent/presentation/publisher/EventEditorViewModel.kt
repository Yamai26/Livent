package com.example.livent.presentation.publisher

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.livent.data.remote.PosterStorageDataSource
import com.example.livent.domain.model.AppSession
import com.example.livent.domain.model.EventStatus
import com.example.livent.domain.model.SubscriptionTier
import com.example.livent.domain.repository.AuthRepository
import com.example.livent.domain.repository.EventRepository
import com.example.livent.domain.repository.ProfileRepository
import com.example.livent.presentation.navigation.PublisherRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

data class EventEditorUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val wizardStep: Int = 1,
    val eventId: String? = null,
    val posterPreviewUri: Uri? = null,
    val existingPosterUrl: String? = null,
    val title: String = "",
    val artist: String = "",
    val description: String = "",
    val location: String = "",
    val startsAtMillis: Long? = null,
    val showFreePlanSheet: Boolean = false,
    val saveSuccess: Boolean = false,
    val subscriptionTier: SubscriptionTier = SubscriptionTier.FREE,
)

@HiltViewModel
class EventEditorViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle,
    private val eventRepository: EventRepository,
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthRepository,
    private val posterStorage: PosterStorageDataSource,
) : ViewModel() {

    private val _uiState = MutableStateFlow(EventEditorUiState())
    val uiState: StateFlow<EventEditorUiState> = _uiState.asStateFlow()

    private var posterBytes: ByteArray? = null
    private var posterContentType: String? = null
    private var posterExtension: String? = null
    private var existingWasActive: Boolean = false
    private var existingIsFeatured: Boolean = false

    init {
        val eventId = savedStateHandle.get<String>(PublisherRoute.EventWizardEdit.ARG_EVENT_ID)
            ?.takeIf { it.isNotBlank() }
        _uiState.update { it.copy(eventId = eventId) }
        loadProfileTier()
        if (eventId != null) {
            loadEvent(eventId)
        }
    }

    fun reloadProfileTier() {
        viewModelScope.launch {
            val session = authRepository.currentSession()
            if (session is AppSession.Authenticated) {
                val tier = profileRepository.refreshProfile(session.userId)?.subscriptionTier
                    ?: SubscriptionTier.FREE
                _uiState.update { it.copy(subscriptionTier = tier, showFreePlanSheet = false) }
            }
        }
    }

    private fun loadProfileTier() {
        reloadProfileTier()
    }

    private fun loadEvent(eventId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val event = eventRepository.getEvent(eventId)
            if (event == null) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Evento no encontrado.")
                }
                return@launch
            }
            existingWasActive = event.status == EventStatus.ACTIVE
            existingIsFeatured = event.isFeatured
            val millis = runCatching { Instant.parse(event.startsAt).toEpochMilli() }.getOrNull()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    title = event.title,
                    artist = event.artist,
                    description = event.description,
                    location = event.location,
                    startsAtMillis = millis,
                    existingPosterUrl = event.posterUrl,
                )
            }
        }
    }

    fun onPosterPicked(uri: Uri) {
        viewModelScope.launch {
            val resolver = context.contentResolver
            val bytes = resolver.openInputStream(uri)?.use { stream -> stream.readBytes() }
                ?: return@launch
            val mime = resolver.getType(uri) ?: "image/jpeg"
            val ext = when {
                mime.contains("png", ignoreCase = true) -> "png"
                mime.contains("webp", ignoreCase = true) -> "webp"
                else -> "jpg"
            }
            posterBytes = bytes
            posterContentType = mime
            posterExtension = ext
            _uiState.update {
                it.copy(posterPreviewUri = uri, existingPosterUrl = null)
            }
        }
    }

    fun nextStep() {
        val step = _uiState.value.wizardStep
        if (step < 3) {
            _uiState.update { it.copy(wizardStep = step + 1, errorMessage = null) }
        }
    }

    fun previousStep() {
        val step = _uiState.value.wizardStep
        if (step > 1) {
            _uiState.update { it.copy(wizardStep = step - 1, errorMessage = null) }
        }
    }

    fun updateTitle(value: String) = _uiState.update { it.copy(title = value) }
    fun updateArtist(value: String) = _uiState.update { it.copy(artist = value) }
    fun updateDescription(value: String) = _uiState.update { it.copy(description = value) }
    fun updateLocation(value: String) = _uiState.update { it.copy(location = value) }
    fun updateStartsAtMillis(millis: Long) = _uiState.update { it.copy(startsAtMillis = millis) }

    fun dismissFreePlanSheet() {
        _uiState.update { it.copy(showFreePlanSheet = false) }
    }

    fun saveDraft() {
        val error = validateForSave(_uiState.value)
        if (error != null) {
            _uiState.update { it.copy(errorMessage = error) }
            return
        }
        persist(EventStatus.DRAFT, skipFreeCheck = true)
    }

    fun publish() {
        viewModelScope.launch {
            if (!canPublishActive()) {
                _uiState.update { it.copy(showFreePlanSheet = true) }
                return@launch
            }
            persist(EventStatus.ACTIVE, skipFreeCheck = true)
        }
    }

    private suspend fun canPublishActive(): Boolean {
        val state = _uiState.value
        if (state.subscriptionTier == SubscriptionTier.PREMIUM) return true
        if (existingWasActive) return true
        val activeCount = eventRepository.countMyActiveEvents()
        return activeCount < 1
    }

    private fun persist(status: EventStatus, skipFreeCheck: Boolean) {
        val state = _uiState.value
        val validationError = validateForSave(state)
        if (validationError != null) {
            _uiState.update { it.copy(errorMessage = validationError) }
            return
        }
        if (status == EventStatus.ACTIVE && !skipFreeCheck) {
            viewModelScope.launch {
                if (!canPublishActive()) {
                    _uiState.update { it.copy(showFreePlanSheet = true) }
                } else {
                    persist(status, skipFreeCheck = true)
                }
            }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            val session = authRepository.currentSession()
            if (session !is AppSession.Authenticated) {
                _uiState.update {
                    it.copy(isSaving = false, errorMessage = "Sesión no válida.")
                }
                return@launch
            }
            val startsAt = Instant.ofEpochMilli(state.startsAtMillis!!).toString()
            var posterUrl = state.existingPosterUrl
            val bytes = posterBytes
            if (bytes != null && posterContentType != null && posterExtension != null) {
                posterStorage.uploadPoster(
                    userId = session.userId,
                    bytes = bytes,
                    contentType = posterContentType!!,
                    extension = posterExtension!!,
                ).onSuccess { url -> posterUrl = url }
                    .onFailure { e ->
                        _uiState.update {
                            it.copy(
                                isSaving = false,
                                errorMessage = e.message ?: "Error al subir el cartel.",
                            )
                        }
                        return@launch
                    }
            }
            val result = if (state.eventId == null) {
                eventRepository.createEvent(
                    title = state.title,
                    artist = state.artist,
                    description = state.description,
                    location = state.location,
                    startsAt = startsAt,
                    posterUrl = posterUrl,
                    status = status,
                )
            } else {
                eventRepository.updateEvent(
                    id = state.eventId,
                    title = state.title,
                    artist = state.artist,
                    description = state.description,
                    location = state.location,
                    startsAt = startsAt,
                    posterUrl = posterUrl,
                    status = status,
                    isFeatured = existingIsFeatured,
                )
            }
            result
                .onSuccess {
                    _uiState.update {
                        it.copy(isSaving = false, saveSuccess = true)
                    }
                }
                .onFailure { e ->
                    val message = e.message ?: "Error al guardar."
                    if (message.contains("plan gratuito", ignoreCase = true)) {
                        _uiState.update {
                            it.copy(isSaving = false, showFreePlanSheet = true)
                        }
                    } else {
                        _uiState.update {
                            it.copy(isSaving = false, errorMessage = message)
                        }
                    }
                }
        }
    }

    fun consumeSaveSuccess() {
        _uiState.update { it.copy(saveSuccess = false) }
    }

    private fun validateForSave(state: EventEditorUiState): String? {
        if (state.title.trim().isEmpty()) return "El título es obligatorio."
        if (state.startsAtMillis == null) return "Selecciona fecha y hora."
        if (state.location.trim().isEmpty()) return "La ubicación es obligatoria."
        return null
    }
}
