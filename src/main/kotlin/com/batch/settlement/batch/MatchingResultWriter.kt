package com.batch.settlement.batch

import com.batch.settlement.batch.result.MatchedResult
import com.batch.settlement.batch.result.MismatchedResult
import com.batch.settlement.batch.result.SettlementResult
import com.batch.settlement.domain.CancellationTransactions
import com.batch.settlement.domain.MatchedTransactions
import com.batch.settlement.repository.CancellationTransactionsRepository
import com.batch.settlement.repository.MatchedTransactionsRepository
import org.springframework.batch.item.Chunk
import org.springframework.batch.item.ItemWriter
import org.springframework.stereotype.Component

@Component
class MatchingResultWriter (
    private val matchedRepository: MatchedTransactionsRepository,
    private val cancellationRepository: CancellationTransactionsRepository
) : ItemWriter<SettlementResult> {
    override fun write(chunk: Chunk<out SettlementResult>) {
        val items = chunk.items
        val matchedList = items.filterIsInstance<MatchedResult>().map { MatchedTransactions(it) }
        val cancellationList = items.filterIsInstance<MismatchedResult>().map { CancellationTransactions(it) }
        if (matchedList.isNotEmpty()) matchedRepository.saveAll(matchedList)
        if (cancellationList.isNotEmpty()) cancellationRepository.saveAll(cancellationList)
    }
}