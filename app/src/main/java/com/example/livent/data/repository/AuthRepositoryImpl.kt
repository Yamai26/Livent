package com.example.livent.data.repository

import com.example.livent.data.mapper.toMetadataRole
import com.example.livent.data.mapper.toUserRole
import com.example.livent.domain.model.AppSession
import com.example.livent.domain.model.UserRole
import com.example.livent.domain.repository.AuthRepository
import com.example.livent.domain.repository.ProfileRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.exceptions.RestException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient,
    private val profileRepository: ProfileRepository,
) : AuthRepository {

    private val auth get() = supabaseClient.auth

    override fun observeSession(): Flow<AppSession> =
        auth.sessionStatus.map { status -> mapSessionStatus(status) }

    override suspend fun signIn(email: String, password: String): Result<AppSession> = runCatching {
        auth.signInWith(Email) {
            this.email = email.trim()
            this.password = password
        }
        resolveCurrentSession()
    }.mapFailure()

    override suspend fun signUp(
        email: String,
        password: String,
        role: UserRole,
        displayName: String?,
    ): Result<AppSession> = runCatching {
        val metadata = buildJsonObject {
            put("role", role.toMetadataRole())
            displayName?.trim()?.takeIf { it.isNotEmpty() }?.let { put("display_name", it) }
        }
        auth.signUpWith(Email) {
            this.email = email.trim()
            this.password = password
            data = metadata
        }
        resolveCurrentSession()
    }.mapFailure()

    override suspend fun signOut() {
        auth.signOut()
    }

    override suspend fun currentSession(): AppSession = resolveCurrentSession()

    override suspend fun refreshSessionOnStart() {
        if (auth.currentUserOrNull() != null) {
            resolveCurrentSession()
        }
    }

    private suspend fun resolveCurrentSession(): AppSession {
        val user = auth.currentUserOrNull() ?: return AppSession.Guest
        val userId = user.id
        val profile = profileRepository.getProfile(userId)
        val role = profile?.role
            ?: user.userMetadata?.get("role")?.toString()?.trim('"')?.toUserRole()
            ?: UserRole.USER
        return AppSession.Authenticated(role = role, userId = userId)
    }

    private suspend fun mapSessionStatus(status: SessionStatus): AppSession = when (status) {
        is SessionStatus.Authenticated -> resolveCurrentSession()
        SessionStatus.Initializing -> currentSessionOrGuest()
        is SessionStatus.NotAuthenticated -> AppSession.Guest
        is SessionStatus.RefreshFailure -> {
            signOut()
            AppSession.Guest
        }
    }

    private suspend fun currentSessionOrGuest(): AppSession =
        if (auth.currentUserOrNull() != null) resolveCurrentSession() else AppSession.Guest

    private fun <T> Result<T>.mapFailure(): Result<T> = fold(
        onSuccess = { Result.success(it) },
        onFailure = { throwable ->
            val message = (throwable as? RestException)?.error
                ?: throwable.message
                ?: "Authentication failed"
            Result.failure(Exception(message, throwable))
        },
    )
}
