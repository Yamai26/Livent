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
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.livent.presentation.components.LiventBottomBar
import com.example.livent.presentation.components.MainTab
import com.example.livent.presentation.explore.EventDetailViewModel
import com.example.livent.presentation.navigation.ExploreNavHost
import com.example.livent.presentation.navigation.ExploreRoute
import com.example.livent.presentation.navigation.UserTabRoute
import com.example.livent.presentation.screens.favorites.FavoritesScreen
import com.example.livent.presentation.screens.profile.ProfileScreen

@Composable
fun UserMainScreen(
    onSignOut: () -> Unit,
) {
    var selectedTab by rememberSaveable { mutableStateOf(MainTab.Home) }
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            LiventBottomBar(
                selectedTab = selectedTab,
                onTabSelected = { tab ->
                    selectedTab = tab
                    val route = when (tab) {
                        MainTab.Home -> UserTabRoute.Home.path
                        MainTab.Favorites -> UserTabRoute.Favorites.path
                        MainTab.Profile -> UserTabRoute.Profile.path
                    }
                    navController.navigate(route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
            )
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = UserTabRoute.Home.path,
            modifier = Modifier.padding(padding),
        ) {
            composable(UserTabRoute.Home.path) {
                ExploreNavHost(
                    favoritesEnabled = true,
                    onEventClick = { eventId ->
                        navController.navigate(ExploreRoute.EventDetail.create(eventId))
                    },
                    onFavoriteNeedsAuth = { },
                )
            }
            composable(UserTabRoute.Favorites.path) {
                FavoritesScreen(
                    onEventClick = { eventId ->
                        navController.navigate(ExploreRoute.EventDetail.create(eventId))
                    },
                )
            }
            composable(UserTabRoute.Profile.path) {
                ProfileScreen(onSignOut = onSignOut)
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
                    detailViewModel.loadEvent(favoritesEnabled = true)
                }

                EventDetailScreen(
                    uiState = detailState,
                    onBack = { navController.popBackStack() },
                    onToggleFavorite = { detailViewModel.toggleFavorite(onNeedsAuth = {}) },
                )
            }
        }
    }
}
