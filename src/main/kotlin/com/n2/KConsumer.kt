package com.n2

import com.n2.event.WEvent
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import java.util.concurrent.CountDownLatch

@Component
class KConsumer {

   private val logger = LoggerFactory.getLogger(javaClass)

   @KafkaListener(topics = arrayOf("#{'\${spring.kafka.template.default-topic}'}"),  autoStartup = "true")
   fun processMessage(message: ConsumerRecord<String, WEvent>) {
        println("got message: " + message.key()+ " "+ message.value())
        logger.info("got message: {}", message.value())
        received = message.value()
        latch.countDown()
    }

    companion object {
        val latch = CountDownLatch(1)
        lateinit var received: WEvent
    }
}
