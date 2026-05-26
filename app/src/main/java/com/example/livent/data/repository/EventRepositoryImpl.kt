package com.example.livent.data.repository

import com.example.livent.data.mapper.toDomain
import com.example.livent.data.mapper.toInsertDto
import com.example.livent.data.mapper.toUpdateDto
import com.example.livent.data.remote.dto.EventDto
import com.example.livent.domain.model.Event
import com.example.livent.domain.model.EventStatus
import com.example.livent.domain.repository.EventRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient,
) : EventRepository {

    private val postgrest get() = supabaseClient.postgrest

    override suspend fun getMyEvents(): List<Event> = runCatching {
        postgrest.from(TABLE)
            .select(Columns.ALL) {
                order("starts_at", Order.DESCENDING)
            }
            .decodeList<EventDto>()
            .map { it.toDomain() }
    }.getOrElse { emptyList() }

    override suspend fun getEvent(id: String): Event? = runCatching {
        postgrest.from(TABLE)
            .select(Columns.ALL) {
                filter { eq("id", id) }
            }
            .decodeSingle<EventDto>()
            .toDomain()
    }.getOrNull()

    override suspend fun createEvent(
        title: String,
        artist: String,
        description: String,
        location: String,
        startsAt: String,
        posterUrl: String?,
        status: EventStatus,
    ): Result<Event> = runCatching {
        val publisherId = requireUserId()
        val event = Event(
            id = "",
            publisherId = publisherId,
            title = title,
            artist = artist,
            description = description,
            location = location,
            startsAt = startsAt,
            posterUrl = posterUrl,
            status = status,
            isFeatured = false,
        )
        postgrest.from(TABLE)
            .insert(event.toInsertDto(publisherId)) {
                select(Columns.ALL)
            }
            .decodeSingle<EventDto>()
            .toDomain()
    }.mapFailure()

    override suspend fun updateEvent(
        id: String,
        title: String,
        artist: String,
        description: String,
        location: String,
        startsAt: String,
        posterUrl: String?,
        status: EventStatus,
        isFeatured: Boolean,
    ): Result<Event> = runCatching {
        val event = Event(
            id = id,
            publisherId = requireUserId(),
            title = title,
            artist = artist,
            description = description,
            location = location,
            startsAt = startsAt,
            posterUrl = posterUrl,
            status = status,
            isFeatured = isFeatured,
        )
        postgrest.from(TABLE)
            .update(event.toUpdateDto()) {
                filter { eq("id", id) }
                select(Columns.ALL)
            }
            .decodeSingle<EventDto>()
            .toDomain()
    }.mapFailure()

    override suspend fun deleteEvent(id: String): Result<Unit> = runCatching {
        postgrest.from(TABLE).delete {
            filter { eq("id", id) }
        }
        Unit
    }.mapFailure()

    override suspend fun countMyActiveEvents(): Int =
        getMyEvents().count { it.status == EventStatus.ACTIVE }

    private fun requireUserId(): String =
        supabaseClient.auth.currentUserOrNull()?.id
            ?: error("Debes iniciar sesión como publisher.")

    private fun <T> Result<T>.mapFailure(): Result<T> = fold(
        onSuccess = { Result.success(it) },
        onFailure = { throwable ->
            Result.failure(Exception(mapEventError(throwable), throwable))
        },
    )

    companion object {
        private const val TABLE = "events"

        fun mapEventError(throwable: Throwable): String {
            val raw = (throwable as? RestException)?.error
                ?: throwable.message
                ?: ""
            return when {
                raw.contains(FREE_PLAN_TRIGGER_MESSAGE, ignoreCase = true) ||
                    raw.contains("check_violation", ignoreCase = true) ->
                    "El plan gratuito solo permite un evento activo. Mejora a Premium."
                raw.isNotBlank() -> raw
                else -> "No se pudo guardar el evento."
            }
        }

        private const val FREE_PLAN_TRIGGER_MESSAGE =
            "Free plan allows at most one active event per publisher"
    }
}
