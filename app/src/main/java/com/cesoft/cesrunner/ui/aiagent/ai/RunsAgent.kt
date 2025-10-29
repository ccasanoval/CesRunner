package com.cesoft.cesrunner.ui.aiagent.ai

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.core.tools.reflect.tools
import ai.koog.agents.features.eventHandler.feature.EventHandler
import ai.koog.agents.features.eventHandler.feature.EventHandlerConfig
import ai.koog.prompt.executor.clients.google.GoogleModels
import ai.koog.prompt.executor.clients.openai.OpenAIModels
import ai.koog.prompt.executor.clients.openrouter.OpenRouterModels
import ai.koog.prompt.executor.llms.all.simpleGoogleAIExecutor
import ai.koog.prompt.executor.llms.all.simpleOpenAIExecutor
import ai.koog.prompt.executor.llms.all.simpleOpenRouterExecutor
import com.cesoft.cesrunner.BuildConfig
import com.cesoft.cesrunner.domain.usecase.ai.FilterTracksUC

class RunsAgent {
    enum class Model { OPENAI, GEMINI, OPEN_ROUTER }

    private val _agent: AIAgent<String, String>//TODO: Try output = Result<List<TrackDto>>
    constructor(
        model: Model,
        filterTracks: FilterTracksUC,
        onAgentCompleted: (String) -> Unit,
        onAgentExecutionFailed: (Throwable) -> Unit,
    ) {
        val systemPrompt = "You are a helpful assistant that answers questions about the runs you have stored in your tools." +
                "Each run has been stored by the user, after a geolocation tool has recorded some data as he or she was running in some route." +
                "Each run has some fields, like distance, start time, end time, duration, id, and name." +
                "When answering a question, return all those values of the run to the user." +
                "The fields like distance with decimal dots, must be expressed with the local internationalization format, in spanish it will have a comma instead." +
                "If the duration is less than an hour, don't show the value for hour, you can hide the 0h value."
                "Format the distance field in km when distance is greater than 1000 meters." +
                ""
        val toolRegistry = ToolRegistry {
            tools(RunsToolSet(filterTracks))
        }
        val eventHandler = RunsEventHandler.getEventHandlerConfig(
            onAgentCompleted, onAgentExecutionFailed)
        _agent = when (model) {
            Model.GEMINI -> {
                val apiKey = BuildConfig.GEMINI_KEY
                AIAgent(
                    promptExecutor = simpleGoogleAIExecutor(apiKey),
                    systemPrompt = systemPrompt,
                    llmModel = GoogleModels.Gemini2_5Pro,
                    installFeatures = { install(EventHandler, eventHandler) },
                    toolRegistry = toolRegistry
                )
            }
            Model.OPENAI -> {
                val apiKey = BuildConfig.OPENAI_KEY
                AIAgent(
                    promptExecutor = simpleOpenAIExecutor(apiKey),
                    systemPrompt = systemPrompt,
                    llmModel = OpenAIModels.Chat.GPT4o,
                    installFeatures = { install(EventHandler, eventHandler) },
                    temperature = 0.9,
                    maxIterations = 5,
                    toolRegistry = toolRegistry
                )
            }
            Model.OPEN_ROUTER -> {
                val apiKey = BuildConfig.OPENROUTER_KEY
                AIAgent(
                    promptExecutor = simpleOpenRouterExecutor(apiKey),
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

    companion object {

    }
}