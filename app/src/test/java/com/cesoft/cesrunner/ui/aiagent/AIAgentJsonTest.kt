package com.cesoft.cesrunner.ui.aiagent

import com.cesoft.cesrunner.ui.aiagent.AIAgentViewModel.Companion.jsonToRunEntity
import com.cesoft.cesrunner.ui.aiagent.ai.RunEntity
import com.cesoft.cesrunner.ui.aiagent.ai.RunsToolSet.Companion.toJson
import org.junit.Assert.assertEquals
import org.junit.Test

class AIAgentJsonTest {
    private val run = RunEntity(
        id = 1,
        name = "Morning",
        timeIni = "01/01/2025 08:00",
        timeEnd = "01/01/2025 09:00",
        distance = 10000,
        time = "1h 60m",
        vo2Max = 45.5,
        location = RunEntity.Location(40.4, -3.7),
    )

    @Test
    fun `tool json is read back as the same runs`() {
        assertEquals(listOf(run, run.copy(id = 2)), jsonToRunEntity(listOf(run, run.copy(id = 2)).toJson()))
    }

    @Test
    fun `a single run object is read as a list of one`() {
        assertEquals(listOf(run), jsonToRunEntity(run.toJson().removePrefix("[").removeSuffix("]")))
    }

    @Test
    fun `unknown fields added by the LLM are ignored`() {
        val json = run.toJson().replace("\"id\":1", "\"id\":1,\"comment\":\"fastest\"")
        assertEquals(listOf(run), jsonToRunEntity(json))
    }

    @Test
    fun `a run without location is rejected instead of holding a null location`() {
        val json = """[{"id":1,"name":"Morning","timeIni":"a","timeEnd":"b","distance":1,"time":"1m","vo2Max":1.0}]"""
        assertEquals(listOf<RunEntity>(), jsonToRunEntity(json))
    }

    @Test
    fun `invalid json returns no runs`() {
        assertEquals(listOf<RunEntity>(), jsonToRunEntity("not json"))
    }
}
