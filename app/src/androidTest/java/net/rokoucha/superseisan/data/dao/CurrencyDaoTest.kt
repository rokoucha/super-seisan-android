package net.rokoucha.superseisan.data.dao

import android.database.sqlite.SQLiteConstraintException
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import net.rokoucha.superseisan.data.SuperSeisanDatabase
import net.rokoucha.superseisan.data.entity.Currency
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
class CurrencyDaoTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    @Named("test_db")
    lateinit var database: SuperSeisanDatabase

    private lateinit var currencyDao: CurrencyDao
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

    private val currency1 = Currency(UUID.randomUUID().toString(), "USD", 150.0, settlement1.id)
    private val currency2 = Currency(UUID.randomUUID().toString(), "EUR", 155.0, settlement1.id)
    private val currency3 = Currency(UUID.randomUUID().toString(), "PHP", 2.5, settlement2.id)

    @Before
    fun createDb() {
        hiltRule.inject()
        currencyDao = database.currencyDao()
        settlementDao = database.settlementDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        database.close()
    }

    @Test
    @Throws(Exception::class)
    fun daoGetCurrenciesStream_returnsCurrencyWhichRelatedWithSettlementId() = runBlocking {
        addThreeCurrencyToDb()
        val actual = currencyDao.getCurrenciesStream(settlement1.id).first()
        assertEquals(2, actual.size)
        // FIXME: なぜか順番が逆になる
        assertEquals(currency2, actual[0])
        assertEquals(currency1, actual[1])
    }

    @Test
    @Throws(Exception::class)
    fun daoGetCurrencyStream_returnsCurrency() = runBlocking {
        addThreeCurrencyToDb()
        val actual = currencyDao.getCurrencyStream(currency1.id).first()
        assertEquals(currency1, actual)
    }

    @Test
    @Throws(Exception::class)
    fun daoInsert_insertsCurrencyIntoDB() = runBlocking {
        settlementDao.insert(settlement1)
        currencyDao.insert(currency1)
        val actual = currencyDao.getCurrencyStream(currency1.id).first()
        assertEquals(currency1, actual)
    }

    @Test(expected = SQLiteConstraintException::class)
    @Throws(Exception::class)
    fun daoInsert_throwsSQLiteConstraintExceptionWhenInsertingSameCurrency() = runBlocking {
        settlementDao.insert(settlement1)
        currencyDao.insert(currency1)
        currencyDao.insert(currency1)
        val actual = currencyDao.getCurrenciesStream(settlement1.id).first()
        assertEquals(1, actual.size)
    }

    @Test(expected = SQLiteConstraintException::class)
    @Throws(Exception::class)
    fun daoInsert_throwsSQLiteConstraintExceptionWhenInsertingSymbol() = runBlocking {
        settlementDao.insert(settlement1)
        currencyDao.insert(currency1)
        currencyDao.insert(
            Currency(
                UUID.randomUUID().toString(),
                "USD",
                99.0,
                settlement1.id
            )
        )
        val actual = currencyDao.getCurrenciesStream(settlement1.id).first()
        assertEquals(1, actual.size)
    }

    @Test
    @Throws(Exception::class)
    fun daoUpdate_updatesCurrency() = runBlocking {
        settlementDao.insert(settlement1)
        currencyDao.insert(currency1)
        val updatedCurrency = currency1.copy(rate = 200.0)
        currencyDao.update(updatedCurrency)
        val actual = currencyDao.getCurrencyStream(currency1.id).first()
        assertEquals(updatedCurrency, actual)
    }

    @Test
    @Throws(Exception::class)
    fun daoDelete_deletesCurrency() = runBlocking {
        addThreeCurrencyToDb()
        currencyDao.delete(currency2)
        val actual = currencyDao.getCurrenciesStream(currency2.id).first()
        assertEquals(0, actual.size)
    }

    private suspend fun addThreeCurrencyToDb() {
        settlementDao.insert(settlement1)
        settlementDao.insert(settlement2)
        currencyDao.insert(currency1)
        currencyDao.insert(currency2)
        currencyDao.insert(currency3)
    }
}
