package com.n2.directorywatcher

import java.io.File

data class WEvent(
        val agentID: Int,
        val file: File,
        val content: ByteArray = ByteArray(0),
        val kind: Kind
) {
    enum class Kind(val kind: String) {
        Initialized("initialized"),
        Created("created"),
    }
}