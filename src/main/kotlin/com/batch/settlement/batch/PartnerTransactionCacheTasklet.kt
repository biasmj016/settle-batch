package com.batch.settlement.batch

import com.batch.settlement.domain.PartnerTransactions
import com.batch.settlement.repository.PartnerTransactionsRepository
import org.springframework.batch.core.StepContribution
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.LocalTime

@Component
class PartnerTransactionCacheTasklet(
    private val partnerTransactionRepository: PartnerTransactionsRepository,
    private val redisTemplate: RedisTemplate<String, PartnerTransactions>
) : Tasklet {
    override fun execute(contribution: StepContribution, chunkContext: ChunkContext): RepeatStatus {
        val today = LocalDate.now()
        partnerTransactionRepository.findAllByTransactionDateBetween(today.atStartOfDay(), today.atTime(LocalTime.MAX))
            .forEach { pt ->
                redisTemplate.opsForValue().set("partner:${pt.transactionId}", pt)
            }
        return RepeatStatus.FINISHED
    }
}