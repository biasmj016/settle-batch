package com.batch.settlement.batch

import com.batch.settlement.batch.result.MatchedResult
import com.batch.settlement.batch.result.MismatchedResult
import com.batch.settlement.batch.result.SettlementResult
import com.batch.settlement.domain.PartnerTransactions
import com.batch.settlement.domain.Transactions
import org.springframework.batch.item.ItemProcessor
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component

@Component
class TransactionsMatchingProcessor (
    private val redisTemplate: RedisTemplate<String, PartnerTransactions>
) : ItemProcessor<Transactions, SettlementResult> {

    override fun process(item: Transactions): SettlementResult? {
        val key = "partner:${item.transactionId}"
        return redisTemplate.opsForValue().get(key)?.let {
            redisTemplate.delete(key)
            MatchedResult(
                transactionId = item.transactionId,
                amount = item.amount,
                transactionDate = item.transactionDate.toLocalDate()
            )
        } ?: MismatchedResult(
            transactionId = item.transactionId,
            amount = item.amount,
            transactionDate = item.transactionDate.toLocalDate()
        )
    }
}