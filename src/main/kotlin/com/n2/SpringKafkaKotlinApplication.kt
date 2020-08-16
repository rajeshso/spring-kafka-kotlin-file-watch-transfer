package com.n2

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.kafka.annotation.EnableKafka

@SpringBootApplication
@EnableKafka
@EnableConfigurationProperties
open class SpringKafkaKotlinApplication {
}
fun main(args: Array<String>) {
	runApplication<SpringKafkaKotlinApplication>(*args)
}


