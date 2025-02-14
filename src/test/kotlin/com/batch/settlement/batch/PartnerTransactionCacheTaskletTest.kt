package com.batch.settlement.batch

import com.batch.settlement.domain.PartnerTransactions
import com.batch.settlement.repository.PartnerTransactionsRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.batch.core.StepContribution
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ValueOperations
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalTime

@ExtendWith(MockitoExtension::class)
class PartnerTransactionCacheTaskletTest {

    private val partnerTransactionRepository: PartnerTransactionsRepository = mock()
    private val redisTemplate: RedisTemplate<String, PartnerTransactions> = mock()
    private val valueOps: ValueOperations<String, PartnerTransactions> = mock()

    private lateinit var tasklet: PartnerTransactionCacheTasklet

    @BeforeEach
    fun setUp() {
        whenever(redisTemplate.opsForValue()).thenReturn(valueOps)
        tasklet = PartnerTransactionCacheTasklet(partnerTransactionRepository, redisTemplate)
    }

    @Test
    fun execute() {
        val start = LocalDate.now().atStartOfDay()
        val end = LocalDate.now().atTime(LocalTime.MAX)
        val partnerTransaction = PartnerTransactions(
            transactionId = "test-id",
            amount = BigDecimal("1000"),
            fee = BigDecimal("500"),
            transactionDate = start.plusHours(1)
        )
        whenever(partnerTransactionRepository.findAllByTransactionDateBetween(start, end))
            .thenReturn(listOf(partnerTransaction))

        tasklet.execute(mock<StepContribution>(), mock<ChunkContext>())

        verify(valueOps).set("partner:test-id", partnerTransaction)
    }
}
