package com.cesoft.cesrunner.ui.aiagent

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

    //TODO: use local model ?
    //https://vivekparasharr.medium.com/how-i-ran-a-local-llm-on-my-android-phone-and-what-i-learned-about-googles-ai-edge-gallery-807572211562
    //TODO: strategy...
    //TODO: MCP...
    //TODO: Desde UI pude elegir que LLM usar: OpenAI, Gemini, ...
    private fun executePrompt(prompt: String) = flow {
        val a = filterTracks(name = "canari")
        android.util.Log.e("AA", "AAA-------------------------------- ${a.getOrNull()?.firstOrNull()?.name}")
        emit(AIAgentTransform.GoInit(prompt = prompt, loading = true))
        val callbackResult: AIAgentTransform.GoInit = suspendCoroutine { cont ->
            val agent = RunsAgent(
                model = RunsAgent.Model.GEMINI,
                filterTracks = filterTracks,
                onAgentCompleted = { response ->
                    cont.resume(AIAgentTransform.GoInit(prompt = prompt, response = response))
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
