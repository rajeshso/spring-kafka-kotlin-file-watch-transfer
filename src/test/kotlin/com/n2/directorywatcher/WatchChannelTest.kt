package com.n2.directorywatcher

import com.n2.directorywatcher.WEvent.Kind
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.take
import kotlinx.coroutines.channels.toList
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path
import java.util.Comparator.reverseOrder

class WatchChannelTest {

    val dir: Path = Files.createTempDirectory("my-dir")
    val fileName = "test-file.txt"

    @BeforeEach
    fun `set up directory`() {
        val fileToCreatePath: Path = dir.resolve(fileName)
        val newFilePath: Path = Files.createFile(fileToCreatePath)
        assertThat(Files.exists(newFilePath))
    }

    @AfterEach
    fun `clean up`() {
        Files.walk(dir)
                .sorted(reverseOrder())
                .map(Path::toFile)
                .forEach{it.delete()}
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `watch current directory for initalization event`() {
        runBlockingTest {
            val currentDirectory = dir.toFile()
            val watchChannel = currentDirectory.asWatchChannel()
            assertThat(watchChannel.isClosedForSend).isFalse()
            assertThat(watchChannel.file.absolutePath).isEqualTo(currentDirectory.absolutePath)
            launch {
                watchChannel.consumeEach { event ->
                    // there is always the first event triggered and here we only test that
                    assertThat(event.kind).isEqualTo(Kind.Initialized)
                    assertThat(event.fileName).isEqualTo(currentDirectory.absolutePath)
                }
            }
            assertThat(watchChannel.isClosedForSend).isFalse()
            watchChannel.close()
            assertThat(watchChannel.isClosedForSend).isTrue()
        }
    }

    @Test
    fun `watch current directory for created event`() {
        val scope = TestCoroutineScope()
        val list = mutableListOf<WEvent>()
        scope.foo(list)
        val fileToCreatePath1: Path = dir.resolve("abc1.txt")
        Files.createFile(fileToCreatePath1)
        val except = scope.uncaughtExceptions
        println("123")
    }
    fun CoroutineScope.foo(list: MutableList<WEvent>) {
        async {
            val watchChannel = WatchChannel(1,dir.toFile())
            watchChannel.consumeEach { event ->
                list.add(event)
            }
        }
    }

}