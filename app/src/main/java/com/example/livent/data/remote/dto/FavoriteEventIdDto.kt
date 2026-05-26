package com.example.livent.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** PostgREST projection when selecting only `event_id`. */
@Serializable
data class FavoriteEventIdDto(
    @SerialName("event_id") val eventId: String,
)
