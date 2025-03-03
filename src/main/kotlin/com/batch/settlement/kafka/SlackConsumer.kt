package com.batch.settlement.kafka

import com.batch.settlement.service.SlackNotificationService
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service

@Service
class SlackConsumer(private val slackNotificationService: SlackNotificationService) {
    private val logger = LoggerFactory.getLogger(SlackConsumer::class.java)

    @KafkaListener(topics = ["settlement_events"], groupId = "slack_group")
    fun consumeEvent(message: String) {
        logger.info("SlackConsumer received event: $message")
        try {
            slackNotificationService.sendSlackNotification(message)
        } catch (ex: Exception) {
            logger.error("Error sending Slack notification", ex)
        }
    }
}