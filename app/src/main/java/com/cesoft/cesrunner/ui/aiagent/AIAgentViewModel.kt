package com.cesoft.cesrunner.ui.aiagent

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.adidas.mvi.MviHost
import com.adidas.mvi.State
import com.adidas.mvi.reducer.Reducer
import com.cesoft.cesrunner.Page
import com.cesoft.cesrunner.domain.usecase.GetLocationUC
import com.cesoft.cesrunner.domain.usecase.ai.FilterTracksUC
import com.cesoft.cesrunner.domain.usecase.ai.GetNearTracksUC
import com.cesoft.cesrunner.ui.aiagent.ai.RunEntity
import com.cesoft.cesrunner.ui.aiagent.ai.RunsAgent
import com.cesoft.cesrunner.ui.aiagent.mvi.AIAgentIntent
import com.cesoft.cesrunner.ui.aiagent.mvi.AIAgentSideEffect
import com.cesoft.cesrunner.ui.aiagent.mvi.AIAgentState
import com.cesoft.cesrunner.ui.aiagent.mvi.AIAgentTransform
import com.google.gson.GsonBuilder
import com.google.gson.Strictness
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

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
//TODO: MCP...
//TODO: Desde UI pude elegir que LLM usar: OpenAI, Gemini, ...
class AIAgentViewModel(
    private val filterTracks: FilterTracksUC,
    private val getLocation: GetLocationUC,
    private val getNearTracks: GetNearTracksUC,
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
            AIAgentIntent.Back -> executeBack()
            is AIAgentIntent.ExecPrompt -> executePrompt(intent.prompt)
            is AIAgentIntent.GoToTrack -> executeGoToTrack(intent.idTrack)
        }

    fun consumeSideEffect(
        sideEffect: AIAgentSideEffect,
        navController: NavController,
    ) {
        when(sideEffect) {
            AIAgentSideEffect.Back -> {
                navController.popBackStack()
            }
            is AIAgentSideEffect.GoToTrack -> {
                navController.navigate(Page.TrackDetail.createRoute(sideEffect.id))
            }
        }
    }

    private fun executeBack() = flow {
        emit(AIAgentTransform.AddSideEffect(AIAgentSideEffect.Back))
    }

    private fun jsonToRunEntity(json: String): List<RunEntity> {
        val gson = GsonBuilder()
            .setStrictness(Strictness.LENIENT)
            .create()
        try {
            val typeOfT = object : TypeToken<List<RunEntity>>() {}.type
            return gson.fromJson(json, typeOfT)
        } catch (e: Exception) {
            android.util.Log.e("AIAgentVM", "executePrompt:ok:---LIST----e: $e")
            try {
                return listOf(gson.fromJson(json, RunEntity::class.java))
            } catch (e: Exception) {
                android.util.Log.e("AIAgentVM", "executePrompt:ok:---OBJ----e: $e")
            }
        }
        return listOf()
    }

    private fun executeGoToTrack(id: Long) = flow {
        emit(AIAgentTransform.AddSideEffect(AIAgentSideEffect.GoToTrack(id)))
    }

    private fun executePrompt(prompt: String) = flow {
        emit(AIAgentTransform.GoInit(prompt = prompt, loading = true))
        val callbackResult: AIAgentTransform.GoInit = suspendCoroutine { cont ->
            val agent = RunsAgent(
                model = RunsAgent.Model.GEMINI,//TODO: Gemini seems the only one working, delete the rest?
                filterTracks = filterTracks,
                getLocation = getLocation,
                getNearTracks = getNearTracks,
                onAgentCompleted = { response ->
                    android.util.Log.e("AIAgentVM", "executePrompt:ok:------- $response")
                    val i = response.indexOf("json")
                    if(i >= 3) {
                        try {
                            android.util.Log.e("AIAgentVM", "executePrompt:ok:i------- $i")
                            val llm = response.substring(0, i-3)
                            android.util.Log.e("AIAgentVM", "executePrompt:ok:LLM------- $llm")
                            val json = response.substring(i + 4, response.length - 3)
                            android.util.Log.e("AIAgentVM", "executePrompt:ok:JSON------- $json")
                            val data = jsonToRunEntity(json)
                            val value = AIAgentTransform.GoInit(prompt = prompt, response = llm, responseData = data)
                            cont.resume(value)
                        }
                        catch (e: Exception) {
                            val value = AIAgentTransform.GoInit(prompt = prompt, response = response)
                            cont.resume(value)
                        }
                    }
                    else {
                        cont.resume(AIAgentTransform.GoInit(prompt = prompt, response = response))
                    }
                },
                onAgentExecutionFailed = { error ->
                    cont.resume(AIAgentTransform.GoInit(prompt = prompt, response = "", error = error))
                }
            )
            viewModelScope.launch {
                try { agent.run(prompt) }
                catch (e: Throwable) { Log.e("AIAgentVM", "executePrompt:e:------ $e") }
            }
        }
        emit(callbackResult)
    }

 }
