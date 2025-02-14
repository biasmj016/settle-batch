package com.batch.settlement.batch.result

import java.time.LocalDate

sealed class SettlementResult {
    abstract val transactionId: String
    abstract val transactionDate: LocalDate
}