package com.example.livent.data.remote

import com.example.livent.data.remote.dto.CreatePaymentErrorDto
import com.example.livent.data.remote.dto.CreatePaymentRequestDto
import com.example.livent.data.remote.dto.CreatePaymentResponseDto
import com.example.livent.domain.model.PaymentClientData
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.functions.functions
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentRemoteDataSource @Inject constructor(
    private val supabaseClient: SupabaseClient,
) {

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun createPayment(
        type: String,
        eventId: String? = null,
    ): Result<PaymentClientData> = runCatching {
        val response = supabaseClient.functions.invoke(
            function = FUNCTION_NAME,
            body = CreatePaymentRequestDto(type = type, eventId = eventId),
        )
        val bodyText = response.bodyAsText()
        if (!response.status.isSuccess()) {
            val apiError = runCatching {
                json.decodeFromString<CreatePaymentErrorDto>(bodyText).error
            }.getOrNull()
            throw Exception(mapPaymentError(apiError ?: bodyText, response.status.value))
        }
        val dto = json.decodeFromString<CreatePaymentResponseDto>(bodyText)
        PaymentClientData(clientSecret = dto.clientSecret)
    }.fold(
        onSuccess = { Result.success(it) },
        onFailure = { throwable ->
            Result.failure(
                Exception(
                    mapPaymentThrowable(throwable),
                    throwable,
                ),
            )
        },
    )

    private fun mapPaymentError(raw: String, status: Int): String = when (status) {
        401 -> "Debes iniciar sesión para pagar."
        403 -> when {
            raw.contains("publisher", ignoreCase = true) ->
                "Solo cuentas de publisher pueden realizar pagos."
            raw.contains("evento", ignoreCase = true) ||
                raw.contains("destacar", ignoreCase = true) ->
                "No puedes destacar este evento."
            else -> "No tienes permiso para este pago."
        }
        400 -> raw.ifBlank { "Solicitud de pago no válida." }
        else -> raw.ifBlank { "No se pudo iniciar el pago. Inténtalo de nuevo." }
    }

    private fun mapPaymentThrowable(throwable: Throwable): String {
        val raw = throwable.message.orEmpty()
        return when {
            raw.contains("401") || raw.contains("No autorizado", ignoreCase = true) ->
                "Debes iniciar sesión para pagar."
            raw.contains("403") -> "No tienes permiso para este pago."
            raw.isNotBlank() -> raw
            else -> "No se pudo conectar con el servidor de pagos."
        }
    }

    companion object {
        private const val FUNCTION_NAME = "create-payment"
    }
}
