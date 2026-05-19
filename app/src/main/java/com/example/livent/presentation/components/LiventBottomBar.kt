package com.example.livent.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
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
    NavigationBar {
        MainTab.entries.forEach { tab ->
            val selected = tab == selectedTab
            NavigationBarItem(
                selected = selected,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.label,
                    )
                },
                label = { Text(tab.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = LiventPrimary,
                    selectedTextColor = LiventPrimary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                ),
            )
        }
    }
}

private val MainTab.icon: ImageVector
    get() = when (this) {
        MainTab.Home -> Icons.Default.Home
        MainTab.Favorites -> Icons.Default.Favorite
        MainTab.Profile -> Icons.Default.Person
    }

private val MainTab.label: String
    get() = when (this) {
        MainTab.Home -> "Inicio"
        MainTab.Favorites -> "Favoritos"
        MainTab.Profile -> "Perfil"
    }
