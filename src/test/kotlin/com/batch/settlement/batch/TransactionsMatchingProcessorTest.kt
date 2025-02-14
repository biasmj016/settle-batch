package com.batch.settlement.batch

import com.batch.settlement.batch.result.MatchedResult
import com.batch.settlement.batch.result.MismatchedResult
import com.batch.settlement.domain.PartnerTransactions
import com.batch.settlement.domain.Transactions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ValueOperations
import java.math.BigDecimal
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class TransactionsMatchingProcessorTest {
    private lateinit var redisTemplate: RedisTemplate<String, PartnerTransactions>
    private lateinit var valueOps: ValueOperations<String, PartnerTransactions>
    private lateinit var processor: TransactionsMatchingProcessor

    @BeforeEach
    fun setUp() {
        redisTemplate = mock()
        valueOps = mock()
        whenever(redisTemplate.opsForValue()).thenReturn(valueOps)
        processor = TransactionsMatchingProcessor(redisTemplate)
    }

    @Test
    fun process() {
        val transactionId = "tx-123"
        val transaction = Transactions(
            transactionId = transactionId,
            amount = BigDecimal("5000"),
            transactionDate = LocalDateTime.now()
        )
        val partnerTransaction = PartnerTransactions(
            transactionId = transactionId,
            amount = BigDecimal("6000"),
            fee = BigDecimal("500"),
            transactionDate = LocalDateTime.now()
        )
        val key = "partner:$transactionId"
        whenever(valueOps.get(key)).thenReturn(partnerTransaction)

        val result = processor.process(transaction)

        System.out.println(result)

        verify(redisTemplate).delete(key)
        assert(result is MatchedResult)
    }

    @Test
    fun process_mismatch() {
        val transactionId = "tx-456"
        val transaction = Transactions(
            transactionId = transactionId,
            amount = BigDecimal("5000"),
            transactionDate = LocalDateTime.now()
        )
        val key = "partner:$transactionId"
        whenever(valueOps.get(key)).thenReturn(null)

        val result = processor.process(transaction)

        System.out.println(result)

        verify(redisTemplate, never()).delete(key)
        assert(result is MismatchedResult)
    }
}
