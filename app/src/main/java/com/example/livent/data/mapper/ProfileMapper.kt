package com.example.livent.data.mapper

import com.example.livent.data.remote.dto.ProfileDto
import com.example.livent.domain.model.Profile
import com.example.livent.domain.model.SubscriptionTier
import com.example.livent.domain.model.UserRole

fun ProfileDto.toDomain(): Profile = Profile(
    id = id,
    role = role.toUserRole(),
    displayName = displayName,
    email = email,
    subscriptionTier = subscriptionTier.toSubscriptionTier(),
)

fun String.toSubscriptionTier(): SubscriptionTier = when (lowercase()) {
    "premium" -> SubscriptionTier.PREMIUM
    else -> SubscriptionTier.FREE
}

fun String.toUserRole(): UserRole = when (lowercase()) {
    "publisher" -> UserRole.PUBLISHER
    else -> UserRole.USER
}

fun UserRole.toMetadataRole(): String = when (this) {
    UserRole.PUBLISHER -> "publisher"
    UserRole.USER -> "user"
}
