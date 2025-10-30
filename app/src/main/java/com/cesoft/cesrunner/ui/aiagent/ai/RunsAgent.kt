package com.cesoft.cesrunner.ui.aiagent.ai

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.core.tools.reflect.tools
import ai.koog.agents.features.eventHandler.feature.EventHandler
import ai.koog.prompt.executor.clients.google.GoogleModels
import ai.koog.prompt.executor.clients.openai.OpenAIModels
import ai.koog.prompt.executor.clients.openrouter.OpenRouterModels
import ai.koog.prompt.executor.llms.all.simpleGoogleAIExecutor
import ai.koog.prompt.executor.llms.all.simpleOllamaAIExecutor
import ai.koog.prompt.executor.llms.all.simpleOpenAIExecutor
import ai.koog.prompt.executor.llms.all.simpleOpenRouterExecutor
import ai.koog.prompt.llm.OllamaModels
import com.cesoft.cesrunner.BuildConfig
import com.cesoft.cesrunner.domain.usecase.ai.FilterTracksUC

class RunsAgent {
    enum class Model { OPENAI, GEMINI, OPEN_ROUTER, OLLAMA }

    private val _agent: AIAgent<String, String>
    //private val _agent: AIAgent<String, Result<List<TrackDto>>>//TODO: Try a different output

    constructor(
        model: Model,
        filterTracks: FilterTracksUC,
        onAgentCompleted: (String) -> Unit,
        onAgentExecutionFailed: (Throwable) -> Unit,
    ) {
        val systemPrompt: String =
            " You are a helpful assistant that answers questions about the runs accessed by the tools." +
            " Each run has the fields: id (identifier), name, timeIni (start time), timeEnd (end time), distance, time (duration of the run) and vo2Max." +
            " You must return all the fields of the run." +
            " The fields with decimal dot, must be expressed in the same language of the question, in spanish it will have a comma, but in english it's a dot." +
            " Format the distance field in km when distance is greater than 1000 meters." +
            " If the duration is less than an hour, do not show the value for hour, ie you can remove the 0h part." +
            ""
        val toolRegistry = ToolRegistry {
            tools(RunsToolSet(filterTracks))
        }
        val eventHandler = RunsEventHandler.getEventHandlerConfig(
            onAgentCompleted, onAgentExecutionFailed)
        _agent = when (model) {
            Model.GEMINI -> {
                AIAgent(
                    promptExecutor = simpleGoogleAIExecutor(BuildConfig.GEMINI_KEY),
                    systemPrompt = systemPrompt,
                    llmModel = GoogleModels.Gemini2_0FlashLite,
                    installFeatures = { install(EventHandler, eventHandler) },
                    toolRegistry = toolRegistry,
                    temperature = 0.9,
                    //maxIterations = 5,
                )
            }
            Model.OPENAI -> {
                AIAgent(
                    promptExecutor = simpleOpenAIExecutor(BuildConfig.OPENAI_KEY),
                    systemPrompt = systemPrompt,
                    llmModel = OpenAIModels.Chat.GPT4_1,
                    installFeatures = { install(EventHandler, eventHandler) },
                    toolRegistry = toolRegistry,
                    temperature = 0.9,
                    //maxIterations = 5,
                )
            }
            Model.OPEN_ROUTER -> {
                AIAgent(
                    promptExecutor = simpleOpenRouterExecutor(BuildConfig.OPENROUTER_KEY),
                    systemPrompt = systemPrompt,
                    llmModel = OpenRouterModels.Gemini2_5FlashLite,
                    installFeatures = { install(EventHandler, eventHandler) },
                    toolRegistry = toolRegistry,
                    temperature = 0.9,
                    //maxIterations = 5,
                )
            }
            Model.OLLAMA -> {//TODO: How does it work?
                AIAgent(
                    promptExecutor = simpleOllamaAIExecutor("http://"),
                    systemPrompt = systemPrompt,
                    llmModel = OllamaModels.Groq.LLAMA_3_GROK_TOOL_USE_8B,
                    installFeatures = { install(EventHandler, eventHandler) },
                    toolRegistry = toolRegistry,
                    temperature = 0.9,
                    //maxIterations = 5,
                )
            }
        }
    }

    suspend fun run(prompt: String) {
        _agent.run(prompt)
    }
}