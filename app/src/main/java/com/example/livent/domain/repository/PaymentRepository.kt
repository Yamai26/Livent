package com.example.livent.domain.repository

import com.example.livent.domain.model.PaymentClientData

interface PaymentRepository {
    suspend fun createPremiumPayment(): Result<PaymentClientData>
    suspend fun createBoostPayment(eventId: String): Result<PaymentClientData>
}
