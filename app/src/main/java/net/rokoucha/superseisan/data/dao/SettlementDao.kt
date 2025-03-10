package net.rokoucha.superseisan.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import net.rokoucha.superseisan.data.entity.Settlement
import net.rokoucha.superseisan.data.entity.SettlementWithParticipants
import net.rokoucha.superseisan.data.entity.SettlementWithRelations
import java.time.OffsetDateTime

@Dao
interface SettlementDao {
    @Transaction
    @Query("SELECT * FROM settlements ORDER BY last_accessed_at DESC, id ASC")
    fun getSettlementsStream(): Flow<List<SettlementWithParticipants>>

    @Transaction
    @Query("SELECT * FROM settlements WHERE id = :id")
    fun getSettlementStream(id: String): Flow<SettlementWithRelations>

    @Query("UPDATE settlements SET last_accessed_at = :now WHERE id = :id")
    suspend fun updateLastAccessedAt(id: String, now: OffsetDateTime)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(settlement: Settlement)

    @Update
    suspend fun update(settlement: Settlement)

    @Delete
    suspend fun delete(settlement: Settlement)
}
