package com.example.livent.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FavoriteInsertDto(
    @SerialName("user_id") val userId: String,
    @SerialName("event_id") val eventId: String,
)
