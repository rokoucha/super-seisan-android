package net.rokoucha.superseisan.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import net.rokoucha.superseisan.data.entity.Participant

@Dao
interface ParticipantDao {
    @Query("SELECT * FROM participants WHERE settlement_id = :settlementId")
    fun getParticipantsStream(settlementId: String): Flow<List<Participant>>

    @Query("SELECT * FROM participants WHERE id = :id")
    fun getParticipantStream(id: String): Flow<Participant>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(participant: Participant)

    @Update
    suspend fun update(participant: Participant)

    @Delete
    suspend fun delete(participant: Participant)
}