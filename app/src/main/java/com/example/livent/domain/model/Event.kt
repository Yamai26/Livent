package com.example.livent.domain.model

data class Event(
    val id: String,
    val publisherId: String,
    val title: String,
    val artist: String,
    val description: String,
    val location: String,
    /** ISO-8601 instant from PostgREST (`starts_at`). */
    val startsAt: String,
    val posterUrl: String?,
    val status: EventStatus,
    val isFeatured: Boolean = false,
    val createdAt: String? = null,
    val updatedAt: String? = null,
)
