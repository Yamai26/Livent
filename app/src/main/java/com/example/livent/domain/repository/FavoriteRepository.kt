package com.example.livent.domain.repository

import com.example.livent.domain.model.Event
import kotlinx.coroutines.flow.Flow

interface FavoriteRepository {

    suspend fun getMyFavoriteEvents(): List<Event>

    suspend fun isFavorite(eventId: String): Boolean

    suspend fun addFavorite(eventId: String): Result<Unit>

    suspend fun removeFavorite(eventId: String): Result<Unit>

    /** Reloads favorite IDs from PostgREST (e.g. after login). */
    suspend fun refreshFavoriteIds()

    /** Count favorites rows whose `event_id` is in the given list (publisher dashboard). */
    suspend fun countFavoritesForEventIds(eventIds: List<String>): Int

    fun observeFavoriteIds(): Flow<Set<String>>
}
