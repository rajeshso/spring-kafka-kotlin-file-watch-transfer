package com.n2.directorywatcher

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.io.File
import java.nio.file.*
import java.nio.file.WatchKey

fun File.asWatchChannel(
) = WatchChannel(
        agentID = 1,
        file = this
)

open class WatchChannel(
        agentID: Int = 1,
        val file: File,
        private val channel: Channel<WEvent> = Channel()
) : Channel<WEvent> by channel {

    val agentID: Int = agentID
    private val watchService: WatchService = FileSystems.getDefault().newWatchService()
    private val registeredKeys = ArrayList<WatchKey>()
    private val path: Path = if (file.isFile) {
        file.parentFile
    } else {
        file
    }.toPath()


    private fun registerPaths() {
        //TODO: Is clearing required ?
        registeredKeys.apply {
            forEach { it.cancel() }
            clear()
        }
        registeredKeys += path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE)
    }

    init {
        registerPaths()
        GlobalScope.launch(Dispatchers.IO) {

            channel.send(
                    WEvent(
                            agentID = agentID,
                            file = path.toFile(),
                            kind = WEvent.Kind.Initialized
                    ))

            while (!isClosedForSend) {
                val monitorKey = watchService.take()
                val dirPath = monitorKey.watchable() as? Path ?: break
                monitorKey.pollEvents().forEach {
                    val eventPath = dirPath.resolve(it.context() as Path)
                    val eventType = WEvent.Kind.Created
                    val event = WEvent(
                            agentID = agentID,
                            file = eventPath.toFile(),
                            content = eventPath.toFile().readBytes(),
                            kind = eventType
                    )
                    // if any folder is created
                    if (event.kind in listOf(WEvent.Kind.Created) &&
                            event.file.isDirectory) {
                        TODO("Warn that this is a directory and not to do anything about it")
                    }
                    channel.send(event)
                }

                if (!monitorKey.reset()) {
                    monitorKey.cancel()
                    close()
                    break
                }
                else if (isClosedForSend) {
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
        return channel.close(cause)
    }

}
