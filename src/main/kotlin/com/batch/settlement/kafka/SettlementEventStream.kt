package com.batch.settlement.kafka

import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.KStream
import org.apache.kafka.streams.kstream.Materialized
import org.apache.kafka.streams.kstream.TimeWindows
import org.apache.kafka.streams.kstream.Windowed
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

@Configuration
class SettlementEventStream {

    private val logger = LoggerFactory.getLogger(SettlementEventStream::class.java)

    @Bean
    fun kStream(builder: StreamsBuilder): KStream<String, String> {
        val stream = builder.stream<String, String>("settlement_events")

        val windowedCounts = stream
            .groupByKey()
            .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofMinutes(1)))
            .count(Materialized.with(Serdes.String(), Serdes.Long()))

        windowedCounts.toStream().foreach { windowedKey: Windowed<String>, count: Long ->
            logger.info("Streams Group - Key: ${windowedKey.key()}, Count: $count, Window: [${windowedKey.window().start()}, ${windowedKey.window().end()}]")
        }
        return stream
    }
}