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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
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
class ItemDaoTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    @Named("test_db")
    lateinit var database: SuperSeisanDatabase

    private lateinit var itemDao: ItemDao
    private lateinit var currencyDao: CurrencyDao
    private lateinit var participantDao: ParticipantDao
    private lateinit var settlementDao: SettlementDao

    private val settlement1 = Settlement(
        UUID.randomUUID().toString(),
        "settlement1",
        OffsetDateTime.MIN,
        OffsetDateTime.MIN
    )
    private val settlement2 = Settlement(
        UUID.randomUUID().toString(),
        "settlement2",
        OffsetDateTime.MIN,
        OffsetDateTime.MIN
    )

    private val participant1 = Participant(
        UUID.randomUUID().toString(),
        "participant1",
        settlement1.id
    )
    private val participant2 = Participant(
        UUID.randomUUID().toString(),
        "participant2",
        settlement1.id
    )
    private val participant3 = Participant(
        UUID.randomUUID().toString(),
        "participant3",
        settlement2.id
    )

    private val currency = Currency(
        UUID.randomUUID().toString(),
        "USD",
        150.0,
        settlement2.id
    )

    private val item1 = Item(
        UUID.randomUUID().toString(),
        "item1",
        100.0,
        1,
        settlement1.id,
        participant1.id,
        null
    )
    private val item2 = Item(
        UUID.randomUUID().toString(),
        "item2",
        200.0,
        2,
        settlement1.id,
        participant2.id,
        null
    )
    private val item3 = Item(
        UUID.randomUUID().toString(),
        "item3",
        300.0,
        3,
        settlement2.id,
        participant3.id,
        currency.id
    )

    @Before
    fun createDb() {
        hiltRule.inject()
        itemDao = database.itemDao()
        currencyDao = database.currencyDao()
        participantDao = database.participantDao()
        settlementDao = database.settlementDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        database.close()
    }

    @Test
    @Throws(Exception::class)
    fun daoGetItemsStream_returnsItemsFromDB() = runBlocking {
        settlementDao.insert(settlement1)
        settlementDao.insert(settlement2)
        participantDao.insert(participant1)
        participantDao.insert(participant2)
        participantDao.insert(participant3)
        currencyDao.insert(currency)

        itemDao.insert(item1, emptyList())
        itemDao.insert(item2, listOf(Benefited(participant1.id, item2.id)))
        itemDao.insert(item3, emptyList())

        val actual = itemDao.getItemsStream(settlement1.id).first()
        assertEquals(2, actual.size)
        assertEquals(item1, actual[0].item)
        assertEquals(participant1, actual[0].payer)
        assertNull(actual[0].currency)
        assertEquals(0, actual[0].benefited.size)
        assertEquals(item2, actual[1].item)
        assertEquals(participant2, actual[1].payer)
        assertNull(actual[1].currency)
        assertEquals(1, actual[1].benefited.size)
        assertEquals(participant1, actual[1].benefited[0])
    }

    @Test
    @Throws(Exception::class)
    fun daoGetItemStream_returnsItemFromDB() = runBlocking {
        settlementDao.insert(settlement2)
        participantDao.insert(participant3)
        currencyDao.insert(currency)
        itemDao.insert(item3, listOf(Benefited(participant3.id, item3.id)))

        val actual = itemDao.getItemStream(item3.id).first()
        assertEquals(item3, actual.item)
        assertEquals(participant3, actual.payer)
        assertEquals(1, actual.benefited.size)
        assertEquals(participant3, actual.benefited[0])
        assertEquals(currency, actual.currency)
    }

    @Test
    @Throws(Exception::class)
    fun daoInsert_insertsItemIntoDB() = runBlocking {
        settlementDao.insert(settlement1)
        participantDao.insert(participant1)
        itemDao.insert(item1, emptyList())
        val actual = itemDao.getItemStream(item1.id).first()
        assertEquals(item1, actual.item)
        assertEquals(participant1, actual.payer)
        assertEquals(0, actual.benefited.size)
        assertNull(actual.currency)
    }

    @Test(expected = SQLiteConstraintException::class)
    @Throws(Exception::class)
    fun daoInsert_throwsSQLiteConstraintExceptionWhenInsertingSameItem() = runBlocking {
        settlementDao.insert(settlement1)
        participantDao.insert(participant1)
        itemDao.insert(item1, emptyList())
        itemDao.insert(item1, emptyList())
        val actual = itemDao.getItemsStream(settlement1.id).first()
        assertEquals(1, actual.size)
    }

    @Test
    @Throws(Exception::class)
    fun daoUpdate_updatesItemInDB() = runBlocking {
        settlementDao.insert(settlement1)
        participantDao.insert(participant1)
        participantDao.insert(participant2)
        itemDao.insert(item1, emptyList())
        val updatedItem = item1.copy(
            payerId = participant2.id
        )
        itemDao.update(updatedItem, emptyList())
        val actual = itemDao.getItemStream(updatedItem.id).first()
        assertEquals(updatedItem, actual.item)
        assertEquals(participant2, actual.payer)
        assertEquals(0, actual.benefited.size)
        assertNull(actual.currency)
    }

    @Test
    @Throws(Exception::class)
    fun daoDelete_deletesItemFromDB() = runBlocking {
        settlementDao.insert(settlement2)
        currencyDao.insert(currency)
        participantDao.insert(participant3)
        itemDao.insert(item3, emptyList())
        val check = itemDao.getItemsStream(settlement2.id).first()
        assertEquals(1, check.size)
        itemDao.delete(item3)
        val actual = itemDao.getItemsStream(settlement2.id).first()
        assertEquals(0, actual.size)
        val participant = participantDao.getParticipantStream(participant3.id).first()
        assertNotNull(participant)
        val currency = currencyDao.getCurrencyStream(currency.id).first()
        assertNotNull(currency)
    }
}