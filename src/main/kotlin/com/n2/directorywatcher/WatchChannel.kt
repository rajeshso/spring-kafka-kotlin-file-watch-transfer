package com.n2.directorywatcher

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.*

@ExperimentalCoroutinesApi
fun File.asWatchChannel(
         agentID:Int = 1,
         maxRequestSize: Int = 2097176
) = WatchChannel(
        agentID = agentID,
        maxRequestSize = maxRequestSize,
        file = this
)

@ExperimentalCoroutinesApi
open class WatchChannel(
        val agentID: Int = 1,
        val maxRequestSize:Int = 2097176,
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
                        logger.warn("Warn that ${eventPath.toFile().absolutePath} is a directory. So ignore the event")
                    } else {
                        val content = eventPath.toFile().readBytes()
                        if (content.size< maxRequestSize) {
                            val event = WEvent(
                                    agentID = agentID,
                                    fileName = eventPath.toFile().absolutePath,
                                    timeStamp = eventPath.toFile().lastModified(),
                                    kind = eventType,
                                    content = content
                            )
                            channel.send(event)
                        }else {
                            logger.warn("Warn that ${eventPath.toFile().absolutePath} has a size of ${content.size} which is more than the permitted ${maxRequestSize}.So ignore the event")
                        }
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
