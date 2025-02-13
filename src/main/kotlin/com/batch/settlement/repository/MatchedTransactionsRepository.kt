package com.batch.settlement.repository

import com.batch.settlement.domain.MatchedTransactions
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

interface MatchedTransactionsRepository : JpaRepository<MatchedTransactions, Long> {
    fun findByTransactionDate(date: LocalDate): List<MatchedTransactions>
}