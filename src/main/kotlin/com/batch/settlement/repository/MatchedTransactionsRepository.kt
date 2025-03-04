package com.batch.settlement.repository

import com.batch.settlement.domain.MatchedTransactions
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

interface MatchedTransactionsRepository : JpaRepository<MatchedTransactions, Long> {
    fun findByTransactionDate(date: LocalDate): List<MatchedTransactions>

    @Modifying
    @Transactional
    @Query("DELETE FROM MatchedTransactions")
    fun deleteAllTransactions()
}