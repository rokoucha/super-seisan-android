package net.rokoucha.superseisan.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import net.rokoucha.superseisan.data.entity.Benefited
import net.rokoucha.superseisan.data.entity.Item
import net.rokoucha.superseisan.data.entity.ItemWithRelations

@Dao
abstract class ItemDao {
    @Transaction
    @Query("SELECT * FROM items WHERE settlement_id = :settlementId")
    abstract fun getItemsStream(settlementId: String): Flow<List<ItemWithRelations>>

    @Transaction
    @Query("SELECT * FROM items WHERE id = :id")
    abstract fun getItemStream(id: String): Flow<ItemWithRelations>

    @Query("DELETE FROM benefited WHERE item_id = :itemId")
    protected abstract suspend fun clearBenefits(itemId: String)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun insertBenefited(benefited: Benefited)

    @Insert
    protected abstract suspend fun insertItem(item: Item)

    @Transaction
    open suspend fun insert(item: Item, benefited: List<Benefited>) {
        insertItem(item)
        benefited.forEach { insertBenefited(it) }
    }

    @Update
    protected abstract suspend fun updateItem(item: Item)

    @Transaction
    open suspend fun update(item: Item, benefited: List<Benefited>) {
        clearBenefits(item.id)
        updateItem(item)
        benefited.forEach { insertBenefited(it) }
    }

    @Delete
    abstract suspend fun delete(item: Item)
}
