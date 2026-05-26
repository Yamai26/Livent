package com.example.livent.domain.repository

import com.example.livent.domain.model.Event
import com.example.livent.domain.model.EventStatus

interface EventRepository {

    /** Publisher's events, newest `starts_at` first. */
    suspend fun getMyEvents(): List<Event>

    suspend fun getEvent(id: String): Event?

    suspend fun createEvent(
        title: String,
        artist: String,
        description: String,
        location: String,
        startsAt: String,
        posterUrl: String?,
        status: EventStatus,
    ): Result<Event>

    suspend fun updateEvent(
        id: String,
        title: String,
        artist: String,
        description: String,
        location: String,
        startsAt: String,
        posterUrl: String?,
        status: EventStatus,
        isFeatured: Boolean,
    ): Result<Event>

    suspend fun deleteEvent(id: String): Result<Unit>

    suspend fun countMyActiveEvents(): Int
}
