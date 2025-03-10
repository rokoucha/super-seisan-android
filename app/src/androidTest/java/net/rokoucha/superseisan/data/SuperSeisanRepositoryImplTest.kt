package net.rokoucha.superseisan.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import net.rokoucha.superseisan.data.dao.CurrencyDao
import net.rokoucha.superseisan.data.dao.ItemDao
import net.rokoucha.superseisan.data.dao.ParticipantDao
import net.rokoucha.superseisan.data.dao.SettlementDao
import net.rokoucha.superseisan.data.entity.Benefited
import net.rokoucha.superseisan.data.entity.Currency
import net.rokoucha.superseisan.data.entity.Item
import net.rokoucha.superseisan.data.entity.Participant
import net.rokoucha.superseisan.data.entity.Settlement
import net.rokoucha.superseisan.data.model.SettlementResult
import net.rokoucha.superseisan.data.model.SettlementResultDetail
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.OffsetDateTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named
import net.rokoucha.superseisan.data.model.Currency as CurrencyModel
import net.rokoucha.superseisan.data.model.Item as ItemModel
import net.rokoucha.superseisan.data.model.Participant as ParticipantModel
import net.rokoucha.superseisan.data.model.Settlement as SettlementModel

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class SuperSeisanRepositoryImplTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    @Named("test_db")
    lateinit var database: SuperSeisanDatabase

    @OptIn(ExperimentalCoroutinesApi::class)
    private var testDispatcher = UnconfinedTestDispatcher()
    private var testScope = TestScope(testDispatcher)

    private lateinit var currencyDao: CurrencyDao
    private lateinit var itemDao: ItemDao
    private lateinit var participantDao: ParticipantDao
    private lateinit var settlementDao: SettlementDao


    private lateinit var repository: SuperSeisanRepository

    @Before
    fun setUp() {
        hiltRule.inject()

        currencyDao = database.currencyDao()
        itemDao = database.itemDao()
        participantDao = database.participantDao()
        settlementDao = database.settlementDao()

        repository = SuperSeisanRepositoryImpl(
            currencyDao,
            itemDao,
            participantDao,
            settlementDao,
            testDispatcher
        )
    }

    @Test
    fun getSettlementsStream_returnsSettlementListItemModel() = testScope.runTest {
        val settlement = Settlement(
            UUID.randomUUID().toString(),
            "settlement",
            OffsetDateTime.now(),
            OffsetDateTime.now()
        )
        settlementDao.insert(settlement)
        val participant = Participant(
            UUID.randomUUID().toString(),
            "participant",
            settlement.id
        )
        participantDao.insert(participant)

        val actual = repository.getSettlementsStream().first()
        assertEquals(1, actual.size)
        assertEquals(settlement.id, actual[0].id)
        assertEquals(settlement.name, actual[0].name)
        assertEquals(1, actual[0].participants.size)
        assertEquals(participant.name, actual[0].participants[0].name)
        assertEquals(settlement.lastAccessedAt, actual[0].lastAccessedAt)
    }

    @Test
    fun getSettlementStream_returnsSettlementModel() = testScope.runTest {
        val settlement = insertSettlementWithRelations()

        val actual = repository.getSettlementStream(settlement.id).first()
        assertEquals(settlement.id, actual.id)
        assertEquals(settlement.name, actual.name)
        assertEquals(settlement.items, actual.items)
        assertEquals(settlement.participants, actual.participants)
        assertEquals(settlement.currencies, actual.currencies)
        assertEquals(settlement.lastAccessedAt, actual.lastAccessedAt)
        assertEquals(settlement.updatedAt, actual.updatedAt)
    }

    @Test
    fun getCurrenciesStream_returnsCurrencyModel() = testScope.runTest {
        val settlement = insertSettlementWithRelations()

        val actual = repository.getCurrenciesStream(settlement.id).first()
        assertEquals(1, actual.size)
        assertEquals(settlement.currencies.elementAt(0), actual[0])
    }

    @Test
    fun getCurrencyStream_returnsCurrencyModel() = testScope.runTest {
        val settlement = insertSettlementWithRelations()
        val currency = settlement.currencies.elementAt(0)

        val actual = repository.getCurrencyStream(settlement.id, currency.id).first()
        assertEquals(currency, actual)
    }

    @Test
    fun getParticipantsStream_returnsParticipantModel() = testScope.runTest {
        val settlement = insertSettlementWithRelations()

        val actual = repository.getParticipantsStream(settlement.id).first()
        assertEquals(2, actual.size)
        assertEquals(settlement.participants, actual)
    }

    @Test
    fun getParticipantStream_returnsParticipantModel() = testScope.runTest {
        val settlement = insertSettlementWithRelations()
        val participant = settlement.participants[0]

        val actual = repository.getParticipantStream(settlement.id, participant.id).first()
        assertEquals(participant.id, actual.id)
        assertEquals(participant.name, actual.name)
    }

    @Test
    fun calculateSettlementResult_returnsSettlementResult() = testScope.runTest {
        val settlement = insertSettlementWithRelations()
        val settlementStream = repository.getSettlementStream(settlement.id)

        val expected = SettlementResult(
            0,
            listOf(
                SettlementResultDetail(
                    settlement.participants[0],
                    15000,
                    0,
                    15000,
                    listOf(
                        Pair("item", 15000)
                    )
                ),
                SettlementResultDetail(
                    settlement.participants[1],
                    0,
                    15000,
                    -15000,
                    emptyList()
                )
            )
        )

        val actual = repository.calculateSettlementResult(settlementStream).first()
        assertEquals(expected, actual)
    }

    @Test
    fun addCurrency_insertsCurrency() = testScope.runTest {
        val settlement = insertSettlementWithRelations()

        val actualId = repository.addCurrency(settlement.id, "EUR", 160.0)

        val actual = currencyDao.getCurrencyStream(actualId).first()
        assertNotEquals("", actual.id)
        assertEquals("EUR", actual.symbol)
        assertEquals(160.0, actual.rate, 0.0)
    }

    @Test
    fun addParticipant_insertsParticipant() = testScope.runTest {
        val settlement = insertSettlementWithRelations()

        val actualId = repository.addParticipant(settlement.id, "participant2")

        val actual = participantDao.getParticipantStream(actualId).first()
        assertNotEquals("", actual.id)
        assertEquals("participant2", actual.name)
    }

    @Test
    fun addItem_insertsItem() = testScope.runTest {
        val settlement = insertSettlementWithRelations()
        val participants = settlement.participants.toList()
        val beneficiary = participants[1]
        val currency = settlement.currencies.elementAtOrNull(0)
        val item = ItemModel(
            "",
            "item2",
            200.0,
            2,
            participants[0],
            currency,
            listOf(beneficiary)
        )

        val actualId = repository.addItem(settlement.id, item)

        val actual = itemDao.getItemStream(actualId).first()
        assertNotEquals("", actual.item.id)
        assertEquals(item.name, actual.item.name)
        assertEquals(item.price, actual.item.price, 0.0)
        assertEquals(item.quantity, actual.item.quantity)
        assertEquals(item.payer?.id, actual.payer?.id)
        assertEquals(item.currency?.id, actual.currency?.id)
        assertEquals(1, actual.benefited.size)
        assertEquals(beneficiary.id, actual.benefited.elementAt(0).id)
    }

    @Test
    fun addSettlement_insertsSettlement() = testScope.runTest {
        val actualId = repository.addSettlement("settlement2")
        assertNotEquals("", actualId)

        val actual = settlementDao.getSettlementStream(actualId).first()
        assertEquals(actualId, actual.settlement.id)
        assertEquals("settlement2", actual.settlement.name)
        assertEquals(0, actual.items.size)
        assertEquals(0, actual.participants.size)
        assertEquals(0, actual.currencies.size)
        assertNotNull(actual.settlement.lastAccessedAt)
        assertNotNull(actual.settlement.updatedAt)
    }

    @Test
    fun updateCurrency_updatesCurrency() = testScope.runTest {
        val settlement = insertSettlementWithRelations()
        val currency = settlement.currencies.elementAt(0)
        val entity = CurrencyModel(
            currency.id,
            "EUR",
            160.0
        )

        repository.updateCurrency(settlement.id, entity)

        val actual = currencyDao.getCurrencyStream(currency.id).first()
        assertEquals(currency.id, actual.id)
        assertEquals("EUR", actual.symbol)
        assertEquals(160.0, actual.rate, 0.0)
    }

    @Test
    fun updateParticipant_updatesParticipant() = testScope.runTest {
        val settlement = insertSettlementWithRelations()
        val participant = settlement.participants.elementAt(0)
        val entity = ParticipantModel(
            participant.id,
            "participant2"
        )

        repository.updateParticipant(settlement.id, entity)

        val actual = participantDao.getParticipantStream(participant.id).first()
        assertEquals(participant.id, actual.id)
        assertEquals("participant2", actual.name)
        assertEquals(settlement.id, actual.settlementId)
    }

    @Test
    fun updateItem_updatesItem() = testScope.runTest {
        val settlement = insertSettlementWithRelations()
        val item = settlement.items.elementAt(0)
        val participants = settlement.participants.toList()
        val currency = settlement.currencies.elementAtOrNull(0)
        val entity = ItemModel(
            item.id,
            "item2",
            200.0,
            2,
            participants[0],
            currency,
            emptyList()
        )

        repository.updateItem(settlement.id, entity)

        val actual = itemDao.getItemStream(item.id).first()
        assertEquals(item.id, actual.item.id)
        assertEquals(entity.name, actual.item.name)
        assertEquals(entity.price, actual.item.price, 0.0)
        assertEquals(entity.quantity, actual.item.quantity)
        assertEquals(entity.payer?.id, actual.payer?.id)
        assertEquals(entity.currency?.id, actual.currency?.id)
        assertEquals(0, actual.benefited.size)
    }

    @Test
    fun deleteCurrency_deletesCurrency() = testScope.runTest {
        val settlement = insertSettlementWithRelations()
        val currency = settlement.currencies.elementAt(0)

        repository.deleteCurrency(settlement.id, currency.id)

        val actual = currencyDao.getCurrencyStream(currency.id).first()
        assertNull(actual)
    }

    @Test
    fun deleteParticipant_deletesParticipant() = testScope.runTest {
        val settlement = insertSettlementWithRelations()
        val participant = settlement.participants.elementAt(0)

        repository.deleteParticipant(settlement.id, participant.id)

        val actual = participantDao.getParticipantStream(participant.id).first()
        assertNull(actual)
    }

    @Test
    fun deleteItem_deletesItem() = testScope.runTest {
        val settlement = insertSettlementWithRelations()
        val item = settlement.items.elementAt(0)

        repository.deleteItem(settlement.id, item.id)

        val actual = itemDao.getItemStream(item.id).first()
        assertNull(actual)
    }

    @Test
    fun deleteSettlement_deletesSettlement() = testScope.runTest {
        val settlement = insertSettlementWithRelations()

        repository.deleteSettlement(settlement.id)

        val actual = settlementDao.getSettlementStream(settlement.id).first()
        assertNull(actual)
    }

    private suspend fun insertSettlementWithRelations(): SettlementModel {
        val settlement = Settlement(
            UUID.randomUUID().toString(),
            "settlement",
            OffsetDateTime.now(),
            OffsetDateTime.now()
        )
        settlementDao.insert(settlement)
        val payer = Participant(
            UUID.randomUUID().toString(),
            "payer",
            settlement.id
        )
        participantDao.insert(payer)
        val beneficiary = Participant(
            UUID.randomUUID().toString(),
            "beneficiary",
            settlement.id
        )
        participantDao.insert(beneficiary)
        val currency = Currency(
            UUID.randomUUID().toString(),
            "USD",
            150.0,
            settlement.id
        )
        currencyDao.insert(currency)
        val item = Item(
            UUID.randomUUID().toString(),
            "item",
            100.0,
            1,
            settlement.id,
            payer.id,
            currency.id
        )
        itemDao.insert(item, listOf(Benefited(beneficiary.id, item.id)))

        return SettlementModel(
            settlement.id,
            settlement.name,
            listOf(
                ItemModel(
                    item.id,
                    item.name,
                    item.price,
                    item.quantity,
                    ParticipantModel(
                        payer.id,
                        payer.name
                    ),
                    CurrencyModel(
                        currency.id,
                        currency.symbol,
                        currency.rate
                    ),
                    listOf(
                        ParticipantModel(
                            beneficiary.id,
                            beneficiary.name
                        )
                    )
                )
            ),
            listOf(
                ParticipantModel(
                    beneficiary.id,
                    beneficiary.name
                ),
                ParticipantModel(
                    payer.id,
                    payer.name
                )
            ),
            listOf(
                CurrencyModel(
                    currency.id,
                    currency.symbol,
                    currency.rate
                )
            ),
            settlement.updatedAt,
            settlement.lastAccessedAt
        )
    }
}
