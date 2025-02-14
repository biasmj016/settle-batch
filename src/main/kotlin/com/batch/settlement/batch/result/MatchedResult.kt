package com.batch.settlement.batch.result

import java.math.BigDecimal
import java.time.LocalDate

data class MatchedResult(
    override val transactionId: String,
    val amount: BigDecimal,
    override val transactionDate: LocalDate,
    private val customerFee: BigDecimal = BigDecimal(1000),
    private val partnerFee: BigDecimal = BigDecimal(500)
) : SettlementResult()