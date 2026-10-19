package com.cesoft.cesrunner.data.groq

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GroqParseRunsTest {

    @Test
    fun `parses the runs inside cesjson`() {
        val msg = """La carrera más larga es esta: {"cesjson": [{"id": 8, "name": "02/07/2025", "distance": 5627}]}"""

        val runs = Groq.parseRuns(msg)

        assertEquals(1, runs.size)
        assertEquals(8L, runs[0].id)
        assertEquals("02/07/2025", runs[0].name)
        assertEquals(5627, runs[0].distance)
    }

    @Test
    fun `parses several runs`() {
        val msg = """{"cesjson":[{"id":1,"distance":100},{"id":2,"distance":200}]}"""

        val runs = Groq.parseRuns(msg)

        assertEquals(listOf(1L, 2L), runs.map { it.id })
    }

    @Test
    fun `fields not returned by the llm keep their default value`() {
        val runs = Groq.parseRuns("""{"cesjson":[{"id":1}]}""")
        assertEquals(-1, runs[0].distanceToLocation)
    }

    @Test
    fun `returns empty list when there is no cesjson`() {
        assertTrue(Groq.parseRuns("No hay carreras").isEmpty())
        assertTrue(Groq.parseRuns("").isEmpty())
    }

    @Test
    fun `returns empty list when the list is not closed`() {
        assertTrue(Groq.parseRuns("""{"cesjson":[{"id":1}""").isEmpty())
    }

    @Test
    fun `returns empty list when the json is malformed`() {
        assertTrue(Groq.parseRuns("""{"cesjson": {"id": ]""").isEmpty())
    }
}
