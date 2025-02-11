package com.batch.settlement

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SettleBatchApplication

fun main(args: Array<String>) {
    runApplication<SettleBatchApplication>(*args)
}
