package net.rokoucha.superseisan.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import net.rokoucha.superseisan.data.dao.CurrencyDao
import net.rokoucha.superseisan.data.dao.ItemDao
import net.rokoucha.superseisan.data.dao.ParticipantDao
import net.rokoucha.superseisan.data.dao.SettlementDao
import net.rokoucha.superseisan.data.entity.Benefited
import net.rokoucha.superseisan.data.entity.Currency
import net.rokoucha.superseisan.data.entity.Item
import net.rokoucha.superseisan.data.entity.Participant
import net.rokoucha.superseisan.data.entity.Settlement

@Database(
    entities = [
        Benefited::class,
        Currency::class,
        Item::class,
        Participant::class,
        Settlement::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class SuperSeisanDatabase : RoomDatabase() {
    abstract fun currencyDao(): CurrencyDao
    abstract fun itemDao(): ItemDao
    abstract fun participantDao(): ParticipantDao
    abstract fun settlementDao(): SettlementDao
}
