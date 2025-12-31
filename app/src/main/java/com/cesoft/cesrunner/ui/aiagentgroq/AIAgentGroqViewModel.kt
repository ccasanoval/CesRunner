package com.cesoft.cesrunner.ui.aiagentgroq

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adidas.mvi.MviHost
import com.adidas.mvi.State
import com.adidas.mvi.reducer.Reducer
import com.cesoft.cesrunner.domain.AppError
import com.cesoft.cesrunner.domain.entity.AIAgentRes
import com.cesoft.cesrunner.domain.usecase.groq.AskGroqUC
import com.cesoft.cesrunner.ui.aiagentgroq.mvi.AIAgentGroqIntent
import com.cesoft.cesrunner.ui.aiagentgroq.mvi.AIAgentGroqSideEffect
import com.cesoft.cesrunner.ui.aiagentgroq.mvi.AIAgentGroqState
import com.cesoft.cesrunner.ui.aiagentgroq.mvi.AIAgentGroqTransform
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

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
            is AIAgentGroqIntent.ExecPrompt -> executePrompt(intent.prompt)
        }

    private fun executePrompt(prompt: String): Flow<AIAgentGroqTransform.GoInit> = flow {
        emit(AIAgentGroqTransform.GoInit(prompt = prompt, loading = true))
        val answer: Result<AIAgentRes> = askGroq(prompt)
        if(answer.isSuccess) {
            val res = answer.getOrNull()
            if(res == null) {
                emit(AIAgentGroqTransform.GoInit(prompt = prompt, response = "", error = AppError.NotFound))
            }
            else {
                emit(AIAgentGroqTransform.GoInit(prompt = prompt, response = res.msg, responseData = res.data))
            }
        }
        else {
            emit(AIAgentGroqTransform.GoInit(prompt = prompt, response = "", error = answer.exceptionOrNull()))
        }
    }

}
