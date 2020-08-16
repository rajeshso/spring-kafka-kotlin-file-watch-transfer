package com.n2

import com.n2.directorywatcher.WatchChannel
import com.n2.directorywatcher.asWatchChannel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.io.File
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy


@Component
class DirectoryWatcherBean {

    @Autowired
    lateinit var kProducer: KProducer

    var watchChannel : WatchChannel? = null

    @PostConstruct
    fun init() {
        println("post constructed")
        val currentDirectory = File("/Users/rajesh/Workspace/KafkaSpace/spirng-kafka-kotlin/testdir")

        watchChannel = currentDirectory.asWatchChannel()

        GlobalScope.launch {
            watchChannel?.consumeEach { event ->
                kProducer.send(" ${event.kind.name}  ${event.file.canonicalPath}")
            }
        }
    }

    @PreDestroy
    fun destroy(): Unit {
        println("closed the watch channel")
        watchChannel?.close()
    }
}