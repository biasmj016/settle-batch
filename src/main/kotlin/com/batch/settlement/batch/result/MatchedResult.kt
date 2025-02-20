package com.batch.settlement.batch.result

import java.math.BigDecimal
import java.time.LocalDate

data class MatchedResult(
    override val transactionId: String,
    val amount: BigDecimal,
    override val transactionDate: LocalDate,
    val customerFee: BigDecimal = BigDecimal(1000),
    val partnerFee: BigDecimal = BigDecimal(500)
) : SettlementResult()