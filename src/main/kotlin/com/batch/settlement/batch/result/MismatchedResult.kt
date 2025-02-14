package com.batch.settlement.batch.result

import java.math.BigDecimal
import java.time.LocalDate

data class MismatchedResult(
    override val transactionId: String,
    val amount: BigDecimal,
    private val loss: BigDecimal = BigDecimal(1000),
    override val transactionDate: LocalDate
) : SettlementResult()