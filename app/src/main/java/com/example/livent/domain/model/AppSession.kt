package com.example.livent.domain.model

sealed class AppSession {
    data object Guest : AppSession()

    data class Authenticated(
        val role: UserRole,
        val userId: String,
    ) : AppSession()
}
