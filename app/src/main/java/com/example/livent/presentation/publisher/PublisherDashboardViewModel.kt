package com.example.livent.presentation.publisher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.livent.domain.model.AppSession
import com.example.livent.domain.model.Event
import com.example.livent.domain.model.EventStatus
import com.example.livent.domain.model.SubscriptionTier
import com.example.livent.domain.repository.AuthRepository
import com.example.livent.domain.repository.EventRepository
import com.example.livent.domain.repository.FavoriteRepository
import com.example.livent.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PublisherDashboardUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val events: List<Event> = emptyList(),
    val subscriptionTier: SubscriptionTier = SubscriptionTier.FREE,
    val activeCount: Int = 0,
    val pastCount: Int = 0,
    val draftCount: Int = 0,
    val totalFavorites: Int = 0,
    val eventPendingDelete: Event? = null,
    val isDeleting: Boolean = false,
    val eventPendingBoost: Event? = null,
)

@HiltViewModel
class PublisherDashboardViewModel @Inject constructor(
    private val eventRepository: EventRepository,
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthRepository,
    private val favoriteRepository: FavoriteRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PublisherDashboardUiState())
    val uiState: StateFlow<PublisherDashboardUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val session = authRepository.currentSession()
            val tier = if (session is AppSession.Authenticated) {
                profileRepository.refreshProfile(session.userId)?.subscriptionTier
                    ?: SubscriptionTier.FREE
            } else {
                SubscriptionTier.FREE
            }
            val events = eventRepository.getMyEvents()
            val eventIds = events.map { it.id }
            val totalFavorites = favoriteRepository.countFavoritesForEventIds(eventIds)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    events = events,
                    subscriptionTier = tier,
                    activeCount = events.count { e -> e.status == EventStatus.ACTIVE },
                    pastCount = events.count { e -> e.status == EventStatus.PAST },
                    draftCount = events.count { e -> e.status == EventStatus.DRAFT },
                    totalFavorites = totalFavorites,
                )
            }
        }
    }

    fun requestDelete(event: Event) {
        _uiState.update { it.copy(eventPendingDelete = event) }
    }

    fun dismissDeleteDialog() {
        _uiState.update { it.copy(eventPendingDelete = null) }
    }

    fun confirmDelete() {
        val event = _uiState.value.eventPendingDelete ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true) }
            eventRepository.deleteEvent(event.id)
                .onSuccess {
                    _uiState.update { it.copy(eventPendingDelete = null, isDeleting = false) }
                    refresh()
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isDeleting = false,
                            eventPendingDelete = null,
                            errorMessage = e.message ?: "No se pudo eliminar el evento.",
                        )
                    }
                }
        }
    }

    fun markAsPast(event: Event) {
        viewModelScope.launch {
            updateStatus(event, EventStatus.PAST)
        }
    }

    fun unpublishToDraft(event: Event) {
        viewModelScope.launch {
            updateStatus(event, EventStatus.DRAFT)
        }
    }

    private suspend fun updateStatus(event: Event, status: EventStatus) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        eventRepository.updateEvent(
            id = event.id,
            title = event.title,
            artist = event.artist,
            description = event.description,
            location = event.location,
            startsAt = event.startsAt,
            posterUrl = event.posterUrl,
            status = status,
            isFeatured = event.isFeatured,
        ).onSuccess { refresh() }
            .onFailure { e ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message,
                    )
                }
            }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun requestBoost(event: Event) {
        _uiState.update { it.copy(eventPendingBoost = event) }
    }

    fun dismissBoostDialog() {
        _uiState.update { it.copy(eventPendingBoost = null) }
    }
}
