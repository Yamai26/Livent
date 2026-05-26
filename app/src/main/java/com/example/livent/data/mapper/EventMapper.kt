package com.example.livent.data.mapper

import com.example.livent.data.remote.dto.EventDto
import com.example.livent.data.remote.dto.EventInsertDto
import com.example.livent.data.remote.dto.EventUpdateDto
import com.example.livent.domain.model.Event
import com.example.livent.domain.model.EventStatus

fun EventDto.toDomain(): Event = Event(
    id = id,
    publisherId = publisherId,
    title = title,
    artist = artist,
    description = description,
    location = location,
    startsAt = startsAt,
    posterUrl = posterUrl,
    status = status.toEventStatus(),
    isFeatured = isFeatured,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun String.toEventStatus(): EventStatus = when (lowercase()) {
    "active" -> EventStatus.ACTIVE
    "past" -> EventStatus.PAST
    "draft" -> EventStatus.DRAFT
    else -> EventStatus.DRAFT
}

fun EventStatus.toPostgresValue(): String = when (this) {
    EventStatus.ACTIVE -> "active"
    EventStatus.PAST -> "past"
    EventStatus.DRAFT -> "draft"
}

fun Event.toInsertDto(publisherId: String): EventInsertDto = EventInsertDto(
    publisherId = publisherId,
    title = title.trim(),
    artist = artist,
    description = description,
    location = location,
    startsAt = startsAt,
    posterUrl = posterUrl,
    status = status.toPostgresValue(),
    isFeatured = isFeatured,
)

fun Event.toUpdateDto(): EventUpdateDto = EventUpdateDto(
    title = title.trim(),
    artist = artist,
    description = description,
    location = location,
    startsAt = startsAt,
    posterUrl = posterUrl,
    status = status.toPostgresValue(),
    isFeatured = isFeatured,
)
