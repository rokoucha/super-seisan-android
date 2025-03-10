package net.rokoucha.superseisan.data.model

import java.time.OffsetDateTime

data class Settlement(
    val id: String,
    val name: String,
    val items: List<Item>,
    val participants: List<Participant>,
    val currencies: List<Currency>,
    val updatedAt: OffsetDateTime,
    val lastAccessedAt: OffsetDateTime
)
