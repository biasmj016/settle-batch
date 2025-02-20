package com.batch.settlement.batch.result

import java.math.BigDecimal
import java.time.LocalDate

data class SettlementStatistics (
    val date: LocalDate,
    val matchedCount: Long,
    val netProfit: BigDecimal,
    val grossProfit: BigDecimal,
    val cancellationCount: Long,
    val cancellationLoss: BigDecimal,
    val suspiciousCount: Long,
    val suspiciousLoss: BigDecimal
)
