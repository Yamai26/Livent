package com.example.livent.di

import com.example.livent.presentation.payment.StripePaymentHandler
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface PaymentEntryPoint {
    fun stripePaymentHandler(): StripePaymentHandler
}
