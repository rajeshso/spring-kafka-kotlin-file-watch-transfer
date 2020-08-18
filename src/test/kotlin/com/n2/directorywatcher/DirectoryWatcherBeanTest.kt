package com.n2.directorywatcher

import com.n2.DirectoryWatcherBean
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DirectoryWatcherBeanTest {

    val testDir = "testdir1"

    @BeforeEach
    fun setup() {
        Files.createDirectories(Path.of(testDir))
    }

    @AfterAll
    fun tearDown() {
        Files.walk(Path.of(testDir))
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach { f -> f.delete() }
    }

    @Test
    fun `validate should be false for an invalid file`() {
        val noFile = File("")
        val directoryWatcherBean = DirectoryWatcherBean()
        val result = directoryWatcherBean.validate(noFile)
        assertThat(result).isFalse()
    }

    @Test
    fun `validate should be false for a file`() {
        val file = File(testDir + "//abc.txt")
        val directoryWatcherBean = DirectoryWatcherBean()
        val result = directoryWatcherBean.validate(file)
        assertThat(result).isFalse()
    }

    @Test
    fun `validate should be false for a can't read directory`() {
        val path = Paths.get(testDir)
        path.toFile().setReadable(false)
        val directoryWatcherBean = DirectoryWatcherBean()
        val result = directoryWatcherBean.validate(path.toFile())
        assertThat(result).isFalse()
        path.toFile().setReadable(true)
    }

    @Test
    fun `validate should be true for a directory that exists and can be read`() {
        val path = Paths.get(testDir)
        val directoryWatcherBean = DirectoryWatcherBean()
        val result = directoryWatcherBean.validate(path.toFile())
        assertThat(result).isTrue()
    }
}