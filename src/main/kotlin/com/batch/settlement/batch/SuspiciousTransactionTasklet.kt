package com.batch.settlement.batch

import com.batch.settlement.domain.PartnerTransactions
import com.batch.settlement.domain.SuspiciousTransactions
import com.batch.settlement.repository.SuspiciousTransactionsRepository
import org.springframework.batch.core.StepContribution
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component

@Component
class SuspiciousTransactionTasklet(
    private val redisTemplate: RedisTemplate<String, PartnerTransactions>,
    private val suspiciousRepository: SuspiciousTransactionsRepository
) : Tasklet {
    override fun execute(contribution: StepContribution, chunkContext: ChunkContext): RepeatStatus =
        redisTemplate.keys("partner:*").takeIf { it.isNotEmpty() }?.let { keys ->
            keys.mapNotNull { redisTemplate.opsForValue().get(it)?.let(::SuspiciousTransactions) }
                .takeIf { it.isNotEmpty() }
                ?.also(suspiciousRepository::saveAll)
            redisTemplate.delete(keys)
        }.let { RepeatStatus.FINISHED }
}
