package com.n2.directorywatcher

import com.n2.directorywatcher.WEvent.Kind
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class WEventTest {
    @Test
    fun `create a initialized WEvent without error`() {
        val wEvent = WEvent(1, "abc.txt", 1, Kind.Initialized)
        val (agentID, fileName, timeStamp, kind, content) = wEvent
        assertThat(agentID).isOne()
        assertThat(fileName).isEqualTo("abc.txt")
        assertThat(timeStamp).isOne()
        assertThat(kind).isEqualTo(Kind.Initialized)
        assertThat(content).isEqualTo(ByteArray(0))
    }

    @Test
    fun `create a file created WEvent without error`() {
        val wEvent = WEvent(1, "abc.txt", 1, Kind.Created, "Euroclear".toByteArray())
        val (agentID, fileName, timeStamp, kind, content) = wEvent
        assertThat(agentID).isOne()
        assertThat(fileName).isEqualTo("abc.txt")
        assertThat(timeStamp).isOne()
        assertThat(kind).isEqualTo(Kind.Created)
        assertThat(content).isEqualTo("Euroclear".toByteArray())
    }
}