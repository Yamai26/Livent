package com.example.livent.presentation.screens.publisher

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.livent.di.PaymentEntryPoint
import com.example.livent.presentation.navigation.PublisherNavHost
import com.example.livent.presentation.publisher.PaymentViewModel
import dagger.hilt.android.EntryPointAccessors

@Composable
fun PublisherMainScreen(
    onSignOut: () -> Unit,
) {
    val context = LocalContext.current
    val paymentViewModel: PaymentViewModel = hiltViewModel()
    val paymentState by paymentViewModel.uiState.collectAsStateWithLifecycle()

    val stripeHandler = EntryPointAccessors.fromApplication(
        context.applicationContext,
        PaymentEntryPoint::class.java,
    ).stripePaymentHandler()

    DisposableEffect(paymentViewModel) {
        stripeHandler.setResultCallback { result ->
            paymentViewModel.onPaymentSheetResult(result)
        }
        onDispose {
            stripeHandler.clearResultCallback()
        }
    }

    LaunchedEffect(paymentState.pendingClientSecret) {
        paymentState.pendingClientSecret?.let { secret ->
            stripeHandler.presentPayment(secret)
            paymentViewModel.onClientSecretConsumed()
        }
    }

    PublisherNavHost(
        onSignOut = onSignOut,
        onSubscribePremium = { paymentViewModel.startPremiumPayment() },
        onBoostEvent = { eventId -> paymentViewModel.startBoostPayment(eventId) },
        paymentViewModel = paymentViewModel,
        modifier = Modifier.fillMaxSize(),
    )
}
