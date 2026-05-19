package com.example.livent.data.mapper

import com.example.livent.data.remote.dto.ProfileDto
import com.example.livent.domain.model.Profile
import com.example.livent.domain.model.UserRole

fun ProfileDto.toDomain(): Profile = Profile(
    id = id,
    role = role.toUserRole(),
    displayName = displayName,
    email = email,
)

fun String.toUserRole(): UserRole = when (lowercase()) {
    "publisher" -> UserRole.PUBLISHER
    else -> UserRole.USER
}

fun UserRole.toMetadataRole(): String = when (this) {
    UserRole.PUBLISHER -> "publisher"
    UserRole.USER -> "user"
}
