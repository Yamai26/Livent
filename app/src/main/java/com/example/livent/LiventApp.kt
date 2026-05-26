package com.example.livent

import android.app.Application
import com.stripe.android.PaymentConfiguration
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class LiventApp : Application() {

    override fun onCreate() {
        super.onCreate()
        val publishableKey = BuildConfig.STRIPE_PUBLISHABLE_KEY.trim()
        if (publishableKey.isNotBlank()) {
            PaymentConfiguration.init(applicationContext, publishableKey)
        }
    }
}
