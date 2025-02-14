package com.batch.settlement.domain

import jakarta.persistence.Entity
import jakarta.persistence.Table
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "partner_transactions")
data class PartnerTransactions(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val transactionId: String = "",
    val amount: BigDecimal = BigDecimal.ZERO,
    val fee: BigDecimal = BigDecimal(500),// 파트너사 서비스 이용료 (항상 500원으로 가정)
    val transactionDate: LocalDateTime = LocalDateTime.now()
)
