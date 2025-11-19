package com.cesoft.cesrunner.ui.aiagentgroq

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.adidas.mvi.MviHost
import com.adidas.mvi.State
import com.adidas.mvi.reducer.Reducer
import com.cesoft.cesrunner.Page
import com.cesoft.cesrunner.data.groq.GroqRunDto
import com.cesoft.cesrunner.domain.AppError
import com.cesoft.cesrunner.domain.entity.TrackDto
import com.cesoft.cesrunner.domain.usecase.groq.AskGroqUC
import com.cesoft.cesrunner.ui.aiagent.ai.RunEntity
import com.cesoft.cesrunner.ui.aiagent.ai.RunEntity.Companion.toDate
import com.cesoft.cesrunner.ui.aiagent.ai.RunEntity.Companion.toHoursMinutes
import com.cesoft.cesrunner.ui.aiagent.ai.RunEntity.Companion.toRunEntity
import com.cesoft.cesrunner.ui.aiagent.ai.RunEntity.Location
import com.cesoft.cesrunner.ui.aiagent.ai.RunEntity.Location.Companion.toLocation
import com.cesoft.cesrunner.ui.aiagentgroq.mvi.AIAgentGroqIntent
import com.cesoft.cesrunner.ui.aiagentgroq.mvi.AIAgentGroqSideEffect
import com.cesoft.cesrunner.ui.aiagentgroq.mvi.AIAgentGroqState
import com.cesoft.cesrunner.ui.aiagentgroq.mvi.AIAgentGroqTransform
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.Strictness
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.koin.compose.application.rememberKoinApplication
import kotlin.reflect.typeOf

class AIAgentGroqViewModel(
    private val askGroq: AskGroqUC,
    coroutineDispatcher: CoroutineDispatcher = Dispatchers.Default,
): ViewModel(), MviHost<AIAgentGroqIntent, State<AIAgentGroqState, AIAgentGroqSideEffect>> {
    private val reducer = Reducer(
        coroutineScope = viewModelScope,
        defaultDispatcher = coroutineDispatcher,
        initialInnerState = AIAgentGroqState.Init(),
        logger = null,
        intentExecutor = this::executeIntent
    )
    override val state = reducer.state
    override fun execute(intent: AIAgentGroqIntent) {
        reducer.executeIntent(intent)
    }
    private fun executeIntent(intent: AIAgentGroqIntent) =
        when(intent) {
            AIAgentGroqIntent.Back -> executeBack()
            is AIAgentGroqIntent.ExecPrompt -> executePrompt(intent.prompt)
            is AIAgentGroqIntent.GoToTrack -> executeGoToTrack(intent.idTrack)
        }

    fun consumeSideEffect(
        sideEffect: AIAgentGroqSideEffect,
        navController: NavController,
    ) {
        when(sideEffect) {
            AIAgentGroqSideEffect.Back -> {
                navController.popBackStack()
            }
            is AIAgentGroqSideEffect.GoToTrack -> {
                navController.navigate(Page.TrackDetail.createRoute(sideEffect.id))
            }
        }
    }

    private fun executeBack(): Flow<AIAgentGroqTransform.AddSideEffect> = flow {
        emit(AIAgentGroqTransform.AddSideEffect(AIAgentGroqSideEffect.Back))
    }

    private fun executeGoToTrack(id: Long):Flow<AIAgentGroqTransform.AddSideEffect> = flow {
        emit(AIAgentGroqTransform.AddSideEffect(AIAgentGroqSideEffect.GoToTrack(id)))
    }

    private fun executePrompt(prompt: String): Flow<AIAgentGroqTransform.GoInit> = flow {
        emit(AIAgentGroqTransform.GoInit(prompt = prompt, loading = true))

        val answer = askGroq(prompt)
        android.util.Log.e("AIAgentVM", "executePrompt:askGroq:------- ${answer.isSuccess} / ${answer.getOrNull()}")

        if(answer.isSuccess) {
            // Get objects
            val res = answer.getOrNull()
            if(res == null) {
                emit(AIAgentGroqTransform.GoInit(prompt = prompt, response = "", error = AppError.NotFound))
            }
            else {
                val i = res.indexOf("\"cesjson\":")//TODO: In groq call, also return RunEntity from the start, so grok can use vo2max etc
                val j = res.lastIndexOf("]")
                val typeOfT = object: TypeToken<List<GroqRunDto>>(){}.type
                var data = mutableListOf<GroqRunDto>()
                if(i >= 0 && res.length > i + 10 && j > 0) {
                    val txt = res.substring(i+10, j+1)
                    android.util.Log.e("AIAgentVM", "executePrompt:askGroq:txt------- $txt")
                    try {
                        val gson = GsonBuilder()
                            .setStrictness(Strictness.LENIENT)
                            .create()
                        data = gson.fromJson(txt, typeOfT)
                    }
                    catch (e: Exception) {
                        Log.e("AA", "--------********************************** data:e = $e / $txt")
                    }
                }
                val responseData = data
                Log.e("AA", "--------********************************** responseData $responseData")
                emit(AIAgentGroqTransform.GoInit(prompt = prompt, response = res, responseData = responseData))
            }
        }
        else {
            emit(AIAgentGroqTransform.GoInit(prompt = prompt, response = "", error = answer.exceptionOrNull()))
        }
    }

}
