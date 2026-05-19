package com.example.livent.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.livent.domain.model.AppSession
import com.example.livent.domain.model.UserRole
import com.example.livent.presentation.screens.auth.LoginScreen
import com.example.livent.presentation.screens.auth.RegisterScreen
import com.example.livent.presentation.screens.explore.GuestMainScreen
import com.example.livent.presentation.screens.explore.UserMainScreen
import com.example.livent.presentation.screens.publisher.PublisherMainScreen
import com.example.livent.presentation.screens.welcome.WelcomeScreen
import com.example.livent.presentation.session.SessionViewModel

@Composable
fun RootNavHost(
    modifier: Modifier = Modifier,
    sessionViewModel: SessionViewModel = hiltViewModel(),
) {
    val session by sessionViewModel.sessionState.collectAsStateWithLifecycle()
    val guestExplore by sessionViewModel.guestExploreUnlocked.collectAsStateWithLifecycle()
    val isInitializing by sessionViewModel.isInitializing.collectAsStateWithLifecycle()
    val navController = rememberNavController()

    LaunchedEffect(session, guestExplore, isInitializing) {
        if (isInitializing) return@LaunchedEffect
        val target = resolveAutoNavigateTarget(session, guestExplore) ?: return@LaunchedEffect
        val current = navController.currentDestination?.route
        if (current != target) {
            navController.navigate(target) {
                popUpTo(Route.Welcome.path) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    if (isInitializing) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    NavHost(
        navController = navController,
        startDestination = Route.Welcome.path,
        modifier = modifier,
    ) {
        composable(Route.Welcome.path) {
            WelcomeScreen(
                onSignUp = { navController.navigate(Route.Register.path) },
                onLogIn = { navController.navigate(Route.Login.path) },
                onBrowseFirst = {
                    sessionViewModel.unlockGuestExplore()
                    navController.navigate(Route.GuestMain.path) {
                        popUpTo(Route.Welcome.path) { inclusive = true }
                    }
                },
            )
        }
        composable(Route.Login.path) {
            LoginScreen(
                onBack = { navController.popBackStack() },
                onNavigateToRegister = {
                    navController.navigate(Route.Register.path) {
                        popUpTo(Route.Login.path) { inclusive = true }
                    }
                },
                onAuthSuccess = { /* session Flow drives navigation */ },
            )
        }
        composable(Route.Register.path) {
            RegisterScreen(
                onBack = { navController.popBackStack() },
                onNavigateToLogin = {
                    navController.navigate(Route.Login.path) {
                        popUpTo(Route.Register.path) { inclusive = true }
                    }
                },
                onAuthSuccess = { /* session Flow drives navigation */ },
            )
        }
        composable(Route.GuestMain.path) {
            GuestMainScreen(
                session = session,
                onNavigateToLogin = {
                    navController.navigate(Route.Login.path)
                },
                onNavigateToRegister = {
                    navController.navigate(Route.Register.path)
                },
            )
        }
        composable(Route.UserMain.path) {
            UserMainScreen(
                onSignOut = {
                    sessionViewModel.signOut()
                    navController.navigate(Route.Welcome.path) {
                        popUpTo(0) { inclusive = true }
                    }
                },
            )
        }
        composable(Route.PublisherMain.path) {
            PublisherMainScreen(
                onSignOut = {
                    sessionViewModel.signOut()
                    navController.navigate(Route.Welcome.path) {
                        popUpTo(0) { inclusive = true }
                    }
                },
            )
        }
    }
}

/** Auto-redirect for persisted auth or guest explore; null keeps auth sub-routes reachable. */
private fun resolveAutoNavigateTarget(
    session: AppSession,
    guestExplore: Boolean,
): String? = when (session) {
    is AppSession.Authenticated -> when (session.role) {
        UserRole.USER -> Route.UserMain.path
        UserRole.PUBLISHER -> Route.PublisherMain.path
    }
    AppSession.Guest -> if (guestExplore) Route.GuestMain.path else null
}
