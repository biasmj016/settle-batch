package com.batch.settlement.service

import com.batch.settlement.batch.result.SettlementStatistics
import com.batch.settlement.kafka.SettlementEventProducer
import org.springframework.stereotype.Service

@Service
class NotificationService(private val settlementEventProducer: SettlementEventProducer) {

    fun sendNotification(statistics: SettlementStatistics) {
        val message = """
            [정산 보고서 - ${statistics.date}]
            정산건: ${statistics.matchedCount}건, 순이익: ${statistics.netProfit}원, 총 이익: ${statistics.grossProfit}원
            취소건: ${statistics.cancellationCount}건, 총 손실: ${statistics.cancellationLoss}원
            이상거래건: ${statistics.suspiciousCount}건, 총 손실: ${statistics.suspiciousLoss}원
        """.trimIndent()
        println(message)

        settlementEventProducer.sendEvent(message)

    }
}