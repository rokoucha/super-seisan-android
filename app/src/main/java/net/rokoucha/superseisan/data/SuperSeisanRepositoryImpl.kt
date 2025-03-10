package net.rokoucha.superseisan.data

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import net.rokoucha.superseisan.data.dao.CurrencyDao
import net.rokoucha.superseisan.data.dao.ItemDao
import net.rokoucha.superseisan.data.dao.ParticipantDao
import net.rokoucha.superseisan.data.dao.SettlementDao
import net.rokoucha.superseisan.data.entity.Benefited
import net.rokoucha.superseisan.data.model.Currency
import net.rokoucha.superseisan.data.model.Item
import net.rokoucha.superseisan.data.model.Participant
import net.rokoucha.superseisan.data.model.Settlement
import net.rokoucha.superseisan.data.model.SettlementListItem
import net.rokoucha.superseisan.data.model.SettlementResult
import net.rokoucha.superseisan.di.DefaultDispatcher
import java.time.OffsetDateTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import net.rokoucha.superseisan.data.entity.Currency as CurrencyEntity
import net.rokoucha.superseisan.data.entity.Item as ItemEntity
import net.rokoucha.superseisan.data.entity.Participant as ParticipantEntity
import net.rokoucha.superseisan.data.entity.Settlement as SettlementEntity

