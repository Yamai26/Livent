package com.example.livent.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.livent.presentation.explore.ExploreViewModel
import com.example.livent.presentation.screens.explore.ExploreHomeScreen

/**
 * Feed de exploración (carrusel + grid). El detalle se navega en el NavHost padre
 * (GuestMainScreen / UserMainScreen) — ver docs/fase5/EXPLORE_FLOW.md.
 */
@Composable
fun ExploreNavHost(
    favoritesEnabled: Boolean,
    onEventClick: (String) -> Unit,
    onFavoriteNeedsAuth: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val exploreViewModel: ExploreViewModel = hiltViewModel()
    val exploreState by exploreViewModel.uiState.collectAsStateWithLifecycle()

    ExploreHomeScreen(
        uiState = exploreState,
        onEventClick = onEventClick,
        onFavoriteClick = { eventId ->
            if (favoritesEnabled) {
                exploreViewModel.toggleFavorite(eventId)
            } else {
                onFavoriteNeedsAuth()
            }
        },
        onRetry = exploreViewModel::loadFeed,
        onFavoriteErrorShown = exploreViewModel::clearError,
        modifier = modifier,
    )
}
