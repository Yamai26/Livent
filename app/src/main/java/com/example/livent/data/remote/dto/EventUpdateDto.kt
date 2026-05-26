package com.example.livent.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EventUpdateDto(
    val title: String,
    val artist: String = "",
    val description: String = "",
    val location: String = "",
    @SerialName("starts_at") val startsAt: String,
    @SerialName("poster_url") val posterUrl: String? = null,
    val status: String,
    @SerialName("is_featured") val isFeatured: Boolean = false,
)
