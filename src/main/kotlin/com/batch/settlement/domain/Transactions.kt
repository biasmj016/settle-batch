package com.batch.settlement.domain

import jakarta.persistence.Entity
import jakarta.persistence.Table
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "transactions")
data class Transactions(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val transactionId: String = "",
    val amount: BigDecimal = BigDecimal.ZERO,
    val fee: BigDecimal = BigDecimal(1000), // 결제 시 고객 수수료 (항상 1000원으로 가정)
    val transactionDate: LocalDateTime = LocalDateTime.now()
)
