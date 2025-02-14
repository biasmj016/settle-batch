package com.batch.settlement.batch.result

import java.math.BigDecimal
import java.time.LocalDate

data class MismatchedResult(
    override val transactionId: String,
    val amount: BigDecimal,
    val loss: BigDecimal,
    override val transactionDate: LocalDate
) : SettlementResult()