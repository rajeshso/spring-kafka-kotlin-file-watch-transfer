package com.n2.directorywatcher

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
}