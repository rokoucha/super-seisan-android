package net.rokoucha.superseisan.data.dao

import android.database.sqlite.SQLiteConstraintException
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import net.rokoucha.superseisan.data.SuperSeisanDatabase
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
class ParticipantDaoTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    @Named("test_db")
    lateinit var database: SuperSeisanDatabase

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

    @Before
    fun createDb() {
        hiltRule.inject()
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
    fun daoGetParticipantsStream_returnsParticipantsFromDB() = runBlocking {
        addThreeParticipantsToDb()

        val participants = participantDao.getParticipantsStream(settlement1.id).first()
        assertEquals(2, participants.size)
        assertEquals(participant1, participants[0])
        assertEquals(participant2, participants[1])
    }

    @Test
    @Throws(Exception::class)
    fun daoGetParticipantStream_returnsParticipantFromDB() = runBlocking {
        addThreeParticipantsToDb()

        val participant = participantDao.getParticipantStream(participant1.id).first()
        assertEquals(participant1, participant)
    }

    @Test
    @Throws(Exception::class)
    fun daoInsert_insertsParticipantIntoDB() = runBlocking {
        settlementDao.insert(settlement1)
        participantDao.insert(participant1)
        val participants = participantDao.getParticipantsStream(settlement1.id).first()
        assertEquals(1, participants.size)
        assertEquals(participant1, participants[0])
    }


    @Test(expected = SQLiteConstraintException::class)
    @Throws(Exception::class)
    fun daoInsert_throwExceptionWhenInsertingDuplicateParticipant() = runBlocking {
        settlementDao.insert(settlement1)
        participantDao.insert(participant1)
        participantDao.insert(participant1)
        val actual = participantDao.getParticipantsStream(settlement1.id).first()
        assertEquals(1, actual.size)
    }

    @Test(expected = SQLiteConstraintException::class)
    @Throws(Exception::class)
    fun daoInsert_throwExceptionWhenInsertingSameNameParticipant() = runBlocking {
        settlementDao.insert(settlement1)
        participantDao.insert(participant1)
        participantDao.insert(
            Participant(
                UUID.randomUUID().toString(),
                participant1.name,
                settlement1.id
            )
        )
        val actual = participantDao.getParticipantsStream(settlement1.id).first()
        assertEquals(1, actual.size)
    }

    @Test
    @Throws(Exception::class)
    fun daoUpdate_updatesParticipantInDB() = runBlocking {
        addThreeParticipantsToDb()

        val updatedParticipant = participant1.copy(name = "updatedName")
        participantDao.update(updatedParticipant)
        val participants = participantDao.getParticipantsStream(settlement1.id).first()
        assertEquals(2, participants.size)
        assertEquals(participant2, participants[0])
        assertEquals(updatedParticipant, participants[1])
    }

    @Test
    @Throws(Exception::class)
    fun daoDelete_deletesParticipantFromDB() = runBlocking {
        addThreeParticipantsToDb()

        participantDao.delete(participant1)
        val participants = participantDao.getParticipantsStream(settlement1.id).first()
        assertEquals(1, participants.size)
        assertEquals(participant2, participants[0])
    }

    private suspend fun addThreeParticipantsToDb() {
        settlementDao.insert(settlement1)
        settlementDao.insert(settlement2)
        participantDao.insert(participant1)
        participantDao.insert(participant2)
        participantDao.insert(participant3)
    }
}