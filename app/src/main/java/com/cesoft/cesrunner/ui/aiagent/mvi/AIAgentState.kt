package com.cesoft.cesrunner.ui.aiagent.mvi

import com.adidas.mvi.LoggableState
import com.cesoft.cesrunner.ui.aiagent.ai.RunEntity

sealed class AIAgentState: LoggableState {
    //data object Loading: AIAgentState()
    data class Init(
        val prompt: String = "",
        val response: String = "",
        val responseData: List<RunEntity> = listOf(),
        val loading: Boolean = false,
        val error: Throwable? = null
    ): AIAgentState()
}