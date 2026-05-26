package com.example.livent.di

import com.example.livent.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.functions.Functions
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SupabaseModule {

    private const val PLACEHOLDER_URL = "https://placeholder.supabase.co"
    private const val PLACEHOLDER_KEY = "placeholder-anon-key"

    /** Strips `/rest/v1` so Auth and PostgREST share the project base URL. */
    internal fun normalizeSupabaseUrl(raw: String): String =
        raw.trim()
            .removeSuffix("/")
            .replace(Regex("/rest/v1/?$"), "")

    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClient {
        val url = normalizeSupabaseUrl(BuildConfig.SUPABASE_URL).ifBlank { PLACEHOLDER_URL }
        val key = BuildConfig.SUPABASE_ANON_KEY.trim().ifBlank { PLACEHOLDER_KEY }
        return createSupabaseClient(supabaseUrl = url, supabaseKey = key) {
            install(Postgrest)
            install(Auth)
            install(Storage)
            install(Functions)
        }
    }
}
