package com.n2

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.ByteArrayDeserializer
import org.apache.kafka.common.serialization.StringDeserializer
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.listener.ContainerProperties
import org.springframework.kafka.listener.KafkaMessageListenerContainer
import org.springframework.kafka.listener.MessageListener
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.kafka.test.utils.ContainerTestUtils
import org.springframework.kafka.test.utils.KafkaTestUtils
import org.springframework.test.context.TestPropertySource
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue


@TestPropertySource(properties = [
    "spring.kafka.bootstrap-servers=\'localhost:9092\'",
    "spring.kafka.producer.client-id=1",
    "spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringDeserializer",
    "spring.kafka.producer.value-serializer=org.apache.kafka.common.serialization.ByteArraySerializer",
    "spring.kafka.template.default-topic=\'simple-message-topic\'",
    "spring.kafka.consumer.client-id=2",
    "spring.kafka.consumer.group-id=200",
    "spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer",
    "spring.kafka.consumer.value-deserializer=org.apache.kafka.common.serialization.ByteArrayDeserializer"
])
@EmbeddedKafka(partitions = 1, topics = [ "simple-message-topic"])
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class KProducerTest {
    var records: BlockingQueue<ConsumerRecord<String, ByteArray>>? = null

    var container: KafkaMessageListenerContainer<String, ByteArray>? = null

    @Value("\${spring.kafka.consumer.group-id}")
    lateinit var groupId: String

    @Value("\${spring.kafka.template.default-topic}")
    lateinit var topic: String

    @Autowired
    lateinit var embeddedKafkaBroker: EmbeddedKafkaBroker

    @BeforeAll
    fun setUp(embeddedKafkaBroker: EmbeddedKafkaBroker?) {
        val configs: Map<String, Any> = HashMap(KafkaTestUtils.consumerProps(groupId, "false", embeddedKafkaBroker))
        val consumerFactory = DefaultKafkaConsumerFactory(configs, StringDeserializer(), ByteArrayDeserializer())
        val containerProperties = ContainerProperties(topic)
        container = KafkaMessageListenerContainer(consumerFactory, containerProperties)
        records = LinkedBlockingQueue()
        //container?.setupMessageListener(MessageListener<String?, ByteArray?> { e: ConsumerRecord<String, ByteArray>? -> records.add(e) } as MessageListener<String?, ByteArray?>)
        container?.start()
        ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker!!.partitionsPerTopic)
    }
}