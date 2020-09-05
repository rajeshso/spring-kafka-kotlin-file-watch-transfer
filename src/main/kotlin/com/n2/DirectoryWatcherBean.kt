package com.n2

import com.n2.directorywatcher.WatchChannel
import com.n2.directorywatcher.asWatchChannel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.io.File
import java.util.concurrent.TimeUnit
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy


@Component
class DirectoryWatcherBean {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Autowired
    lateinit var kProducer: KProducer

    @Value("\${spring-kafka-kotlin-file-watch-transfer.filepath}")
    lateinit var filepath: String

    @Value("\${spring.kafka.producer.client-id}")
    lateinit var agentID: String

    @Value("\${spring.kafka.producer.properties.max.request.size}")
    lateinit var maxRequestSize: String

    @ExperimentalCoroutinesApi
    var watchChannel: WatchChannel? = null

    var job: Job? = null

    @PostConstruct
    fun init() {
        logger.info("directory watch constructed")
        val currentDirectory = File(filepath)
        if (!validate(currentDirectory)) throw IllegalStateException("$currentDirectory is invalid")

        watchChannel = currentDirectory.asWatchChannel(agentID.toInt(), maxRequestSize.toInt())

        job = GlobalScope.launch {
            watchChannel?.consumeEach { event ->
                kProducer.send(event.toWEventAvro())
                println(KConsumer.latch.await(10,TimeUnit.SECONDS))
            }
        }
    }

    @ExperimentalCoroutinesApi
    @PreDestroy
    fun destroy() {
        logger.info("closed the watch channel")
        watchChannel?.close()
        job?.cancel()
    }

    fun validate(file: File): Boolean = file.exists() && file.isDirectory && file.canRead()
}