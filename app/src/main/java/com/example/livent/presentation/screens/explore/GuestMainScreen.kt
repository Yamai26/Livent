package com.example.livent.presentation.screens.explore

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.livent.domain.model.AppSession
import com.example.livent.presentation.components.GuestSessionGate
import com.example.livent.presentation.components.LiventBottomBar
import com.example.livent.presentation.components.MainTab
import com.example.livent.presentation.explore.EventDetailViewModel
import com.example.livent.presentation.navigation.ExploreNavHost
import com.example.livent.presentation.navigation.ExploreRoute

private const val GUEST_HOME = "guest_home"

@Composable
fun GuestMainScreen(
    session: AppSession,
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit,
) {
    GuestSessionGate(
        session = session,
        onNavigateToLogin = onNavigateToLogin,
        onNavigateToRegister = onNavigateToRegister,
    ) { onProtectedAction ->
        var selectedTab by rememberSaveable { mutableStateOf(MainTab.Home) }
        val navController = rememberNavController()

        Scaffold(
            bottomBar = {
                LiventBottomBar(
                    selectedTab = selectedTab,
                    onTabSelected = { tab ->
                        when (tab) {
                            MainTab.Home -> {
                                selectedTab = MainTab.Home
                                navController.navigate(GUEST_HOME) {
                                    launchSingleTop = true
                                    popUpTo(GUEST_HOME) { inclusive = false }
                                }
                            }
                            MainTab.Favorites,
                            MainTab.Profile,
                            -> onProtectedAction { }
                        }
                    },
                )
            },
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = GUEST_HOME,
                modifier = Modifier.padding(padding),
            ) {
                composable(GUEST_HOME) {
                    ExploreNavHost(
                        favoritesEnabled = false,
                        onEventClick = { eventId ->
                            navController.navigate(ExploreRoute.EventDetail.create(eventId))
                        },
                        onFavoriteNeedsAuth = { onProtectedAction { } },
                    )
                }
                composable(
                    route = ExploreRoute.EventDetail.path,
                    arguments = listOf(
                        navArgument(ExploreRoute.EventDetail.ARG_EVENT_ID) {
                            type = NavType.StringType
                        },
                    ),
                ) { backStackEntry ->
                    val detailViewModel: EventDetailViewModel = hiltViewModel(backStackEntry)
                    val detailState by detailViewModel.uiState.collectAsStateWithLifecycle()

                    LaunchedEffect(Unit) {
                        detailViewModel.loadEvent(favoritesEnabled = false)
                    }

                    EventDetailScreen(
                        uiState = detailState,
                        onBack = { navController.popBackStack() },
                        onToggleFavorite = { onProtectedAction { } },
                    )
                }
            }
        }
    }
}
