package com.batch.settlement.repository

import com.batch.settlement.domain.CancellationTransactions
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

interface CancellationTransactionsRepository : JpaRepository<CancellationTransactions, Long> {
    fun findByTransactionDate(date: LocalDate): List<CancellationTransactions>
}