package com.batch.settlement.config

import org.apache.kafka.streams.StreamsBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class KafkaStreamsBuilderConfig {
    @Bean
    fun streamsBuilder(): StreamsBuilder = StreamsBuilder()
}