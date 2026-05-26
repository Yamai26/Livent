package com.example.livent.domain.repository

import com.example.livent.domain.model.Event

/**
 * Public exploration feed (anon + authenticated).
 * Separate from [EventRepository] which is publisher-scoped CRUD.
 */
interface ExploreRepository {

    suspend fun getFeaturedActiveEvents(): List<Event>

    /** All active events ordered by `starts_at` ascending (upcoming first). */
    suspend fun getUpcomingActiveEvents(): List<Event>

    suspend fun getActiveEvent(id: String): Event?
}
