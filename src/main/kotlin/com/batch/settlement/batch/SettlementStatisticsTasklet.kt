package com.batch.settlement.batch

import com.batch.settlement.batch.result.SettlementStatistics
import com.batch.settlement.domain.CancellationTransactions
import com.batch.settlement.domain.MatchedTransactions
import com.batch.settlement.domain.SuspiciousTransactions
import com.batch.settlement.repository.CancellationTransactionsRepository
import com.batch.settlement.repository.MatchedTransactionsRepository
import com.batch.settlement.repository.SuspiciousTransactionsRepository
import com.batch.settlement.service.NotificationService
import org.springframework.batch.core.StepContribution
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.LocalDate

@Component
class SettlementStatisticsTasklet (
    private val matchedRepository: MatchedTransactionsRepository,
    private val cancellationRepository: CancellationTransactionsRepository,
    private val suspiciousRepository: SuspiciousTransactionsRepository,
    private val notificationService: NotificationService
) : Tasklet {
    override fun execute(contribution: StepContribution, chunkContext: ChunkContext): RepeatStatus {
        val transactionDate = LocalDate.now()
        val matchedList: List<MatchedTransactions> = matchedRepository.findByTransactionDate(transactionDate)
        val cancellationList: List<CancellationTransactions> = cancellationRepository.findByTransactionDate(transactionDate)
        val suspiciousList: List<SuspiciousTransactions> = suspiciousRepository.findByTransactionDate(transactionDate)
        val stats = SettlementStatistics(
            date = transactionDate,
            matchedCount = matchedList.size.toLong(),
            netProfit = matchedList.fold(BigDecimal.ZERO) { acc, m -> acc + m.netProfit },
            grossProfit = matchedList.fold(BigDecimal.ZERO) { acc, m -> acc + m.grossProfit },
            cancellationCount = cancellationList.size.toLong(),
            cancellationLoss = cancellationList.fold(BigDecimal.ZERO) { acc, c -> acc + c.loss },
            suspiciousCount = suspiciousList.size.toLong(),
            suspiciousLoss = suspiciousList.fold(BigDecimal.ZERO) { acc, s -> acc + s.loss }
        )
        notificationService.sendNotification(stats)
        return RepeatStatus.FINISHED
    }
}