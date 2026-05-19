package com.example.livent.domain.repository

import com.example.livent.domain.model.Profile

interface ProfileRepository {
    suspend fun getProfile(userId: String): Profile?
}
