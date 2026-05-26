package com.example.livent.presentation.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.livent.domain.model.Event
import com.example.livent.domain.repository.FavoriteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FavoritesUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val events: List<Event> = emptyList(),
    val eventPendingRemove: Event? = null,
    val isRemoving: Boolean = false,
)

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val favoriteRepository: FavoriteRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    init {
        refresh()
        viewModelScope.launch {
            favoriteRepository.observeFavoriteIds().collect {
                refresh()
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val events = favoriteRepository.getMyFavoriteEvents()
            _uiState.update { it.copy(isLoading = false, events = events) }
        }
    }

    fun requestRemove(event: Event) {
        _uiState.update { it.copy(eventPendingRemove = event) }
    }

    fun dismissRemoveDialog() {
        _uiState.update { it.copy(eventPendingRemove = null) }
    }

    fun confirmRemove() {
        val event = _uiState.value.eventPendingRemove ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isRemoving = true) }
            favoriteRepository.removeFavorite(event.id)
                .onSuccess {
                    _uiState.update { it.copy(eventPendingRemove = null, isRemoving = false) }
                    refresh()
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isRemoving = false,
                            eventPendingRemove = null,
                            errorMessage = e.message ?: "No se pudo eliminar el favorito.",
                        )
                    }
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
