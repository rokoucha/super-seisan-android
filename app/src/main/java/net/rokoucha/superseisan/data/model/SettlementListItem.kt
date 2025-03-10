package net.rokoucha.superseisan.data.model

import java.time.OffsetDateTime

data class SettlementListItem(
    val id: String,
    val name: String,
    val participants: List<Participant>,
    val lastAccessedAt: OffsetDateTime
)
