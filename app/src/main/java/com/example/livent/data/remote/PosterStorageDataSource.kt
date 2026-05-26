package com.example.livent.data.remote

import com.example.livent.BuildConfig
import com.example.livent.di.SupabaseModule
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.storage.storage
import io.ktor.http.ContentType
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PosterStorageDataSource @Inject constructor(
    private val supabaseClient: SupabaseClient,
) {

    suspend fun uploadPoster(
        userId: String,
        bytes: ByteArray,
        contentType: String,
        extension: String,
    ): Result<String> = runCatching {
        val ext = extension.trimStart('.').lowercase().ifBlank { "jpg" }
        val path = "$userId/${UUID.randomUUID()}.$ext"
        val bucket = supabaseClient.storage.from(BUCKET)
        bucket.upload(path, bytes) {
            this.contentType = ContentType.parse(contentType)
            upsert = false
        }
        publicPosterUrl(path)
    }

    private fun publicPosterUrl(relativePath: String): String {
        val base = SupabaseModule.normalizeSupabaseUrl(BuildConfig.SUPABASE_URL)
            .removeSuffix("/")
        return "$base/storage/v1/object/public/$BUCKET/$relativePath"
    }

    companion object {
        const val BUCKET = "posters"
    }
}
