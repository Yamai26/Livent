package com.example.livent.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreatePaymentRequestDto(
    val type: String,
    @SerialName("eventId") val eventId: String? = null,
)

@Serializable
data class CreatePaymentResponseDto(
    val clientSecret: String,
)

@Serializable
data class CreatePaymentErrorDto(
    val error: String? = null,
)
