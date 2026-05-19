package com.example.livent.presentation.screens.explore

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.livent.domain.model.AppSession
import com.example.livent.presentation.components.GuestSessionGate
import com.example.livent.presentation.components.LiventBottomBar
import com.example.livent.presentation.components.MainTab

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

        Scaffold(
            bottomBar = {
                LiventBottomBar(
                    selectedTab = selectedTab,
                    onTabSelected = { tab ->
                        when (tab) {
                            MainTab.Home -> selectedTab = MainTab.Home
                            MainTab.Favorites,
                            MainTab.Profile,
                            -> onProtectedAction { }
                        }
                    },
                )
            },
        ) { padding ->
            ExploreHomeContent(
                title = "Explorar eventos",
                showFavoriteAction = true,
                onFavoriteClick = { onProtectedAction { } },
                modifier = Modifier.padding(padding),
            )
        }
    }
}
