package com.example.livent.presentation.explore

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

data class ExploreUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val featuredEvents: List<Event> = emptyList(),
    val upcomingEvents: List<Event> = emptyList(),
    val favoriteIds: Set<String> = emptySet(),
    val isTogglingFavorite: Boolean = false,
    val favoriteErrorMessage: String? = null,
)

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val exploreRepository: ExploreRepository,
    private val favoriteRepository: FavoriteRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExploreUiState())
    val uiState: StateFlow<ExploreUiState> = _uiState.asStateFlow()

    init {
        loadFeed()
        viewModelScope.launch {
            favoriteRepository.refreshFavoriteIds()
            favoriteRepository.observeFavoriteIds().collect { ids ->
                _uiState.update { it.copy(favoriteIds = ids) }
            }
        }
    }

    fun loadFeed() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val featured = exploreRepository.getFeaturedActiveEvents()
            val upcoming = exploreRepository.getUpcomingActiveEvents()
            if (featured.isEmpty() && upcoming.isEmpty()) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        featuredEvents = featured,
                        upcomingEvents = upcoming,
                        errorMessage = null,
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        featuredEvents = featured,
                        upcomingEvents = upcoming,
                    )
                }
            }
        }
    }

    fun toggleFavorite(eventId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isTogglingFavorite = true, favoriteErrorMessage = null) }
            val result = if (eventId in _uiState.value.favoriteIds) {
                favoriteRepository.removeFavorite(eventId)
            } else {
                favoriteRepository.addFavorite(eventId)
            }
            result.onFailure { e ->
                _uiState.update {
                    it.copy(
                        isTogglingFavorite = false,
                        favoriteErrorMessage = e.message ?: "No se pudo actualizar el favorito.",
                    )
                }
            }
            if (result.isSuccess) {
                _uiState.update { it.copy(isTogglingFavorite = false) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null, favoriteErrorMessage = null) }
    }
}
