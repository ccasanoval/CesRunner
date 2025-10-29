package com.cesoft.cesrunner.ui.aiagent

import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.core.tools.reflect.tools
import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.adidas.mvi.MviHost
import com.adidas.mvi.State
import com.adidas.mvi.reducer.Reducer
import com.cesoft.cesrunner.domain.usecase.ai.FilterTracksUC
import com.cesoft.cesrunner.ui.aiagent.ai.RunsAgent
import com.cesoft.cesrunner.ui.aiagent.ai.RunsEventHandler
import com.cesoft.cesrunner.ui.aiagent.ai.RunsToolSet
import com.cesoft.cesrunner.ui.aiagent.mvi.AIAgentIntent
import com.cesoft.cesrunner.ui.aiagent.mvi.AIAgentSideEffect
import com.cesoft.cesrunner.ui.aiagent.mvi.AIAgentState
import com.cesoft.cesrunner.ui.aiagent.mvi.AIAgentTransform
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class AIAgentViewModel(
    //private val readAllTracks: ReadAllTracksUC,
    private val filterTracks: FilterTracksUC,
    coroutineDispatcher: CoroutineDispatcher = Dispatchers.Default,
): ViewModel(), MviHost<AIAgentIntent, State<AIAgentState, AIAgentSideEffect>> {
    private val reducer = Reducer(
        coroutineScope = viewModelScope,
        defaultDispatcher = coroutineDispatcher,
        initialInnerState = AIAgentState.Init(),
        logger = null,
        intentExecutor = this::executeIntent
    )
    override val state = reducer.state
    override fun execute(intent: AIAgentIntent) {
        reducer.executeIntent(intent)
    }
    private fun executeIntent(intent: AIAgentIntent) =
        when(intent) {
            AIAgentIntent.Close -> executeClose()
            is AIAgentIntent.ExecPrompt -> executePrompt(intent.prompt)
        }

    fun consumeSideEffect(
        sideEffect: AIAgentSideEffect,
        navController: NavController,
        context: Context,
    ) {
        when(sideEffect) {
            AIAgentSideEffect.Close -> { (context as Activity).finish() }
        }
    }

    private fun executeClose() = flow {
        emit(AIAgentTransform.AddSideEffect(AIAgentSideEffect.Close))
    }

    //TODO: use local model ?
    //https://vivekparasharr.medium.com/how-i-ran-a-local-llm-on-my-android-phone-and-what-i-learned-about-googles-ai-edge-gallery-807572211562
    //TODO: flujos
    //TODO: CM...
    //TODO:
    enum class Model { OPENAI, GEMINI, OPENROUTER }
    private fun executePrompt(prompt: String) = flow {
        emit(AIAgentTransform.GoLoading)

        val callbackResult: AIAgentTransform.GoInit = suspendCoroutine { cont ->
            val eventHandlerConfig = RunsEventHandler.getEventHandlerConfig(
                onAgentCompleted = { response -> cont.resume(AIAgentTransform.GoInit(prompt = prompt, response = response))},
                onAgentExecutionFailed = { error -> cont.resume(AIAgentTransform.GoInit(prompt = prompt, response = "", error = error)) }
            )

            val agent = RunsAgent(
                model = RunsAgent.Model.OPEN_ROUTER,
                filterTracks = filterTracks,
                eventHandlerConfig = eventHandlerConfig
            )
            viewModelScope.launch {
                try {
                    agent.run(prompt)
                }
                catch (e: Throwable) {
                    Log.e("AIAgentVM", "viewModelScope.launch:e:------------------ $e")
                }
            }
        }
        Log.e("AIAgentVM", "EMIT-------------------- $callbackResult")
        emit(callbackResult)
    }
/*
    ////////////////////////////////////////////////////////////////////////////////////////////////
    // TOOLS
    ////////////////////////////////////////////////////////////////////////////////////////////////
    @LLMDescription("Tools for getting run information")
    class RunsToolSet(
        //private val readAllTracks: ReadAllTracksUC,
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
    }*/

 }
