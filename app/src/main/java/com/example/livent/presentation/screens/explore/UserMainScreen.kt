package com.example.livent.presentation.screens.explore

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.livent.presentation.components.LiventBottomBar
import com.example.livent.presentation.components.MainTab
import com.example.livent.presentation.screens.favorites.FavoritesScreen
import com.example.livent.presentation.screens.profile.ProfileScreen

@Composable
fun UserMainScreen(
    onSignOut: () -> Unit,
) {
    var selectedTab by rememberSaveable { mutableStateOf(MainTab.Home) }

    Scaffold(
        bottomBar = {
            LiventBottomBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when (selectedTab) {
                MainTab.Home -> ExploreHomeContent(
                    title = "Descubrir eventos",
                    showFavoriteAction = true,
                    onFavoriteClick = { },
                )
                MainTab.Favorites -> FavoritesScreen()
                MainTab.Profile -> ProfileScreen(onSignOut = onSignOut)
            }
        }
    }
}
