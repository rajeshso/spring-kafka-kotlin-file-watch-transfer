package com.n2

import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import java.util.concurrent.CountDownLatch

@Component
class KConsumer {

    private val logger = LoggerFactory.getLogger(javaClass)

    // More on SpEl here https://docs.spring.io/spring/docs/4.3.10.RELEASE/spring-framework-reference/html/expressions.html
    @KafkaListener(topics = arrayOf("#{'\${spring.kafka.template.default-topic}'}"))
    fun processMessage(message: String) {
        println("got message: " + message)
        logger.info("got message: {}", message)
        received = message
        latch.countDown()
    }

    companion object {
        val latch = CountDownLatch(1)
        lateinit var received:String
    }
}
