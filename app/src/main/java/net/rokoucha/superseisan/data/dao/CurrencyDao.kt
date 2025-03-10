package net.rokoucha.superseisan.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import net.rokoucha.superseisan.data.entity.Currency

@Dao
interface CurrencyDao {
    @Query("SELECT * FROM currencies WHERE settlement_id = :settlementId")
    fun getCurrenciesStream(settlementId: String): Flow<List<Currency>>

    @Query("SELECT * FROM currencies WHERE id = :id")
    fun getCurrencyStream(id: String): Flow<Currency>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(currency: Currency)

    @Update
    suspend fun update(currency: Currency)

    @Delete
    suspend fun delete(currency: Currency)
}