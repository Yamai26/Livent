package com.example.livent.data.repository

import com.example.livent.data.remote.PaymentRemoteDataSource
import com.example.livent.domain.model.PaymentClientData
import com.example.livent.domain.repository.PaymentRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentRepositoryImpl @Inject constructor(
    private val remote: PaymentRemoteDataSource,
) : PaymentRepository {

    override suspend fun createPremiumPayment(): Result<PaymentClientData> =
        remote.createPayment(type = TYPE_PREMIUM)

    override suspend fun createBoostPayment(eventId: String): Result<PaymentClientData> =
        remote.createPayment(type = TYPE_BOOST, eventId = eventId)

    companion object {
        private const val TYPE_PREMIUM = "premium"
        private const val TYPE_BOOST = "boost"
    }
}
