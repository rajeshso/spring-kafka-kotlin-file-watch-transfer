package com.n2.directorywatcher

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.*

fun File.asWatchChannel(
) = WatchChannel(
        agentID = 1,
        file = this
)

@ExperimentalCoroutinesApi
open class WatchChannel(
        agentID: Int = 1,
        val file: File,
        val launchScope: CoroutineScope = GlobalScope,
        val dispatcher: CoroutineDispatcher = Dispatchers.IO,
        private val channel: Channel<WEvent> = Channel()
) : Channel<WEvent> by channel {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val watchService: WatchService = FileSystems.getDefault().newWatchService()
    private val registeredKeys = ArrayList<WatchKey>()
    private val path: Path = if (file.isFile) {
        file.parentFile
    } else {
        file
    }.toPath()
    var job: Job? = null


    private fun registerPaths() {
        registeredKeys.apply {
            forEach { it.cancel() }
            clear()
        }
        registeredKeys += path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE)
    }

    init {
        registerPaths()
        job = launchScope.launch(dispatcher) {

            channel.send(
                    WEvent(
                            agentID = agentID,
                            fileName = path.toFile().absolutePath,
                            timeStamp = path.toFile().lastModified(),
                            kind = WEvent.Kind.Initialized
                    ))

            while (!isClosedForSend) {
                val monitorKey = watchService.take()
                val dirPath = monitorKey.watchable() as? Path ?: break
                monitorKey.pollEvents().forEach {
                    val eventPath = dirPath.resolve(it.context() as Path)
                    val eventType = WEvent.Kind.Created
                    if (eventType in listOf(WEvent.Kind.Created) &&
                            eventPath.toFile().isDirectory) {
                        logger.warn("Warn that ${eventPath.toFile().absolutePath} is a directory and ignore the event")
                    } else {
                        val event = WEvent(
                                agentID = agentID,
                                fileName = eventPath.toFile().absolutePath,
                                timeStamp = eventPath.toFile().lastModified(),
                                kind = eventType,
                                content = eventPath.toFile().readBytes()
                        )
                        channel.send(event)
                    }
                }

                if (!monitorKey.reset()) {
                    monitorKey.cancel()
                    close()
                    break
                } else if (isClosedForSend) {
                    break
                }
            }
        }
    }

    override fun close(cause: Throwable?): Boolean {
        registeredKeys.apply {
            forEach { it.cancel() }
            clear()
        }
        job?.cancel()
        return channel.close(cause)
    }

}
