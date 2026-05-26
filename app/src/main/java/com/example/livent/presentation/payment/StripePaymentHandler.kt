package com.example.livent.presentation.payment

import androidx.activity.ComponentActivity
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import javax.inject.Inject
import javax.inject.Singleton

/**
 * PaymentSheet must be created in [ComponentActivity.onCreate] (before STARTED).
 * Call [initialize] from MainActivity; set the result callback from Compose when needed.
 */
@Singleton
class StripePaymentHandler @Inject constructor() {

    private var paymentSheet: PaymentSheet? = null
    private var onResult: ((PaymentSheetResult) -> Unit)? = null

    fun initialize(activity: ComponentActivity) {
        if (paymentSheet == null) {
            paymentSheet = PaymentSheet(activity) { result ->
                onResult?.invoke(result)
            }
        }
    }

    fun setResultCallback(callback: (PaymentSheetResult) -> Unit) {
        onResult = callback
    }

    fun clearResultCallback() {
        onResult = null
    }

    fun presentPayment(clientSecret: String) {
        val sheet = paymentSheet
            ?: error("StripePaymentHandler.initialize() must run in MainActivity.onCreate")
        sheet.presentWithPaymentIntent(
            paymentIntentClientSecret = clientSecret,
            configuration = PaymentSheet.Configuration.Builder("Livent").build(),
        )
    }
}
