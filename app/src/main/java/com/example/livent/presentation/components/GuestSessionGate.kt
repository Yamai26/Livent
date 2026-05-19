package com.example.livent.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.example.livent.domain.model.AppSession

@Composable
fun GuestSessionGate(
    session: AppSession,
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit,
    content: @Composable (onProtectedAction: (() -> Unit) -> Unit) -> Unit,
) {
    var showAuthSheet by rememberSaveable { mutableStateOf(false) }

    val onProtectedAction: (() -> Unit) -> Unit = { action ->
        if (session is AppSession.Guest) {
            showAuthSheet = true
        } else {
            action()
        }
    }

    content(onProtectedAction)

    if (showAuthSheet) {
        AuthRequiredBottomSheet(
            onDismiss = { showAuthSheet = false },
            onLogin = {
                showAuthSheet = false
                onNavigateToLogin()
            },
            onRegister = {
                showAuthSheet = false
                onNavigateToRegister()
            },
        )
    }
}
