package com.cesoft.cesrunner.ui.aiagent.ai

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.core.tools.reflect.tools
import ai.koog.agents.features.eventHandler.feature.EventHandler
import ai.koog.prompt.executor.clients.deepseek.DeepSeekClientSettings
import ai.koog.prompt.executor.clients.deepseek.DeepSeekLLMClient
import ai.koog.prompt.executor.clients.deepseek.DeepSeekModels
import ai.koog.prompt.executor.clients.google.GoogleModels
import ai.koog.prompt.executor.clients.openai.OpenAIModels
import ai.koog.prompt.executor.clients.openrouter.OpenRouterModels
import ai.koog.prompt.executor.llms.MultiLLMPromptExecutor
import ai.koog.prompt.executor.llms.all.simpleGoogleAIExecutor
import ai.koog.prompt.executor.llms.all.simpleOllamaAIExecutor
import ai.koog.prompt.executor.llms.all.simpleOpenAIExecutor
import ai.koog.prompt.executor.llms.all.simpleOpenRouterExecutor
import ai.koog.prompt.llm.OllamaModels
import com.cesoft.cesrunner.BuildConfig
import com.cesoft.cesrunner.domain.usecase.GetLocationUC
import com.cesoft.cesrunner.domain.usecase.ai.FilterTracksUC
import com.cesoft.cesrunner.domain.usecase.ai.GetNearTracksUC

class RunsAgent {
    enum class Model { GEMINI, OPENAI, OPEN_ROUTER, OLLAMA, DEEPSEEK }

    private val _agent: AIAgent<String, String>

    private val systemPrompt: String =
        " You are a helpful assistant that answers questions about the user runs. " +
        " You can access the runs with some tools you can call." +
        //" A tool called getCurrentLocation let you get the user current location." +
        " A tool called searchByLocationNear let you get the runs near the current location." +
        " One tool lets you list all the runs with their fields, so you can filter by the fields. " +
        " Each tool returns a json with the values requested." +
        " Each run has the fields: id (an identifier), name (the run name or title)," +
            " timeIni (start time as date), timeEnd (end time as date), distance (in meters)," +
            " time (duration in hours and minutes), vo2Max (a value related to fitness), and " +
            " location (and object containing the latitude and longitude of the run)." +
        " After your text response, show the runs as json selected as the answer." +
        " If the user ask for a run that is near here, you must first call the tool to get the" +
            " current location and then the tool to get the runs"

    constructor(
        model: Model,
        filterTracks: FilterTracksUC,
        getLocation: GetLocationUC,
        getNearTracks: GetNearTracksUC,
        onAgentCompleted: (String) -> Unit,
        onAgentExecutionFailed: (Throwable) -> Unit,
    ) {
        val toolRegistry = ToolRegistry {
            tools(RunsToolSet(filterTracks, getLocation, getNearTracks))
        }
        val eventHandler = RunsEventHandler.getEventHandlerConfig(
            onAgentCompleted, onAgentExecutionFailed)

        val strategy = strategyStr

        _agent = when (model) {
            Model.GEMINI -> {
                AIAgent<String, String>(
                    promptExecutor = simpleGoogleAIExecutor(BuildConfig.GEMINI_KEY),
                    systemPrompt = systemPrompt,
                    llmModel = GoogleModels.Gemini2_0FlashLite,
                    installFeatures = { install(EventHandler, eventHandler) },
                    toolRegistry = toolRegistry,
                    strategy = strategy,
                    temperature = 0.9,
                    //maxIterations = 5,
                )
            }
            //NOTE: DEEPSEEK = 402 Payment Required
            Model.DEEPSEEK -> {
                AIAgent(
                    promptExecutor = MultiLLMPromptExecutor(
                        DeepSeekLLMClient(
                            apiKey = BuildConfig.DEEPSEEK_KEY,
                            settings = DeepSeekClientSettings()
                        )
                    ),
                    systemPrompt = systemPrompt,
                    llmModel = DeepSeekModels.DeepSeekReasoner,
                    installFeatures = { install(EventHandler, eventHandler) },
                    toolRegistry = toolRegistry,
                    strategy = strategy,
                    temperature = 0.9,
                    //maxIterations = 5,
                )
            }
            Model.OPENAI -> {
                AIAgent(
                    promptExecutor = simpleOpenAIExecutor(BuildConfig.OPENAI_KEY),
                    systemPrompt = systemPrompt,
                    llmModel = OpenAIModels.Chat.GPT5Nano,
                    installFeatures = { install(EventHandler, eventHandler) },
                    toolRegistry = toolRegistry,
                    strategy = strategy,
                    temperature = 0.9,
                    //maxIterations = 5,
                )
            }
            Model.OPEN_ROUTER -> {
                AIAgent(
                    promptExecutor = simpleOpenRouterExecutor(BuildConfig.OPENROUTER_KEY),
                    systemPrompt = systemPrompt,
                    llmModel = OpenRouterModels.Claude3Haiku, //.Gemini2_5FlashLite,
                    installFeatures = { install(EventHandler, eventHandler) },
                    toolRegistry = toolRegistry,
                    strategy = strategy,
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
                    strategy = strategy,
                    temperature = 0.9,
                    //maxIterations = 5,
                )
            }
        }
    }

    suspend fun run(prompt: String) { _agent.run(prompt) }

}