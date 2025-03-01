package com.batch.settlement.kafka

import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class SettlementEventProducer(private val kafkaTemplate: KafkaTemplate<String, String>) {
    fun sendEvent(message: String) {
        kafkaTemplate.send("settlement_events", message)
    }
}