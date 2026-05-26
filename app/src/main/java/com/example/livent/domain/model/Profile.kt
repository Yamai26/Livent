package com.example.livent.domain.model

data class Profile(
    val id: String,
    val role: UserRole,
    val displayName: String?,
    val email: String?,
    val subscriptionTier: SubscriptionTier = SubscriptionTier.FREE,
)
