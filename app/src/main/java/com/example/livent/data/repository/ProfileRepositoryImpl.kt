package com.example.livent.data.repository

import com.example.livent.data.mapper.toDomain
import com.example.livent.data.remote.dto.ProfileDto
import com.example.livent.domain.model.Profile
import com.example.livent.domain.repository.ProfileRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient,
) : ProfileRepository {

    override suspend fun getProfile(userId: String): Profile? = runCatching {
        supabaseClient.postgrest.from("profiles")
            .select(Columns.ALL) {
                filter {
                    eq("id", userId)
                }
            }
            .decodeSingle<ProfileDto>()
            .toDomain()
    }.getOrNull()
}
