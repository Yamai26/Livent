package com.example.livent.presentation.navigation

sealed class UserTabRoute(val path: String) {
    data object Home : UserTabRoute("user_tab_home")
    data object Favorites : UserTabRoute("user_tab_favorites")
    data object Profile : UserTabRoute("user_tab_profile")
}
