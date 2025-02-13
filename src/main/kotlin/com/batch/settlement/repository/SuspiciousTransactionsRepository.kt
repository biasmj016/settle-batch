package com.batch.settlement.repository

import com.batch.settlement.domain.SuspiciousTransactions
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

interface SuspiciousTransactionsRepository : JpaRepository<SuspiciousTransactions, Long> {
    fun findByTransactionDate(date: LocalDate): List<SuspiciousTransactions>
}