package com.example.livent.presentation.explore

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.livent.domain.model.Event
import com.example.livent.domain.repository.ExploreRepository
import com.example.livent.domain.repository.FavoriteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EventDetailUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val event: Event? = null,
    val isFavorite: Boolean = false,
    val favoritesEnabled: Boolean = false,
    val isTogglingFavorite: Boolean = false,
)

@HiltViewModel
class EventDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val exploreRepository: ExploreRepository,
    private val favoriteRepository: FavoriteRepository,
) : ViewModel() {

    private val eventId: String = checkNotNull(savedStateHandle["eventId"])

    private val _uiState = MutableStateFlow(EventDetailUiState())
    val uiState: StateFlow<EventDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            favoriteRepository.observeFavoriteIds().collect { ids ->
                _uiState.update { state ->
                    state.copy(isFavorite = eventId in ids)
                }
            }
        }
    }

    fun loadEvent(favoritesEnabled: Boolean) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true, errorMessage = null, favoritesEnabled = favoritesEnabled)
            }
            val event = exploreRepository.getActiveEvent(eventId)
            val isFavorite = if (favoritesEnabled && event != null) {
                favoriteRepository.isFavorite(event.id)
            } else {
                false
            }
            _uiState.update {
                it.copy(
                    isLoading = false,
                    event = event,
                    isFavorite = isFavorite,
                    errorMessage = if (event == null) "Evento no encontrado o no disponible." else null,
                )
            }
        }
    }

    fun toggleFavorite(onNeedsAuth: () -> Unit) {
        if (!_uiState.value.favoritesEnabled) {
            onNeedsAuth()
            return
        }
        val event = _uiState.value.event ?: return
        val wasFavorite = _uiState.value.isFavorite
        viewModelScope.launch {
            _uiState.update {
                it.copy(isTogglingFavorite = true, isFavorite = !wasFavorite)
            }
            val result = if (wasFavorite) {
                favoriteRepository.removeFavorite(event.id)
            } else {
                favoriteRepository.addFavorite(event.id)
            }
            result.onSuccess {
                _uiState.update { it.copy(isTogglingFavorite = false) }
            }.onFailure { e ->
                _uiState.update {
                    it.copy(
                        isTogglingFavorite = false,
                        isFavorite = wasFavorite,
                        errorMessage = e.message ?: "No se pudo actualizar el favorito.",
                    )
                }
            }
        }
    }
}
