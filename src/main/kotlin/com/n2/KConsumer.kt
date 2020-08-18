package com.n2

import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class KConsumer {

    private val logger = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = ["simple-message-topic"])
    fun processMessage(message: String) {
        logger.info("got message: {}", message)
    }
}
