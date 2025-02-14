package com.batch.settlement.batch.result

import java.math.BigDecimal
import java.time.LocalDate

data class MatchedResult(
    override val transactionId: String,
    val amount: BigDecimal,
    val netProfit: BigDecimal,
    val grossProfit: BigDecimal,
    override val transactionDate: LocalDate
) : SettlementResult()