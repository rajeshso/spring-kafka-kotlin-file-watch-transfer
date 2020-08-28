package com.n2.directorywatcher

import java.nio.ByteBuffer

data class WEvent(
        val agentID: Int,
        val fileName: String,
        val timeStamp: Long,
        val kind: Kind,
        val content: ByteArray = ByteArray(0)
) {
    enum class Kind(val kind: String) {
        Initialized("initialized"),
        Created("new file created event"),
    }
    fun toWEventAvro(): com.n2.event.WEvent = com.n2.event.WEvent(
                                                agentID,
                                                fileName,
                                                timeStamp,
                                                kind.name,
                                                ByteBuffer.wrap(content)
                                                )
}
