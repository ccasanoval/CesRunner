package com.cesoft.cesrunner.ui.aiagent.ai

import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.core.tools.annotations.Tool
import ai.koog.agents.core.tools.reflect.ToolSet
import com.cesoft.cesrunner.domain.entity.TrackDto
import com.cesoft.cesrunner.domain.usecase.ai.FilterTracksUC
import com.google.gson.Gson

//TODO: Search by lat/lng ?'
//TODO: Agent MCP ?

@LLMDescription("Tools for finding runs")
class RunsToolSet(
    private val filterTracks: FilterTracksUC,
): ToolSet {

    //----------------------------- RUN NAME -------------------------------------------
    @Tool
    @LLMDescription("Finds a run in the database which name is like the parameter")
    suspend fun searchByName(
        @LLMDescription("The name of the run, how the run is called")
        name: String?,
    ): String {
        val res: Result<List<TrackDto>> = filterTracks(name)
        return if (res.isSuccess) {
            val tracks = res.getOrNull()?.map { RunEntity.toUi(it) }
            if (tracks.isNullOrEmpty()) {
                NO_RUN
            } else {
                tracksToString(tracks)
            }
        } else {
            DB_ERR + res.exceptionOrNull()?.message
        }
    }

    //----------------------------- LONGEST/SHORTEST RUN -------------------------------------------
    @Tool
    @LLMDescription("Get the longest or the shortest run in the database")//, get the run with maximum distance in the database
    suspend fun getLongestOrShorterRun(
        @LLMDescription("When true, get the longest run, when false, get the shortest run")
        theLongest: Boolean = true,
    ): String {
        val res: Result<List<TrackDto>> = filterTracks()
        return if (res.isSuccess) {
            val tracks = res.getOrNull()
            if(tracks.isNullOrEmpty()) {
                NO_RUN
            }
            else {
                var result = tracks.first()
                if(theLongest) {
                    for (t in tracks) if (t.distance > result.distance) result = t
                }
                else {
                    for (t in tracks) if (t.distance < result.distance) result = t
                }
                tracksToString(listOf(RunEntity.toUi(result)))
            }
        } else {
            DB_ERR + res.exceptionOrNull()?.message
        }
    }

    //----------------------------- HIGHEST/LOWER VO2MAX RUN -------------------------------------------
    @Tool
    @LLMDescription("Get the highest or the lower vo2max run in the database")
    suspend fun getBiggerLowestVo2MaxRun(
        @LLMDescription("When true, get the biggest vo2max run, when false, get the smaller vo2max run")
        theBiggest: Boolean = true,
    ): String {
        val res: Result<List<TrackDto>> = filterTracks()
        return if (res.isSuccess) {
            val tracks = res.getOrNull()?.map { RunEntity.toUi(it) }
            if(tracks.isNullOrEmpty()) {
                NO_RUN
            }
            else {
                var result = tracks.first()
                if(theBiggest) {
                    for (t in tracks) if (t.vo2Max > result.vo2Max) result = t
                }
                else {
                    for (t in tracks) if (t.vo2Max < result.vo2Max) result = t
                }
                tracksToString(listOf(result))
            }
        } else {
            DB_ERR + res.exceptionOrNull()?.message
        }
    }

    @Tool
    @LLMDescription("Get all the runs in the database")
    suspend fun getRuns(): String {
    //suspend fun getRuns(): Result<List<RunEntity>> {
        val res: Result<List<TrackDto>> = filterTracks()//TODO: getAll()
        return if (res.isSuccess) {
            val tracks = res.getOrNull()
            if(tracks.isNullOrEmpty()) {
                NO_RUN
                //Result.failure(Throwable(NO_RUN))
            }
            else {
                tracksToString(tracks.map { RunEntity.toUi(it) })
                //Result.success(tracks.map { RunEntity.toUi(it) })
            }
        } else {
            DB_ERR + res.exceptionOrNull()?.message
            //Result.failure(Throwable(DB_ERR + res.exceptionOrNull()?.message))
        }
    }

    companion object {
        const val NO_RUN = "There is no run stored"
        const val DB_ERR = "There was an error while searching the database. The error was "
        /*private fun trackToString(track: RunEntity): String {
            val gson = Gson()
            return gson.toJson(track)
        }*/
        private fun tracksToString(tracks: List<RunEntity>): String {
            val gson = Gson()
            return gson.toJson(tracks)
        }
    }
}
