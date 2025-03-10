package net.rokoucha.superseisan.data.model

data class SettlementResult(
    val surplus: Int,
    val details: List<SettlementResultDetail>
)
