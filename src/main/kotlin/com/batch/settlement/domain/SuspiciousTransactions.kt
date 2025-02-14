package com.batch.settlement.domain

import jakarta.persistence.Entity
import jakarta.persistence.Table
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.math.BigDecimal
import java.time.LocalDate

@Entity
@Table(name = "suspicious_transactions")
data class SuspiciousTransactions(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val transactionId: String = "",
    val amount: BigDecimal = BigDecimal.ZERO,
    val loss: BigDecimal = BigDecimal(1000),// 확인이 필요한 이상 거래 건이나 임시로 손실로 가정
    val transactionDate: LocalDate = LocalDate.now()
)