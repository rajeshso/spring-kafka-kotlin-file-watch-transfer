package com.n2.directorywatcher

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File

class FileAsWatchChannel {
    @Test
    fun `the agent id should be configurable from the extension function`() {
        val currentDirectory = File(".")
        val channel = currentDirectory.asWatchChannel(2000)
        assertThat(channel).isInstanceOf(WatchChannel::class.java)
        assertThat(channel.agentID).isEqualTo(2000)
    }
    @Test
    fun `the agent id should take a default value from the extension function`() {
        val currentDirectory = File(".")
        val channel = currentDirectory.asWatchChannel()
        assertThat(channel).isInstanceOf(WatchChannel::class.java)
        assertThat(channel.agentID).isEqualTo(1)
    }
}