package com.batch.settlement.repository

import com.batch.settlement.domain.Transactions
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

interface TransactionsRepository  : JpaRepository<Transactions, Long> {
    fun findAllByTransactionDateBetween(from: LocalDateTime, to: LocalDateTime): List<Transactions>

    @Modifying
    @Transactional
    @Query("DELETE FROM Transactions")
    fun deleteAllTransactions()
}