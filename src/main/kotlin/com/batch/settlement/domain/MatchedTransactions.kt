package com.batch.settlement.domain

import com.batch.settlement.batch.result.MatchedResult
import jakarta.persistence.Entity
import jakarta.persistence.Table
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.math.BigDecimal
import java.time.LocalDate

@Entity
@Table(name = "matched_transactions")
data class MatchedTransactions(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val transactionId: String = "",
    val amount: BigDecimal = BigDecimal.ZERO,
    val netProfit: BigDecimal = BigDecimal.ZERO,
    val grossProfit: BigDecimal = BigDecimal.ZERO,
    val transactionDate: LocalDate = LocalDate.now()
){
    constructor(result: MatchedResult) : this(
        transactionId = result.transactionId,
        amount = result.amount,
        netProfit = result.customerFee.subtract(result.partnerFee),// 순이익(고객 수수료 - 파트너 수수료)
        grossProfit = result.customerFee,//총 수익(고객 수수료)
        transactionDate = result.transactionDate
    )
}