package net.rokoucha.superseisan.data

import net.rokoucha.superseisan.data.entity.SettlementWithParticipants
import net.rokoucha.superseisan.data.model.Currency
import net.rokoucha.superseisan.data.model.Item
import net.rokoucha.superseisan.data.model.Participant
import net.rokoucha.superseisan.data.model.Settlement
import net.rokoucha.superseisan.data.model.SettlementResult
import net.rokoucha.superseisan.data.model.SettlementResultDetail
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.OffsetDateTime
import java.util.UUID
import net.rokoucha.superseisan.data.entity.Currency as CurrencyEntity
import net.rokoucha.superseisan.data.entity.Item as ItemEntity
import net.rokoucha.superseisan.data.entity.Participant as ParticipantEntity
import net.rokoucha.superseisan.data.entity.Settlement as SettlementEntity

class ModelMappingTest {
    @Test
    fun toCurrency_returnsCurrencyModel() {
        val entity = CurrencyEntity(
            UUID.randomUUID().toString(),
            "USD",
            150.0,
            UUID.randomUUID().toString()
        )

        val actual = ModelMapping.toCurrency(entity)
        assertEquals(entity.id, actual.id)
        assertEquals(entity.symbol, actual.symbol)
        assertEquals(entity.rate, actual.rate, 0.0)
    }

    @Test
    fun toItem_returnsItemModel() {
        val entity = ItemEntity(
            UUID.randomUUID().toString(),
            "item",
            100.0,
            2,
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString()
        )
        val payer = Participant(UUID.randomUUID().toString(), "payer")
        val currency = Currency(UUID.randomUUID().toString(), "USD", 150.0)
        val benefited = listOf(Participant(UUID.randomUUID().toString(), "benefited"))

        val actual = ModelMapping.toItem(entity, payer, currency, benefited)
        assertEquals(entity.id, actual.id)
        assertEquals(entity.name, actual.name)
        assertEquals(entity.price, actual.price, 0.0)
        assertEquals(entity.quantity, actual.quantity)
        assertEquals(payer, actual.payer)
        assertEquals(currency, actual.currency)
        assertEquals(benefited, actual.benefited)
    }

    @Test
    fun toParticipant_returnsParticipantModel() {
        val entity = ParticipantEntity(
            UUID.randomUUID().toString(),
            "participant",
            UUID.randomUUID().toString()
        )

        val actual = ModelMapping.toParticipant(entity)
        assertEquals(entity.id, actual.id)
        assertEquals(entity.name, actual.name)
    }

    @Test
    fun toSettlement_returnsSettlementModel() {
        val entity = SettlementEntity(
            UUID.randomUUID().toString(),
            "settlement",
            OffsetDateTime.now(),
            OffsetDateTime.now()
        )
        val currency = Currency(
            UUID.randomUUID().toString(),
            "USD",
            150.0
        )
        val payer = Participant(
            UUID.randomUUID().toString(),
            "payer"
        )
        val benefited = listOf(
            Participant(UUID.randomUUID().toString(), "benefited1"),
            Participant(UUID.randomUUID().toString(), "benefited2")
        )
        val items = listOf(
            Item(
                UUID.randomUUID().toString(),
                "item1",
                100.0,
                2,
                payer,
                currency,
                benefited
            ),
            Item(
                UUID.randomUUID().toString(),
                "item2",
                200.0,
                1,
                payer,
                currency,
                benefited
            )
        )
        val participants = listOf(
            Participant(UUID.randomUUID().toString(), "participant"),
            Participant(UUID.randomUUID().toString(), "participant")
        )
        val currencies = listOf(
            Currency(UUID.randomUUID().toString(), "USD", 150.0),
            Currency(UUID.randomUUID().toString(), "USD", 150.0)
        )

        val actual = ModelMapping.toSettlement(entity, items, participants, currencies)
        assertEquals(entity.id, actual.id)
        assertEquals(entity.name, actual.name)
        assertEquals(items, actual.items)
        assertEquals(participants, actual.participants)
        assertEquals(currencies, actual.currencies)
        assertEquals(entity.updatedAt, actual.updatedAt)
        assertEquals(entity.lastAccessedAt, actual.lastAccessedAt)
    }

    @Test
    fun toSettlementListItem_returnsSettlementListItemModel() {
        val settlement = Settlement(
            UUID.randomUUID().toString(),
            "settlement",
            emptyList(),
            emptyList(),
            emptyList(),
            OffsetDateTime.now(),
            OffsetDateTime.now()
        )
        val participants = listOf(
            Participant(UUID.randomUUID().toString(), "participant1"),
            Participant(UUID.randomUUID().toString(), "participant2")
        )
        val entity = SettlementEntity(
            settlement.id,
            settlement.name,
            OffsetDateTime.now(),
            OffsetDateTime.now()
        )
        val participantEntities = participants.map {
            ParticipantEntity(it.id, it.name, entity.id)
        }

        val actual = ModelMapping.toSettlementListItem(
            SettlementWithParticipants(entity, participantEntities)
        )
        assertEquals(entity.id, actual.id)
        assertEquals(entity.name, actual.name)
        assertEquals(participants, actual.participants)
        assertEquals(entity.lastAccessedAt, actual.lastAccessedAt)
    }

    @Test
    fun toSettlementResult_returnsSettlementResultModel() {
        val participant1 = Participant(UUID.randomUUID().toString(), "participant1")
        val participant2 = Participant(UUID.randomUUID().toString(), "participant2")
        val participant3 = Participant(UUID.randomUUID().toString(), "participant3")

        val currency = Currency(UUID.randomUUID().toString(), "USD", 150.0)

        val settlement = Settlement(
            UUID.randomUUID().toString(),
            "settlement",
            listOf(
                Item(
                    UUID.randomUUID().toString(),
                    "item1",
                    100.0,
                    2,
                    participant1,
                    null,
                    listOf(participant1, participant2, participant3)
                ),
                Item(
                    UUID.randomUUID().toString(),
                    "item2",
                    200.0,
                    1,
                    participant2,
                    currency,
                    listOf(participant1, participant2, participant3)
                ),
                Item(
                    UUID.randomUUID().toString(),
                    "item3",
                    500.0,
                    1,
                    participant3,
                    null,
                    listOf(participant2, participant3)
                )
            ),
            listOf(participant1, participant2, participant3),
            listOf(currency),
            OffsetDateTime.now(),
            OffsetDateTime.now()
        )

        val expected = SettlementResult(
            surplus = -2,
            details = listOf(
                SettlementResultDetail(
                    participant = participant1,
                    expenditures = 10066,
                    payments = 200,
                    differences = 9866,
                    items = listOf(
                        "item1" to 66,
                        "item2" to 10000
                    )
                ),
                SettlementResultDetail(
                    participant = participant2,
                    expenditures = 10316,
                    payments = 30000,
                    differences = -19684,
                    items = listOf(
                        "item1" to 66,
                        "item2" to 10000,
                        "item3" to 250
                    )
                ),
                SettlementResultDetail(
                    participant = participant3,
                    expenditures = 10316,
                    payments = 500,
                    differences = 9816,
                    items = listOf(
                        "item1" to 66,
                        "item2" to 10000,
                        "item3" to 250
                    )
                )
            )
        )

        val actual = ModelMapping.toSettlementResult(settlement)
        assertEquals(expected, actual)
    }
}
