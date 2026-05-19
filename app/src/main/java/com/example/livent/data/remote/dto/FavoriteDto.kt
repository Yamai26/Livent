package com.example.livent.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FavoriteDto(
    @SerialName("user_id") val userId: String,
    @SerialName("event_id") val eventId: String,
    @SerialName("created_at") val createdAt: String? = null,
)
