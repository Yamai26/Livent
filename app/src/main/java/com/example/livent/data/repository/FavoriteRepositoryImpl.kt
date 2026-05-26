package com.example.livent.data.repository

import com.example.livent.data.remote.dto.FavoriteEventIdDto
import com.example.livent.data.remote.dto.FavoriteInsertDto
import com.example.livent.domain.model.Event
import com.example.livent.domain.repository.ExploreRepository
import com.example.livent.domain.repository.FavoriteRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@Singleton
class FavoriteRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient,
    private val exploreRepository: ExploreRepository,
) : FavoriteRepository {

    private val postgrest get() = supabaseClient.postgrest
    private val favoriteIds = MutableStateFlow<Set<String>>(emptySet())

    override suspend fun getMyFavoriteEvents(): List<Event> {
        val ids = loadFavoriteIds()
        favoriteIds.value = ids
        if (ids.isEmpty()) return emptyList()
        return ids.mapNotNull { exploreRepository.getActiveEvent(it) }
    }

    override suspend fun isFavorite(eventId: String): Boolean {
        if (eventId in favoriteIds.value) return true
        val ids = loadFavoriteIds()
        favoriteIds.value = ids
        return eventId in ids
    }

    override suspend fun addFavorite(eventId: String): Result<Unit> = runCatching {
        val userId = requireUserId()
        if (eventId in favoriteIds.value) return@runCatching
        postgrest.from(TABLE).insert(FavoriteInsertDto(userId = userId, eventId = eventId))
        favoriteIds.update { it + eventId }
    }.mapFavoriteFailure().recoverCatching { throwable ->
        if (isDuplicateFavorite(throwable)) {
            favoriteIds.update { it + eventId }
        } else {
            throw throwable
        }
    }

    override suspend fun removeFavorite(eventId: String): Result<Unit> = runCatching {
        val userId = requireUserId()
        postgrest.from(TABLE).delete {
            filter {
                eq("user_id", userId)
                eq("event_id", eventId)
            }
        }
        favoriteIds.update { it - eventId }
    }.mapFavoriteFailure()

    override suspend fun refreshFavoriteIds() {
        favoriteIds.value = loadFavoriteIds()
    }

    override suspend fun countFavoritesForEventIds(eventIds: List<String>): Int {
        if (eventIds.isEmpty()) return 0
        return runCatching {
            postgrest.from(TABLE)
                .select(Columns.list("event_id")) {
                    filter {
                        isIn("event_id", eventIds)
                    }
                }
                .decodeList<FavoriteEventIdDto>()
                .size
        }.getOrElse { 0 }
    }

    override fun observeFavoriteIds(): Flow<Set<String>> = favoriteIds.asStateFlow()

    private suspend fun loadFavoriteIds(): Set<String> = runCatching {
        postgrest.from(TABLE)
            .select(Columns.list("event_id"))
            .decodeList<FavoriteEventIdDto>()
            .map { it.eventId }
            .toSet()
    }.getOrElse { emptySet() }

    private fun requireUserId(): String =
        supabaseClient.auth.currentUserOrNull()?.id
            ?: error("Debes iniciar sesión para gestionar favoritos.")

    private fun <T> Result<T>.mapFavoriteFailure(): Result<T> = fold(
        onSuccess = { Result.success(it) },
        onFailure = { throwable ->
            Result.failure(Exception(mapFavoriteError(throwable), throwable))
        },
    )

    companion object {
        private const val TABLE = "favorites"

        private fun isDuplicateFavorite(throwable: Throwable): Boolean {
            val candidates = listOfNotNull(throwable, throwable.cause)
            return candidates.any { t ->
                val raw = (t as? RestException)?.error ?: t.message ?: ""
                raw.contains("23505", ignoreCase = true) ||
                    raw.contains("duplicate key", ignoreCase = true) ||
                    raw.contains("already exists", ignoreCase = true)
            }
        }

        fun mapFavoriteError(throwable: Throwable): String {
            val raw = (throwable as? RestException)?.error
                ?: throwable.message
                ?: ""
            return when {
                raw.contains("42501", ignoreCase = true) ||
                    raw.contains("permission denied", ignoreCase = true) ||
                    raw.contains("403", ignoreCase = true) ->
                    "No tienes permiso para guardar favoritos. Inicia sesión de nuevo."
                raw.contains("23503", ignoreCase = true) ->
                    "No se pudo guardar: el evento o tu perfil no están disponibles en el servidor."
                raw.contains("JWT", ignoreCase = true) ||
                    raw.contains("401", ignoreCase = true) ->
                    "Sesión expirada. Vuelve a iniciar sesión."
                raw.isNotBlank() -> raw
                else -> "No se pudo actualizar el favorito."
            }
        }
    }
}
