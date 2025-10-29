package com.cesoft.cesrunner.ui.aiagent.ai

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.core.tools.annotations.Tool
import ai.koog.agents.core.tools.reflect.ToolSet
import com.cesoft.cesrunner.domain.AppError
import com.cesoft.cesrunner.domain.entity.TrackDto
import com.cesoft.cesrunner.domain.entity.TrackUiDto
import com.cesoft.cesrunner.domain.usecase.ai.FilterTracksUC
import com.cesoft.cesrunner.toDateStr
import com.cesoft.cesrunner.toTimeStr
import com.google.gson.Gson

@LLMDescription("Tools for getting run information")
class RunsToolSet(
    private val filterTracks: FilterTracksUC,
): ToolSet {
    //TODO: Search by lat/lng ?'

    //----------------------------- FILTER RUNS -------------------------------------------
    // SERACHING FOR WITH OPTIONAL PARAMETERS DOESN'T WORK
    /*@Tool
    @LLMDescription("Finds a list of runs in the database that meet one or more of the parameters of the search")
    suspend fun searchForRuns(
//            @LLMDescription("Date and time when the run started")
//            dateIni: String?,
//            @LLMDescription("Date and time when the run finish")
//            dateEnd: String?,
//            @LLMDescription("Duration of the run")
//            duration: String?,
        @LLMDescription("The name of the run, how the run is called")
        name: String?,
        @LLMDescription("Total distance of the run in meters")
        distance: Int?,
    ): String {
        val res: Result<List<TrackDto>> = filterTracks(name, distance)
        return if (res.isSuccess) {
            val tracks = res.getOrNull()
            if (tracks.isNullOrEmpty()) {
                NO_RUN
            } else {
                data(TrackUiDto.toUi(tracks.first()))
            }
        } else {
            DB_ERR + res.exceptionOrNull()?.message
        }
    }*/

    //----------------------------- RUN NAME -------------------------------------------
    @Tool
    @LLMDescription("Finds a run in the database which name is like the parameter")
    suspend fun searchForRuns(
        @LLMDescription("The name of the run, how the run is called")
        name: String?,
    ): String {
        val res: Result<List<TrackDto>> = filterTracks(name)
        return if (res.isSuccess) {
            val tracks = res.getOrNull()
            if (tracks.isNullOrEmpty()) {
                NO_RUN
            } else {
                data(TrackUiDto.toUi(tracks.first()))
            }
        } else {
            DB_ERR + res.exceptionOrNull()?.message
        }
    }

    //----------------------------- LONGEST/SHORTEST RUN -------------------------------------------
    @Tool
    @LLMDescription("Get the longest or the shortest run in the database")//, get the run with maximum distance in the database
    suspend fun getLongestRun(
        @LLMDescription("When true, get the longest run, when false, get the shortest run")
        theLongest: Boolean = true,
    //): Result<List<TrackDto>> {
    ): String {
        // This return tipe depends on the definition of agetn: val _agent: AIAgent<String, String> or AIAgent<String, Result<List<TrackDto>>>
        val res: Result<List<TrackDto>> = filterTracks()
        return if (res.isSuccess) {
            val tracks = res.getOrNull()
            if(tracks.isNullOrEmpty()) {
                "There is no run stored"
                //Result.failure(Exception("There is no run stored"))
            }
            else {
                var result = tracks.first()
                if(theLongest) {
                    for (t in tracks) if (t.distance > result.distance) result = t
                }
                else {
                    for (t in tracks) if (t.distance < result.distance) result = t
                }
                "The run is ${result.distance} meters long," +
                        " with id ${result.id}," +
                        " with name ${result.name}," +
                        " started at ${result.timeIni.toDateStr()}," +
                        " finished at ${result.timeEnd.toDateStr()}," +
                        " with a duration of ${result.time.toTimeStr()}"
                //Result.success(tracks)
                data(TrackUiDto.toUi(result))
            }
        } else {
            DB_ERR + res.exceptionOrNull()?.message
            //Result.failure(res.exceptionOrNull() ?: AppError.NotFound)
        }
    }

    companion object {
        const val NO_RUN = "There is no run stored"
        const val DB_ERR = "There was an error while searching the database. The error was "
        private fun data(track: TrackUiDto): String {
            val gson = Gson()
            return gson.toJson(track)
        }
    }
}
