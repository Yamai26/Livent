package com.example.livent.presentation.util

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val displayFormatter = DateTimeFormatter.ofPattern(
    "d MMM yyyy, HH:mm",
    Locale.forLanguageTag("es-ES"),
)

fun formatEventDate(isoTimestamp: String): String = runCatching {
    val instant = Instant.parse(isoTimestamp)
    displayFormatter.format(instant.atZone(ZoneId.systemDefault()))
}.getOrElse { isoTimestamp }
