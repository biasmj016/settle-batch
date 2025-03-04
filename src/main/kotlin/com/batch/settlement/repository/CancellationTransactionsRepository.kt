package com.batch.settlement.repository

import com.batch.settlement.domain.CancellationTransactions
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

interface CancellationTransactionsRepository : JpaRepository<CancellationTransactions, Long> {
    fun findByTransactionDate(date: LocalDate): List<CancellationTransactions>

    @Modifying
    @Transactional
    @Query("DELETE FROM CancellationTransactions")
    fun deleteAllTransactions()
}