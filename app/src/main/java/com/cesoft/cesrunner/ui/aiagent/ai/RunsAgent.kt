package com.cesoft.cesrunner.ui.aiagent.ai

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.core.tools.reflect.tools
import ai.koog.agents.features.eventHandler.feature.EventHandler
import ai.koog.agents.features.tracing.feature.Tracing
import ai.koog.agents.features.tracing.writer.TraceFeatureMessageLogWriter
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
import ai.koog.prompt.llm.LLMProvider
import ai.koog.prompt.llm.OllamaModels
import com.cesoft.cesrunner.BuildConfig
import com.cesoft.cesrunner.domain.usecase.GetLocationUC
import com.cesoft.cesrunner.domain.usecase.ai.FilterTracksUC
import com.cesoft.cesrunner.domain.usecase.ai.GetNearTracksUC
import com.cesoft.cesrunner.domain.usecase.ai.GetTrackLocationUC
import io.github.oshai.kotlinlogging.KotlinLogging

class RunsAgent {
    enum class Model { GEMINI, OPENAI, OPEN_ROUTER, OLLAMA, DEEPSEEK }

    private val _agent: AIAgent<String, String>

    private val systemPrompt: String =
        " You are a helpful assistant that answers questions about the runs accessed by the tools." +
                " Each run has the fields: id (identifier integer), name (as string)," +
                " timeIni (start time as date), timeEnd (end time as date), distance (in meters)," +
                " time (duration in hours and minutes), vo2Max, and " +
                " latLng (and object containing the latitude and longitude of the run location)." +
                " The tools you have return a json with a list of runs." +
                " You have to filter the runs to get the ones that fulfills the request." +
                " After your text response, show the runs as json selected as the answer."

    constructor(
        model: Model,
        filterTracks: FilterTracksUC,
        getLocation: GetLocationUC,
        getNearTracks: GetNearTracksUC,
        getTrackLocation: GetTrackLocationUC,
        onAgentCompleted: (String) -> Unit,
        onAgentExecutionFailed: (Throwable) -> Unit,
    ) {
        val toolRegistry = ToolRegistry {
            tools(RunsToolSet(filterTracks, getLocation, getNearTracks, getTrackLocation))
        }
        val eventHandler = RunsEventHandler.getEventHandlerConfig(
            onAgentCompleted, onAgentExecutionFailed)

        _agent = when (model) {
            Model.GEMINI -> {
                AIAgent<String, String>(
                    promptExecutor = simpleGoogleAIExecutor(BuildConfig.GEMINI_KEY),
                    systemPrompt = systemPrompt,
                    llmModel = GoogleModels.Gemini2_0FlashLite,
                    installFeatures = { install(EventHandler, eventHandler) },
                    toolRegistry = toolRegistry,
                    strategy = strategyStr,
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
                    strategy = strategyStr,
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
                    strategy = strategyStr,
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
                    strategy = strategyStr,
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
                    strategy = strategyStr,
                    temperature = 0.9,
                    //maxIterations = 5,
                )
            }
        }
    }

    suspend fun run(prompt: String) { _agent.run(prompt) }

}