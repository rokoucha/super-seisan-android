package net.rokoucha.superseisan.data.model

data class SettlementResultDetail(
    val participant: Participant,
    val expenditures: Int,
    val payments: Int,
    val differences: Int,
    val items: List<Pair<String, Int>>
)
