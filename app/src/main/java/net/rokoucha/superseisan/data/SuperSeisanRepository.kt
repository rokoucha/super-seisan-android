package net.rokoucha.superseisan.data

import kotlinx.coroutines.flow.Flow
import net.rokoucha.superseisan.data.model.Currency
import net.rokoucha.superseisan.data.model.Item
import net.rokoucha.superseisan.data.model.Participant
import net.rokoucha.superseisan.data.model.Settlement
import net.rokoucha.superseisan.data.model.SettlementListItem
import net.rokoucha.superseisan.data.model.SettlementResult

interface SuperSeisanRepository {
    fun getSettlementsStream(): Flow<List<SettlementListItem>>

    fun getSettlementStream(settlementId: String): Flow<Settlement>

    fun getCurrenciesStream(settlementId: String): Flow<List<Currency>>

    fun getCurrencyStream(settlementId: String, currencyId: String): Flow<Currency>

    fun getParticipantsStream(settlementId: String): Flow<List<Participant>>

    fun getParticipantStream(settlementId: String, participantId: String): Flow<Participant>

    fun calculateSettlementResult(settlementStream: Flow<Settlement>): Flow<SettlementResult>

    suspend fun addCurrency(settlementId: String, symbol: String, rate: Double): String

    suspend fun addParticipant(settlementId: String, name: String): String

    suspend fun addItem(settlementId: String, item: Item): String

    suspend fun addSettlement(name: String): String

    suspend fun updateCurrency(settlementId: String, currency: Currency)

    suspend fun updateParticipant(settlementId: String, participant: Participant)

    suspend fun updateItem(settlementId: String, item: Item)

    suspend fun deleteCurrency(settlementId: String, currencyId: String)

    suspend fun deleteParticipant(settlementId: String, participantId: String)

    suspend fun deleteItem(settlementId: String, itemId: String)

    suspend fun deleteSettlement(settlementId: String)
}
