package com.batch.settlement.repository

import com.batch.settlement.domain.Transactions
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime

interface TransactionsRepository  : JpaRepository<Transactions, Long> {
    fun findAllByTransactionDateBetween(from: LocalDateTime, to: LocalDateTime): List<Transactions>
}