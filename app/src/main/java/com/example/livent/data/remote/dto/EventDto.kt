package com.example.livent.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EventDto(
    val id: String,
    @SerialName("publisher_id") val publisherId: String,
    val title: String,
    val artist: String = "",
    val description: String = "",
    val location: String = "",
    @SerialName("starts_at") val startsAt: String,
    @SerialName("poster_url") val posterUrl: String? = null,
    val status: String,
    @SerialName("is_featured") val isFeatured: Boolean = false,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
)