@Singleton
class SuperSeisanRepositoryImpl @Inject constructor(
    private val currencyDao: CurrencyDao,
    private val itemDao: ItemDao,
    private val participantDao: ParticipantDao,
    private val settlementDao: SettlementDao,
    @DefaultDispatcher private val dispatcher: CoroutineDispatcher
) : SuperSeisanRepository {
    override fun getSettlementsStream(): Flow<List<SettlementListItem>> {
        return settlementDao.getSettlementsStream().map { settlements ->
            settlements.map { settlement ->
                withContext(dispatcher) {
                    ModelMapping.toSettlementListItem(settlement)
                }
            }
        }
    }

    override fun getSettlementStream(settlementId: String): Flow<Settlement> {
        return settlementDao.getSettlementStream(settlementId).map { settlement ->
            withContext(dispatcher) {
                Settlement(
                    id = settlement.settlement.id,
                    name = settlement.settlement.name,
                    items = settlement.items.map { item ->
                        Item(
                            id = item.item.id,
                            name = item.item.name,
                            price = item.item.price,
                            quantity = item.item.quantity,
                            payer = if (item.payer != null) {
                                Participant(
                                    id = item.payer.id,
                                    name = item.payer.name
                                )
                            } else {
                                null
                            },
                            currency = if (item.currency != null) {
                                Currency(
                                    id = item.currency.id,
                                    symbol = item.currency.symbol,
                                    rate = item.currency.rate
                                )
                            } else {
                                null
                            },
                            benefited = item.benefited.map { beneficiary ->
                                Participant(
                                    id = beneficiary.id,
                                    name = beneficiary.name
                                )
                            }
                        )
                    },
                    participants = settlement.participants.map { participant ->
                        Participant(
                            id = participant.id,
                            name = participant.name
                        )
                    },
                    currencies = settlement.currencies.map { currency ->
                        Currency(
                            id = currency.id,
                            symbol = currency.symbol,
                            rate = currency.rate
                        )
                    },
                    updatedAt = settlement.settlement.updatedAt,
                    lastAccessedAt = settlement.settlement.lastAccessedAt
                )
            }
        }
    }

    override fun getCurrenciesStream(settlementId: String): Flow<List<Currency>> {
        return currencyDao.getCurrenciesStream(settlementId).map { currencies ->
            currencies.map { currency ->
                Currency(
                    id = currency.id,
                    symbol = currency.symbol,
                    rate = currency.rate
                )
            }
        }
    }

    override fun getCurrencyStream(
        settlementId: String,
        currencyId: String
    ): Flow<Currency> {
        return currencyDao.getCurrencyStream(currencyId).map { currency ->
            Currency(
                id = currency.id,
                symbol = currency.symbol,
                rate = currency.rate
            )
        }
    }

    override fun getParticipantsStream(settlementId: String): Flow<List<Participant>> {
        return participantDao.getParticipantsStream(settlementId).map { participants ->
            participants.map { participant ->
                Participant(
                    id = participant.id,
                    name = participant.name
                )
            }
        }
    }

    override fun getParticipantStream(
        settlementId: String,
        participantId: String
    ): Flow<Participant> {
        return participantDao.getParticipantStream(participantId).map { participant ->
            Participant(
                id = participant.id,
                name = participant.name
            )
        }
    }

    override fun calculateSettlementResult(settlementStream: Flow<Settlement>): Flow<SettlementResult> {
        return settlementStream.map {
            withContext(dispatcher) {
                ModelMapping.toSettlementResult(it)
            }
        }
    }

    override suspend fun addCurrency(settlementId: String, symbol: String, rate: Double): String {
        val id = UUID.randomUUID().toString()
        currencyDao.insert(
            CurrencyEntity(
                id = id,
                symbol = symbol,
                rate = rate,
                settlementId = settlementId
            )
        )
        return id
    }

    override suspend fun addParticipant(settlementId: String, name: String): String {
        val id = UUID.randomUUID().toString()
        participantDao.insert(
            ParticipantEntity(
                id = id,
                name = name,
                settlementId = settlementId
            )
        )
        return id
    }

    override suspend fun addItem(settlementId: String, item: Item): String {
        val id = UUID.randomUUID().toString()
        itemDao.insert(
            ItemEntity(
                id = id,
                name = item.name,
                price = item.price,
                quantity = item.quantity,
                settlementId = settlementId,
                payerId = item.payer?.id,
                currencyId = item.currency?.id
            ),
            item.benefited.map {
                Benefited(
                    participantId = it.id,
                    itemId = id
                )
            }
        )
        return id
    }

    override suspend fun addSettlement(name: String): String {
        val now = OffsetDateTime.now()
        val id = UUID.randomUUID().toString()
        settlementDao.insert(
            SettlementEntity(
                id = id,
                name = name,
                updatedAt = now,
                lastAccessedAt = now
            )
        )
        return id
    }

    override suspend fun updateCurrency(settlementId: String, currency: Currency) {
        currencyDao.update(
            CurrencyEntity(
                id = currency.id,
                symbol = currency.symbol,
                rate = currency.rate,
                settlementId = settlementId
            )
        )
    }

    override suspend fun updateParticipant(settlementId: String, participant: Participant) {
        participantDao.update(
            ParticipantEntity(
                id = participant.id,
                name = participant.name,
                settlementId = settlementId
            )
        )
    }

    override suspend fun updateItem(settlementId: String, item: Item) {
        itemDao.update(
            ItemEntity(
                id = item.id,
                name = item.name,
                price = item.price,
                quantity = item.quantity,
                settlementId = settlementId,
                payerId = item.payer?.id,
                currencyId = item.currency?.id
            ),
            item.benefited.map {
                Benefited(
                    participantId = it.id,
                    itemId = item.id
                )
            }
        )
    }

    override suspend fun deleteCurrency(settlementId: String, currencyId: String) {
        currencyDao.delete(
            CurrencyEntity(
                id = currencyId,
                symbol = "",
                rate = 0.0,
                settlementId = settlementId
            )
        )
    }

    override suspend fun deleteParticipant(settlementId: String, participantId: String) {
        participantDao.delete(
            ParticipantEntity(
                id = participantId,
                name = "",
                settlementId = settlementId
            )
        )
    }

    override suspend fun deleteItem(settlementId: String, itemId: String) {
        itemDao.delete(
            ItemEntity(
                id = itemId,
                name = "",
                price = 0.0,
                quantity = 0,
                settlementId = settlementId,
                payerId = null,
                currencyId = null
            )
        )
    }

    override suspend fun deleteSettlement(settlementId: String) {
        settlementDao.delete(
            SettlementEntity(
                id = settlementId,
                name = "",
                updatedAt = OffsetDateTime.MIN,
                lastAccessedAt = OffsetDateTime.MIN
            )
        )
    }
}
