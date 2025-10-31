package com.cesoft.cesrunner.ui.aiagent

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import aws.smithy.kotlin.runtime.util.length
import com.adidas.mvi.MviHost
import com.adidas.mvi.State
import com.adidas.mvi.reducer.Reducer
import com.cesoft.cesrunner.domain.usecase.ai.FilterTracksUC
import com.cesoft.cesrunner.ui.aiagent.ai.RunEntity
import com.cesoft.cesrunner.ui.aiagent.ai.RunsAgent
import com.cesoft.cesrunner.ui.aiagent.mvi.AIAgentIntent
import com.cesoft.cesrunner.ui.aiagent.mvi.AIAgentSideEffect
import com.cesoft.cesrunner.ui.aiagent.mvi.AIAgentState
import com.cesoft.cesrunner.ui.aiagent.mvi.AIAgentTransform
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.Strictness
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


// TODO: Local LLM:
// https://vivekparasharr.medium.com/how-i-ran-a-local-llm-on-my-android-phone-and-what-i-learned-about-googles-ai-edge-gallery-807572211562
//TODO: MCP...
//TODO: Desde UI pude elegir que LLM usar: OpenAI, Gemini, ...
//TODO: Que ruta esta cerca de aqui: Tengo que hacer una herramienta de gps y que TrackUiDto devuelva una lat/lng (la ultima, por ejemplo)
class AIAgentViewModel(
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
            AIAgentIntent.Back -> executeBack()
            is AIAgentIntent.ExecPrompt -> executePrompt(intent.prompt)
        }

    fun consumeSideEffect(
        sideEffect: AIAgentSideEffect,
        navController: NavController,
        context: Context,
    ) {
        when(sideEffect) {
            AIAgentSideEffect.Back -> { navController.popBackStack() }
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
            val myType = object : TypeToken<List<RunEntity>>() {}.type
            return gson.fromJson<List<RunEntity>>(json, myType)
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

    private fun jsonToUi(json: String): String {
        val gson = GsonBuilder()
            .setStrictness(Strictness.LENIENT)
            .create()
        try {
            val myType = object : TypeToken<List<RunEntity>>() {}.type
            val os = gson.fromJson<List<RunEntity>>(json, myType)
            val bs = StringBuilder()
            for(o in os) {//TODO: I18N
                bs.append("------------------------ \n")
                bs.append("${o.id} - ${o.name} \n")
                bs.append("Fecha: ${o.timeIni}  VO2Max: ${o.vo2Max} \n")
                bs.append("Tiempo: ${o.time}    Distancia: ${o.distance} m \n")//To Km if > 1000m
            }
            return bs.toString()
        } catch (e: Exception) {
            android.util.Log.e("AIAgentVM", "executePrompt:ok:---LIST----e: $e")
            try {
                val o = gson.fromJson(json, RunEntity::class.java)
                val bs = StringBuilder()
                bs.append("------------------------------------------------ \n")//TODO: Pass the object to the view
                bs.append("(ID ${o.id}) - ${o.name} \n")
                bs.append("Fecha: ${o.timeIni}  VO2Max: ${o.vo2Max} \n")
                bs.append("Tiempo: ${o.time}    Distancia: ${o.distance} m \n")//To Km if > 1000m
                return bs.toString()
            } catch (e: Exception) {
                android.util.Log.e("AIAgentVM", "executePrompt:ok:---OBJ----e: $e")
            }
        }
        return ""
    }

    private fun executePrompt(prompt: String) = flow {
        emit(AIAgentTransform.GoInit(prompt = prompt, loading = true))
        val callbackResult: AIAgentTransform.GoInit = suspendCoroutine { cont ->
            val agent = RunsAgent(
                model = RunsAgent.Model.GEMINI,
                filterTracks = filterTracks,
                onAgentCompleted = { response ->
                    android.util.Log.e("AIAgentVM", "executePrompt:ok:------- $response")
                    val i = response.indexOf("json")
                    if(i >= 3) {
                        val llm = response.substring(0, i-4)
                        val json = response.substring(i+4, response.length-3)
                        android.util.Log.e("AIAgentVM", "executePrompt:ok:LLM------- $llm")
                        android.util.Log.e("AIAgentVM", "executePrompt:ok:JSON------- $json")
                        val msg = jsonToUi(json)
                        android.util.Log.e("AIAgentVM", "executePrompt:ok:MSG------- $msg")
                        val data = jsonToRunEntity(json)
                        cont.resume(AIAgentTransform.GoInit(prompt = prompt, response = llm, responseData = data))
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
