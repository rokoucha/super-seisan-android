package net.rokoucha.superseisan.data

import net.rokoucha.superseisan.data.entity.SettlementWithParticipants
import net.rokoucha.superseisan.data.model.Currency
import net.rokoucha.superseisan.data.model.Item
import net.rokoucha.superseisan.data.model.Participant
import net.rokoucha.superseisan.data.model.Settlement
import net.rokoucha.superseisan.data.model.SettlementListItem
import net.rokoucha.superseisan.data.model.SettlementResult
import net.rokoucha.superseisan.data.model.SettlementResultDetail
import kotlin.math.floor
import net.rokoucha.superseisan.data.entity.Currency as CurrencyEntity
import net.rokoucha.superseisan.data.entity.Item as ItemEntity
import net.rokoucha.superseisan.data.entity.Participant as ParticipantEntity
import net.rokoucha.superseisan.data.entity.Settlement as SettlementEntity

class ModelMapping {
    companion object {
        fun toCurrency(currencyEntity: CurrencyEntity): Currency {
            return Currency(
                currencyEntity.id,
                currencyEntity.symbol,
                currencyEntity.rate
            )
        }

        fun toItem(
            item: ItemEntity,
            payer: Participant,
            currency: Currency?,
            benefited: List<Participant>
        ): Item {
            return Item(
                item.id,
                item.name,
                item.price,
                item.quantity,
                payer,
                currency,
                benefited
            )
        }

        fun toParticipant(participantEntity: ParticipantEntity): Participant {
            return Participant(
                participantEntity.id,
                participantEntity.name
            )
        }

        fun toSettlement(
            settlementEntity: SettlementEntity,
            items: List<Item>,
            participants: List<Participant>,
            currencies: List<Currency>
        ): Settlement {
            return Settlement(
                settlementEntity.id,
                settlementEntity.name,
                items,
                participants,
                currencies,
                settlementEntity.updatedAt,
                settlementEntity.lastAccessedAt
            )
        }

        fun toSettlementListItem(
            settlementWithParticipants: SettlementWithParticipants
        ): SettlementListItem {
            return SettlementListItem(
                settlementWithParticipants.settlement.id,
                settlementWithParticipants.settlement.name,
                settlementWithParticipants.participants.map { toParticipant(it) },
                settlementWithParticipants.settlement.lastAccessedAt
            )
        }

        fun toSettlementResult(
            settlement: Settlement
        ): SettlementResult {
            val details = settlement.participants.map { participant ->
                // 自分が受益した支出項目
                val items = settlement.items
                    .filter { participant in it.benefited }
                    .map {
                        it.name to floor(
                            it.price * (it.currency?.rate ?: 1.0) * it.quantity / it.benefited.size
                        ).toInt()
                    }

                // 自分が受益した支出の合計
                val expenditures = items.sumOf { it.second }

                // 自分が支払った合計
                val payments = floor(
                    settlement.items
                        .filter { it.payer == participant }
                        .sumOf { it.price * (it.currency?.rate ?: 1.0) * it.quantity }
                ).toInt()

                // 自分が受益した支出の合計と支払いの合計の差額が自分の精算額
                val differences = expenditures - payments

                SettlementResultDetail(
                    participant,
                    expenditures,
                    payments,
                    differences,
                    items
                )
            }

            // 全体での支出額と受益者の精算額の合計の差額が余剰金
            val surplus = details.sumOf { it.differences }

            return SettlementResult(
                surplus,
                details
            )
        }
    }
}
