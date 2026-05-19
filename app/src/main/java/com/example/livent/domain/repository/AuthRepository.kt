package com.example.livent.domain.repository

import com.example.livent.domain.model.AppSession
import com.example.livent.domain.model.UserRole
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun observeSession(): Flow<AppSession>

    suspend fun signIn(email: String, password: String): Result<AppSession>

    suspend fun signUp(
        email: String,
        password: String,
        role: UserRole,
        displayName: String?,
    ): Result<AppSession>

    suspend fun signOut()

    suspend fun currentSession(): AppSession

    suspend fun refreshSessionOnStart()
}
