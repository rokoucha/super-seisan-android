package net.rokoucha.superseisan.data.dao

import android.database.sqlite.SQLiteConstraintException
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import net.rokoucha.superseisan.data.SuperSeisanDatabase
import net.rokoucha.superseisan.data.entity.Benefited
import net.rokoucha.superseisan.data.entity.Currency
import net.rokoucha.superseisan.data.entity.Item
import net.rokoucha.superseisan.data.entity.Participant
import net.rokoucha.superseisan.data.entity.Settlement
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.time.OffsetDateTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class SettlementDaoTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    @Named("test_db")
    lateinit var database: SuperSeisanDatabase

    private lateinit var settlementDao: SettlementDao

    private lateinit var currencyDao: CurrencyDao
    private lateinit var itemDao: ItemDao
    private lateinit var participantDao: ParticipantDao

    private val now = OffsetDateTime.now()

    private val settlement1 = Settlement(
        UUID.randomUUID().toString(),
        "settlement1",
        now,
        now
    )
    private val settlement2 = Settlement(
        UUID.randomUUID().toString(),
        "settlement2",
        now,
        now.plusSeconds(1)
    )

    @Before
    fun createDb() {
        hiltRule.inject()
        settlementDao = database.settlementDao()
        currencyDao = database.currencyDao()
        itemDao = database.itemDao()
        participantDao = database.participantDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        database.close()
    }

    @Test
    @Throws(Exception::class)
    fun daoGetSettlementsStream_returnsSettlementsFromDB() = runBlocking {
        addTwoSettlementsToDb()
        val participant1 = Participant(
            UUID.randomUUID().toString(),
            "participant1",
            settlement1.id
        )
        participantDao.insert(participant1)
        val participant2 = Participant(
            UUID.randomUUID().toString(),
            "participant2",
            settlement2.id
        )
        participantDao.insert(participant2)

        val settlements = settlementDao.getSettlementsStream().first()
        assertEquals(2, settlements.size)
        assertEquals(settlement2, settlements[0].settlement)
        assertEquals(1, settlements[0].participants.size)
        assertEquals(participant2, settlements[0].participants[0])
        assertEquals(settlement1, settlements[1].settlement)
        assertEquals(1, settlements[1].participants.size)
        assertEquals(participant1, settlements[1].participants[0])
    }

    @Test
    @Throws(Exception::class)
    fun daoGetSettlementStream_returnsSettlementWithItemsFromDB() = runBlocking {
        val settlement = Settlement(
            UUID.randomUUID().toString(),
            "settlement",
            now,
            now
        )
        settlementDao.insert(settlement)
        val payer = Participant(
            UUID.randomUUID().toString(),
            "participant",
            settlement.id
        )
        participantDao.insert(payer)
        val beneficiary = Participant(
            UUID.randomUUID().toString(),
            "benefited",
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
            10.0,
            1,
            settlement.id,
            payer.id,
            currency.id
        )
        itemDao.insert(item, listOf(Benefited(beneficiary.id, item.id)))

        val actual = settlementDao.getSettlementStream(settlement.id).first()
        assertEquals(settlement, actual.settlement)
        assertEquals(1, actual.items.size)
        assertEquals(item, actual.items[0].item)
        assertEquals(payer, actual.items[0].payer)
        assertEquals(currency, actual.items[0].currency)
        assertEquals(1, actual.items[0].benefited.size)
        assertEquals(beneficiary, actual.items[0].benefited[0])
        assertEquals(2, actual.participants.size)
        assertEquals(beneficiary, actual.participants[0])
        assertEquals(payer, actual.participants[1])
        assertEquals(1, actual.currencies.size)
        assertEquals(currency, actual.currencies[0])
    }

    @Test
    @Throws(Exception::class)
    fun daoUpdateLastAccessedAt_updatesLastAccessedAtInDB() = runBlocking {
        addTwoSettlementsToDb()

        val future = now.plusSeconds(2)
        val updatedSettlement = settlement1.copy(lastAccessedAt = future)

        settlementDao.updateLastAccessedAt(settlement1.id, future)
        val actual = settlementDao.getSettlementsStream().first()
        assertEquals(2, actual.size)
        assertEquals(updatedSettlement, actual[0].settlement)
        assertEquals(settlement2, actual[1].settlement)
    }

    @Test
    @Throws(Exception::class)
    fun daoInsert_insertsSettlementIntoDB() = runBlocking {
        settlementDao.insert(settlement1)
        val actual = settlementDao.getSettlementsStream().first()
        assertEquals(1, actual.size)
        assertEquals(settlement1, actual[0].settlement)
    }

    @Test(expected = SQLiteConstraintException::class)
    @Throws(Exception::class)
    fun daoInsert_throwsSQLiteConstraintExceptionOnDuplicateId() = runBlocking {
        settlementDao.insert(settlement1)
        settlementDao.insert(settlement1)
        val actual = settlementDao.getSettlementsStream().first()
        assertEquals(1, actual.size)
    }

    @Test
    @Throws(Exception::class)
    fun daoUpdate_updatesSettlementInDB() = runBlocking {
        addTwoSettlementsToDb()

        val updatedSettlement = settlement1.copy(name = "updated")
        settlementDao.update(updatedSettlement)
        val actual = settlementDao.getSettlementsStream().first()
        assertEquals(2, actual.size)
        assertEquals(settlement2, actual[0].settlement)
        assertEquals(updatedSettlement, actual[1].settlement)
    }

    @Test
    @Throws(Exception::class)
    fun daoDelete_deletesSettlementFromDB() = runBlocking {
        addTwoSettlementsToDb()

        settlementDao.delete(settlement1)
        val actual = settlementDao.getSettlementsStream().first()
        assertEquals(1, actual.size)
        assertEquals(settlement2, actual[0].settlement)
    }

    private suspend fun addTwoSettlementsToDb() {
        settlementDao.insert(settlement1)
        settlementDao.insert(settlement2)
    }
}