package com.batch.settlement.repository

import com.batch.settlement.domain.PartnerTransactions
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime

interface PartnerTransactionsRepository : JpaRepository<PartnerTransactions, Long> {
    fun findAllByTransactionDateBetween(from: LocalDateTime, to: LocalDateTime): List<PartnerTransactions>
}