package com.batch.settlement.domain

import jakarta.persistence.Entity
import jakarta.persistence.Table
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.math.BigDecimal
import java.time.LocalDate

@Entity
@Table(name = "cancellation_transactions")
data class CancellationTransactions(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val transactionId: String = "",
    val amount: BigDecimal = BigDecimal.ZERO,
    val loss: BigDecimal = BigDecimal(1000), // 취소 시 고객 환불로 인한 손실 (1000원)
    val transactionDate: LocalDate = LocalDate.now()
)