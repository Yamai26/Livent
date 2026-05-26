package com.example.livent.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.livent.presentation.theme.LiventPrimary

enum class MainTab {
    Home,
    Favorites,
    Profile,
}

@Composable
fun LiventBottomBar(
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        MainTab.entries.forEach { tab ->
            val selected = tab == selectedTab
            NavigationBarItem(
                selected = selected,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                        contentDescription = tab.label,
                    )
                },
                label = { Text(tab.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = LiventPrimary,
                    selectedTextColor = LiventPrimary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.surface,
                ),
            )
        }
    }
}

private val MainTab.selectedIcon: ImageVector
    get() = when (this) {
        MainTab.Home -> Icons.Filled.Home
        MainTab.Favorites -> Icons.Filled.Favorite
        MainTab.Profile -> Icons.Filled.Person
    }

private val MainTab.unselectedIcon: ImageVector
    get() = when (this) {
        MainTab.Home -> Icons.Outlined.Home
        MainTab.Favorites -> Icons.Outlined.FavoriteBorder
        MainTab.Profile -> Icons.Outlined.Person
    }

private val MainTab.label: String
    get() = when (this) {
        MainTab.Home -> "Inicio"
        MainTab.Favorites -> "Favoritos"
        MainTab.Profile -> "Perfil"
    }
