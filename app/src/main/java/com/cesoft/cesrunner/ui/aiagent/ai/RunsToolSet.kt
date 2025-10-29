package com.cesoft.cesrunner.ui.aiagent.ai

import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.core.tools.annotations.Tool
import ai.koog.agents.core.tools.reflect.ToolSet
import com.cesoft.cesrunner.domain.entity.TrackDto
import com.cesoft.cesrunner.domain.usecase.ai.FilterTracksUC
import com.cesoft.cesrunner.toDateStr
import com.cesoft.cesrunner.toTimeStr

////////////////////////////////////////////////////////////////////////////////////////////////
// TOOLS
////////////////////////////////////////////////////////////////////////////////////////////////
@LLMDescription("Tools for getting run information")
class RunsToolSet(
    private val filterTracks: FilterTracksUC,
): ToolSet {
    //TODO: Search by lat/lng ?'
    ////////////////// FILTER RUN
    /*        @Tool
            @LLMDescription("Finds a list of runs in the database that meet the parameters of the search")
            suspend fun searchForRuns(
    //            @LLMDescription("Date and time when the run started")
    //            dateIni: String?,
    //            @LLMDescription("Date and time when the run finish")
    //            dateEnd: String?,
    //            @LLMDescription("Duration of the run")
    //            duration: String?,
                @LLMDescription("Name of the run")
                name: String?,
                @LLMDescription("Total distance of the run")
                distance: Int?,
            ): Result<TrackDto> {
                val res: Result<List<TrackDto>> = filterTracks(name, distance)
                return if (res.isSuccess) {
                    val tracks = res.getOrNull()
                    if (tracks.isNullOrEmpty()) {
                        Result.failure(Exception("There is no run stored"))
                    } else {
                        Result.success(tracks.first())
                    }
                } else {
                    val msg = "There was an error while searching the database." +
                            " The error was " + res.exceptionOrNull()?.message
                    Result.failure(Exception(msg))
                }
            }*/
    ////////////////// LONGEST/SHORTEST RUN
    @Tool
    @LLMDescription("Get the longest or the shortest run in the database")//, get the run with maximum distance in the database
    suspend fun getLongestRun(
        @LLMDescription("When true, get the longest run, when false, get the shortest run")
        theLongest: Boolean = true,
        //TODO: Try returning Result<TrackDto>
    ): String {
        val res: Result<List<TrackDto>> = filterTracks()
        return if (res.isSuccess) {
            val tracks = res.getOrNull()
            if(tracks.isNullOrEmpty()) {
                "There is no run stored"
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
            }
        } else {
            "There was an error while searching the database." +
                    " The error was " + res.exceptionOrNull()?.message
        }
    }
}
