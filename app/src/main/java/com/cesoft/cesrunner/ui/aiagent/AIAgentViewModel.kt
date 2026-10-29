package com.cesoft.cesrunner.ui.aiagent

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adidas.mvi.MviHost
import com.adidas.mvi.State
import com.adidas.mvi.reducer.Reducer
import com.cesoft.cesrunner.domain.AppError
import com.cesoft.cesrunner.domain.entity.ApiKeysDto
import com.cesoft.cesrunner.domain.usecase.GetLocationUC
import com.cesoft.cesrunner.domain.usecase.ReadApiKeysUC
import com.cesoft.cesrunner.domain.usecase.ai.FilterTracksUC
import com.cesoft.cesrunner.domain.usecase.ai.GetNearTracksUC
import com.cesoft.cesrunner.ui.aiagent.ai.RunEntity
import com.cesoft.cesrunner.ui.aiagent.ai.RunsAgent
import com.cesoft.cesrunner.ui.aiagent.mvi.AIAgentIntent
import com.cesoft.cesrunner.ui.aiagent.mvi.AIAgentSideEffect
import com.cesoft.cesrunner.ui.aiagent.mvi.AIAgentState
import com.cesoft.cesrunner.ui.aiagent.mvi.AIAgentTransform
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json

//TODO: AGENTE DE ACCION (fuera de busquedas)
//   En pagina principal: User dice: "Empieza una nueva carrera en 10 segundos"
//     Y el agente cerrara la carrera actual si la hay, esperara 10 segundos y lanzara una nueva carrera...
//   En pagina principal: User dice: "Muestra las carreras que tienen mayor vo2max"
//     Y el agente abrira la pagina de listado de carreras con las carreras resultado
//   En pagina principal: User dice: "Muestra las carreras que tienen mayor vo2max en mapa"
//     Y el agente abrira la pagina de mapa con las carreras resultado
//   En pagina principal: User dice: "Cambia la distancia minima a 2 metros"
//     Y el agente abrira la pagina de opciones y pondra la opcion distancia a 2 metros y guardara
//(reconocimiento de voz)
//https://medium.com/@volodymyrpastukh99/implementing-voice-controller-in-android-application-by-using-google-speech-to-text-api-615dff546587
// PAso 1: reconocer voz y abrir ventanas en respuesta: "Abre ventana de listado de carreras" + "Abre mapa" + "Abre configuracion" + "Abre nueva carrera"

// TODO: Local LLM:
// https://vivekparasharr.medium.com/how-i-ran-a-local-llm-on-my-android-phone-and-what-i-learned-about-googles-ai-edge-gallery-807572211562
//TODO: Desde UI pude elegir que LLM usar: OpenAI, Gemini, ...
//TODO:NON-GRAPH: https://medium.com/kotlin-academy/non-graph-strategies-and-when-to-use-them-in-ai-agents-eb0cee6dba73
//TODO: Gemini Nano local LLM => Only for devices with big memory and cpu

class AIAgentViewModel(
    private val filterTracks: FilterTracksUC,
    private val getLocation: GetLocationUC,
    private val getNearTracks: GetNearTracksUC,
    private val readApiKeys: ReadApiKeysUC,
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
             is AIAgentIntent.ExecPrompt -> executePrompt(intent.prompt)
         }

    /// The LLM answers with text followed by a ```json block with the selected runs
    private fun parseResponse(prompt: String, response: String): AIAgentTransform.GoInit {
        val i = response.indexOf("json")
        if(i < 3) return AIAgentTransform.GoInit(prompt = prompt, response = response)
        return try {
            val llm = response.substring(0, i - 3)
            val json = response.substring(i + 4, response.length - 3)
            AIAgentTransform.GoInit(prompt = prompt, response = llm, responseData = jsonToRunEntity(json))
        }
        catch(e: Exception) {
            AIAgentTransform.GoInit(prompt = prompt, response = response)
        }
    }

    private fun executePrompt(prompt: String) = flow {
        emit(AIAgentTransform.GoInit(prompt = prompt, loading = true))

        val model = MODEL
        val apiKey = RunsAgent.apiKeyFor(model, readApiKeys().getOrNull() ?: ApiKeysDto.Empty)
        if(RunsAgent.needsApiKey(model) && apiKey.isBlank()) {
            emit(AIAgentTransform.GoInit(prompt = prompt, response = "", error = AppError.MissingApiKey))
            return@flow
        }

        // The agent answers through callbacks; complete() only keeps the first result
        val result = CompletableDeferred<AIAgentTransform.GoInit>()
        val agent = RunsAgent(
            model = model,
            apiKey = apiKey,
            filterTracks = filterTracks,
            getLocation = getLocation,
            getNearTracks = getNearTracks,
            onAgentCompleted = { response -> result.complete(parseResponse(prompt, response)) },
            onAgentExecutionFailed = { error ->
                result.complete(AIAgentTransform.GoInit(prompt = prompt, response = "", error = error))
            }
        )
        try {
            agent.run(prompt)
        }
        catch(e: Exception) {
            Log.e(TAG, "executePrompt:e: $e")
            result.complete(AIAgentTransform.GoInit(prompt = prompt, response = "", error = e))
        }
        // In case the agent finished without calling any callback, don't wait forever
        result.complete(AIAgentTransform.GoInit(prompt = prompt, response = ""))
        emit(result.await())
    }

    companion object {
        private const val TAG = "AIAgentVM"
        private val json = Json {
            isLenient = true
            ignoreUnknownKeys = true
        }

        /// A run with missing fields is rejected instead of being built with nulls in non-null properties
        internal fun jsonToRunEntity(text: String): List<RunEntity> {
            try {
                return json.decodeFromString<List<RunEntity>>(text)
            } catch (e: Exception) {
                try {
                    return listOf(json.decodeFromString<RunEntity>(text))
                } catch (e: Exception) {
                    Log.e(TAG, "jsonToRunEntity:e: ${e::class.simpleName}")
                }
            }
            return listOf()
        }
        private val MODEL = RunsAgent.Model.GEMINI//TODO: Gemini seems the only one working, delete the rest?
    }
}
