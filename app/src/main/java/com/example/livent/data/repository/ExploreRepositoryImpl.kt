package com.example.livent.data.repository

import com.example.livent.data.mapper.toDomain
import com.example.livent.data.remote.dto.EventDto
import com.example.livent.domain.model.Event
import com.example.livent.domain.repository.ExploreRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExploreRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient,
) : ExploreRepository {

    private val postgrest get() = supabaseClient.postgrest

    override suspend fun getFeaturedActiveEvents(): List<Event> = runCatching {
        postgrest.from(TABLE)
            .select(Columns.ALL) {
                filter {
                    eq(STATUS_COLUMN, STATUS_ACTIVE)
                    eq(FEATURED_COLUMN, true)
                }
                order(STARTS_AT_COLUMN, Order.ASCENDING)
            }
            .decodeList<EventDto>()
            .map { it.toDomain() }
    }.getOrElse { emptyList() }

    override suspend fun getUpcomingActiveEvents(): List<Event> = runCatching {
        postgrest.from(TABLE)
            .select(Columns.ALL) {
                filter {
                    eq(STATUS_COLUMN, STATUS_ACTIVE)
                }
                order(STARTS_AT_COLUMN, Order.ASCENDING)
            }
            .decodeList<EventDto>()
            .map { it.toDomain() }
    }.getOrElse { emptyList() }

    override suspend fun getActiveEvent(id: String): Event? = runCatching {
        postgrest.from(TABLE)
            .select(Columns.ALL) {
                filter {
                    eq("id", id)
                    eq(STATUS_COLUMN, STATUS_ACTIVE)
                }
            }
            .decodeSingle<EventDto>()
            .toDomain()
    }.getOrNull()

    companion object {
        private const val TABLE = "events"
        private const val STATUS_COLUMN = "status"
        private const val STATUS_ACTIVE = "active"
        private const val FEATURED_COLUMN = "is_featured"
        private const val STARTS_AT_COLUMN = "starts_at"
    }
}
