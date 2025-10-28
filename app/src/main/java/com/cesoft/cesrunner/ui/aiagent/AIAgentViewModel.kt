package com.cesoft.cesrunner.ui.aiagent

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.core.tools.annotations.Tool
import ai.koog.agents.core.tools.reflect.ToolSet
import ai.koog.agents.core.tools.reflect.tools
import ai.koog.agents.features.eventHandler.feature.EventHandler
import ai.koog.agents.features.eventHandler.feature.EventHandlerConfig
import ai.koog.prompt.executor.clients.google.GoogleModels
import ai.koog.prompt.executor.clients.openai.OpenAIModels
import ai.koog.prompt.executor.clients.openrouter.OpenRouterModels
import ai.koog.prompt.executor.llms.all.simpleGoogleAIExecutor
import ai.koog.prompt.executor.llms.all.simpleOpenAIExecutor
import ai.koog.prompt.executor.llms.all.simpleOpenRouterExecutor
import ai.koog.prompt.llm.LLMProvider
import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.adidas.mvi.MviHost
import com.adidas.mvi.State
import com.adidas.mvi.reducer.Reducer
import com.cesoft.cesrunner.BuildConfig
import com.cesoft.cesrunner.domain.entity.TrackDto
import com.cesoft.cesrunner.domain.usecase.ReadAllTracksUC
import com.cesoft.cesrunner.domain.usecase.ai.FilterTracksUC
import com.cesoft.cesrunner.toDateStr
import com.cesoft.cesrunner.toTimeStr
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

    enum class Model { OPENAI, GEMINI, OPENROUTER }
    private fun executePrompt(prompt: String) = flow {
        emit(AIAgentTransform.GoLoading)

        val callbackResult: AIAgentTransform.GoInit = suspendCoroutine { cont ->
            val eventHandlerConfig: EventHandlerConfig.() -> Unit = {
                // We assign the agent response to the result
                onAgentCompleted { res ->
                    android.util.Log.e("AIAgentVM", "onAgentCompleted------------- ${res.result} / ${res.eventType} / ${res.component1()} / ${res.component2()}")
                    val response = res.result.toString()
                    //emit(AIAgentTransform.GoInit(prompt = prompt, response = response))
                    cont.resume(AIAgentTransform.GoInit(prompt = prompt, response = response))
                }
                onAgentExecutionFailed { fail ->
                    android.util.Log.e("AIAgentVM", "onAgentExecutionFailed------------- ${fail.throwable} / ${fail.eventType} / ${fail.component1()} / ${fail.component2()}")
                    val error = fail.throwable
                    //emit(AIAgentTransform.GoInit(prompt = prompt, response = "", error = error))
                    cont.resume(AIAgentTransform.GoInit(prompt = prompt, response = "", error = error))
                }
                onToolCallCompleted { res ->
                    android.util.Log.e("AIAgentVM", "onToolCallCompleted------------- $res ")
                }
                onToolCallFailed { res ->
                    android.util.Log.e("AIAgentVM", "onToolCallFailed------------- $res ")
                }
                /*onAgentClosing { res ->
                    android.util.Log.e("AIAgentVM", "onAgentClosing------------- ${res.eventType} / ${res.component1()}")
                }
                onAgentStarting { res ->
                    android.util.Log.e("AIAgentVM","onAgentStarting------------- ${res.eventType} / ${res.component1()} / ${res.context.agentInput} / ${res.agent.agentConfig.prompt} / ${res.agent.getState()}")
                }
                onLLMCallCompleted { res ->
                    android.util.Log.e("AIAgentVM", "onLLMCallCompleted------------- ${res.eventType} / ${res.component1()} / ${res.prompt}")
                    for (r in res.responses) android.util.Log.e("AIAgentVM", "onLLMCallCompleted:responses--------- ${r.content} / ${r.role} / ${r.metaInfo.metadata}")
                }*/
            }

            val toolRegistry = ToolRegistry {
                tools(RunsToolSet(filterTracks))
            }
            //TODO: use local model ?
            //https://vivekparasharr.medium.com/how-i-ran-a-local-llm-on-my-android-phone-and-what-i-learned-about-googles-ai-edge-gallery-807572211562
            //TODO: flujos
            //TODO:

            val model = Model.OPENROUTER

            val agent = when (model) {
                Model.GEMINI -> {
                    val apiKey = BuildConfig.GEMINI_KEY
                    AIAgent(
                        promptExecutor = simpleGoogleAIExecutor(apiKey),
                        systemPrompt = "You are a helpful assistant that answers questions about the runs you have stored in your tools." +
                                "Each run has been stored by the user, after a geolocation tool has recorded some data as he or she was running in some route." +
                                "Each run has some fields, like distance, start time, end time, duration, id, and name" +
                                "Format the distance field in km when distance is greater than 1000 meters" +
                                "",
                        llmModel = GoogleModels.Gemini2_5Pro,
                        installFeatures = { install(EventHandler, eventHandlerConfig) },
                        toolRegistry = toolRegistry
                    )
                }
                Model.OPENAI -> {
                    val apiKey = BuildConfig.OPENAI_KEY
                    AIAgent(
                        promptExecutor = simpleOpenAIExecutor(apiKey),
                        systemPrompt = "You are a helpful assistant. Answer user questions concisely.",
                        llmModel = OpenAIModels.Chat.GPT4o,
                        installFeatures = { install(EventHandler, eventHandlerConfig) },
                        temperature = 0.7,
                        maxIterations = 5,
                        toolRegistry = toolRegistry
                    )
                }
                //TODO: AIAgentError(message=Cannot read Json element because of unexpected end of the input at path: $
                Model.OPENROUTER -> {
                    val apiKey = BuildConfig.OPENROUTER_KEY
                    AIAgent(
                        promptExecutor = simpleOpenRouterExecutor(apiKey),
                        systemPrompt = "You are a helpful assistant that answers questions about the runs you have stored in your tools." +
                                "Each run has been stored by the user, after a geolocation tool has recorded some data as he or she was running in some route." +
                                "Each run has some fields, like distance, start time, end time, duration, id, and name" +
                                "Format the distance field in km when distance is greater than 1000 meters" +
                                "",
                        llmModel = OpenRouterModels.Gemini2_5Pro,
                        installFeatures = { install(EventHandler, eventHandlerConfig) },
                        toolRegistry = toolRegistry
                    )
                }
            }
            viewModelScope.launch {
                try {
                    agent.run(prompt)
                }
                catch (e: Throwable) {
                    android.util.Log.e("AIAgentVM", "viewModelScope.launch:e:-------------------- $e")
                    //val callbackResult = AIAgentTransform.GoInit(prompt = prompt, response = "", error = AppError.fromThrowable(e))
                }
            }
            //CoroutineScope(Dispatchers.Default).launch {}
        }
        android.util.Log.e("AIAgentVM", "EMIT-------------------- $callbackResult")
        emit(callbackResult)
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // TOOLS
    ////////////////////////////////////////////////////////////////////////////////////////////////
    @LLMDescription("Tools for getting run information")
    class RunsToolSet(
        //private val readAllTracks: ReadAllTracksUC,
        private val filterTracks: FilterTracksUC,
    ): ToolSet {
        //TODO: Serach by lt/lng ?'
        ////////////////// FILTER RUN
        /*@Tool
        @LLMDescription("Find a run in the database that meet the parameters of the search")
        suspend fun findRun(
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
        ): TrackDto {
            val res = filterTracks(name, distance)
            return TrackDto.Empty
        }*/
        ////////////////// LONGEST RUN
        @Tool
        @LLMDescription("Get the longest run in the database, get the run with maximum distance in the database")
        suspend fun getLongestRun(
            //@LLMDescription("The city and state/country")
            //location: String
            //TODO: return trackDto?
        ): String {
            val res: Result<List<TrackDto>> = filterTracks()//readAllTracks()
            android.util.Log.e("AAAAAAA", "------------------ ${res.exceptionOrNull()?.message} / ${res.getOrNull()}")
            return if (res.isSuccess) {
                val tracks = res.getOrNull()
                if(tracks.isNullOrEmpty()) {
                    "There is no run stored"
                }
                else {
                    var longest = TrackDto.Empty
                    for (t in tracks) if (t.distance > longest.distance) longest = t
                    "The longest run is ${longest.distance} meters long," +
                            " with id ${longest.id}," +
                            " with name ${longest.name}," +
                            " started at ${longest.timeIni.toDateStr()}," +
                            " finished at ${longest.timeEnd.toDateStr()}," +
                            " with a duration of ${longest.time.toTimeStr()}"
                }
            } else {
                "There was an error while searching the database." +
                        " The error was " + res.exceptionOrNull()?.message
            }
        }
    }

 }
