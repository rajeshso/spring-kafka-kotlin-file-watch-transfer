package com.n2

import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import java.util.*

@Component
class KProducer(private val kafkaTemplate: KafkaTemplate<String, ByteArray>) {

  fun send(message: String) {
    kafkaTemplate.sendDefault(UUID.randomUUID().toString(),message.toByteArray())
  }

}
