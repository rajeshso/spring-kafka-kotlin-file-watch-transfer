package com.n2.directorywatcher

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.io.File

class WatchChannelTest {
    @ExperimentalCoroutinesApi
    @Test
    fun `watch current directory for initalization event`() {
        runBlockingTest {
            val currentDirectory = File(System.getProperty("user.dir"))

            val watchChannel = currentDirectory.asWatchChannel()

            assertThat(watchChannel.isClosedForSend).isFalse()
            assertThat(watchChannel.file.absolutePath).isEqualTo(currentDirectory.absolutePath)

            launch {
                watchChannel.consumeEach { event ->
                    // there is always the first event triggered and here we only test that
                    assertThat(event.kind).isEqualTo(WEvent.Kind.Initialized)
                    assertThat(event.fileName).isEqualTo(currentDirectory.absolutePath)
                }
            }

            assertThat(watchChannel.isClosedForSend).isFalse()

            watchChannel.close()

            assertThat(watchChannel.isClosedForSend).isTrue()
        }
    }

    @Disabled
    fun `watch current directory for created event`() {
        runBlockingTest {
            val currentDirectory = File(System.getProperty("user.dir"))
            val fileName = System.getProperty("user.dir") + "/" + "abc.txt"
            val watchChannel = currentDirectory.asWatchChannel()

            assertThat(watchChannel.isClosedForSend).isFalse()
            assertThat(watchChannel.file.absolutePath).isEqualTo(currentDirectory.absolutePath)
            val newFile = File(fileName).createNewFile()
            launch {
                watchChannel.consumeEach { event ->
                    // there is always the first event triggered and here we only test that
                    assertThat(event.kind).isEqualTo(WEvent.Kind.Created)
                    assertThat(event.fileName).isEqualTo(newFile)
                }
            }

            assertThat(watchChannel.isClosedForSend).isFalse()
            watchChannel.close()
            assertThat(watchChannel.isClosedForSend).isTrue()
        }
    }
}