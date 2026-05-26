package com.example.livent.domain.repository

import com.example.livent.domain.model.Profile

interface ProfileRepository {
    suspend fun getProfile(userId: String): Profile?

    /** Reloads profile from PostgREST (e.g. after Stripe webhook updates tier). */
    suspend fun refreshProfile(userId: String): Profile? = getProfile(userId)
}
