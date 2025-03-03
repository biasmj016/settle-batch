package com.batch.settlement.kafka

import com.batch.settlement.domain.AuditLog
import com.batch.settlement.repository.AuditLogRepository
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class AuditLogConsumer(private val auditLogRepository: AuditLogRepository) {
    private val logger = LoggerFactory.getLogger(AuditLogConsumer::class.java)

    @KafkaListener(topics = ["settlement_events"], groupId = "audit_log_group")
    fun consumeEvent(eventMessage: String) {
        logger.info("AuditLogConsumer received event: $eventMessage")
        try {
            val auditLog = AuditLog(event = eventMessage, receivedAt = LocalDateTime.now())
            auditLogRepository.save(auditLog)
        } catch (ex: Exception) {
            logger.error("Error saving audit log", ex)
        }
    }
}