package com.cesoft.cesrunner.ui.aiagent.ai

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.core.tools.reflect.tools
import ai.koog.agents.features.eventHandler.feature.EventHandler
import ai.koog.prompt.executor.clients.openrouter.OpenRouterModels
import ai.koog.prompt.executor.llms.all.simpleOpenRouterExecutor
import com.cesoft.cesrunner.BuildConfig
import com.cesoft.cesrunner.domain.usecase.ai.FilterTracksUC

class RunsAgent {
    enum class Model { /*OPENAI, GEMINI,*/ OPEN_ROUTER }

    private val _agent: AIAgent<String, String>
    //private val _agent: AIAgent<String, Result<List<TrackDto>>>//TODO: Try output

    constructor(
        model: Model,
        filterTracks: FilterTracksUC,
        onAgentCompleted: (String) -> Unit,
        onAgentExecutionFailed: (Throwable) -> Unit,
    ) {
        val systemPrompt =
            " You are a helpful assistant that answers questions about the runs stored in the tools." +
            " Each run has the fields: id, name, distance, start time, end time, duration and vo2max." +
            " When answering a question about runs, return all those values of the run to the user." +
            " The fields like distance with decimal dots, must be expressed with the local internationalization format, in spanish it will have a comma instead." +
            " Format the distance field in km when distance is greater than 1000 meters." +
            " If the duration is less than an hour, don't show the value for hour, you can hide the 0h value."
             ""
        val toolRegistry = ToolRegistry {
            tools(RunsToolSet(filterTracks))
        }
        val eventHandler = RunsEventHandler.getEventHandlerConfig(
            onAgentCompleted, onAgentExecutionFailed)
        _agent = when (model) {
            /*Model.GEMINI -> {
                AIAgent(
                    promptExecutor = simpleGoogleAIExecutor(BuildConfig.GEMINI_KEY),
                    systemPrompt = systemPrompt,
                    llmModel = GoogleModels.Gemini2_5Pro,
                    installFeatures = { install(EventHandler, eventHandler) },
                    toolRegistry = toolRegistry
                )
            }
            Model.OPENAI -> {
                AIAgent(
                    promptExecutor = simpleOpenAIExecutor(BuildConfig.OPENAI_KEY),
                    systemPrompt = systemPrompt,
                    llmModel = OpenAIModels.Chat.GPT4o,
                    installFeatures = { install(EventHandler, eventHandler) },
                    temperature = 0.9,
                    maxIterations = 5,
                    toolRegistry = toolRegistry
                )
            }*/
            Model.OPEN_ROUTER -> {
                AIAgent(
                    promptExecutor = simpleOpenRouterExecutor(BuildConfig.OPENROUTER_KEY),
                    systemPrompt = systemPrompt,
                    llmModel = OpenRouterModels.Gemini2_5Pro,
                    installFeatures = { install(EventHandler, eventHandler) },
                    toolRegistry = toolRegistry,
                    temperature = 0.9,
                )
            }
        }
    }

    suspend fun run(prompt: String) {
        _agent.run(prompt)
    }
}