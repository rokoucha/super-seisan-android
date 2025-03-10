package net.rokoucha.superseisan.data.model

data class Item(
    val id: String,
    val name: String,
    val price: Double,
    val quantity: Int,
    val payer: Participant?,
    val currency: Currency?,
    val benefited: List<Participant>
)
