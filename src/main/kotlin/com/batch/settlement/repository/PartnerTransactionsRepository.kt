package com.batch.settlement.repository

import com.batch.settlement.domain.PartnerTransactions
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

interface PartnerTransactionsRepository : JpaRepository<PartnerTransactions, Long> {
    fun findAllByTransactionDateBetween(from: LocalDateTime, to: LocalDateTime): List<PartnerTransactions>

    @Modifying
    @Transactional
    @Query("DELETE FROM PartnerTransactions")
    fun deleteAllTransactions()
}