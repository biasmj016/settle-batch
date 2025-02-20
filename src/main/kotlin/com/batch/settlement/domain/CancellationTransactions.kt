package com.batch.settlement.domain

import com.batch.settlement.batch.result.MismatchedResult
import jakarta.persistence.*
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
){
    constructor(result: MismatchedResult) : this(
        transactionId = result.transactionId,
        amount = result.amount,
        loss = result.loss,
        transactionDate = result.transactionDate
    )
}