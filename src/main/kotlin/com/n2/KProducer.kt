package com.n2

import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import java.util.*

@Component
class KProducer(private val kafkaTemplate: KafkaTemplate<String, com.n2.event.WEvent>) {

    fun send(wevent: com.n2.event.WEvent) {
        kafkaTemplate.sendDefault(UUID.randomUUID().toString(), wevent)
    }

}
