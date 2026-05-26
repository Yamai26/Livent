package com.example.livent.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.livent.presentation.publisher.PaymentViewModel
import com.example.livent.presentation.publisher.PublisherDashboardViewModel
import com.example.livent.presentation.screens.publisher.EventWizardScreen
import com.example.livent.presentation.screens.publisher.PublisherDashboardScreen

@Composable
fun PublisherNavHost(
    onSignOut: () -> Unit,
    onSubscribePremium: () -> Unit,
    onBoostEvent: (String) -> Unit,
    paymentViewModel: PaymentViewModel,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = PublisherRoute.Dashboard.path,
        modifier = modifier,
    ) {
        composable(PublisherRoute.Dashboard.path) {
            val dashboardViewModel: PublisherDashboardViewModel = hiltViewModel()
            LaunchedEffect(paymentViewModel) {
                paymentViewModel.paymentCompleted.collect {
                    dashboardViewModel.refresh()
                }
            }
            PublisherDashboardScreen(
                onCreateEvent = {
                    navController.navigate(PublisherRoute.EventWizardCreate.path)
                },
                onEditEvent = { eventId ->
                    navController.navigate(PublisherRoute.EventWizardEdit.create(eventId))
                },
                onSignOut = onSignOut,
                onSubscribePremium = onSubscribePremium,
                onBoostEvent = onBoostEvent,
                paymentViewModel = paymentViewModel,
                viewModel = dashboardViewModel,
            )
        }
        composable(PublisherRoute.EventWizardCreate.path) {
            val dashboardViewModel: PublisherDashboardViewModel = hiltViewModel(
                navController.getBackStackEntry(PublisherRoute.Dashboard.path),
            )
            EventWizardScreen(
                onBack = { navController.popBackStack() },
                onSaved = {
                    dashboardViewModel.refresh()
                    navController.popBackStack(
                        PublisherRoute.Dashboard.path,
                        inclusive = false,
                    )
                },
                onSubscribePremium = onSubscribePremium,
            )
        }
        composable(
            route = PublisherRoute.EventWizardEdit.path,
            arguments = listOf(
                navArgument(PublisherRoute.EventWizardEdit.ARG_EVENT_ID) {
                    type = NavType.StringType
                },
            ),
        ) {
            val dashboardViewModel: PublisherDashboardViewModel = hiltViewModel(
                navController.getBackStackEntry(PublisherRoute.Dashboard.path),
            )
            EventWizardScreen(
                onBack = { navController.popBackStack() },
                onSaved = {
                    dashboardViewModel.refresh()
                    navController.popBackStack(
                        PublisherRoute.Dashboard.path,
                        inclusive = false,
                    )
                },
                onSubscribePremium = onSubscribePremium,
            )
        }
    }
}
