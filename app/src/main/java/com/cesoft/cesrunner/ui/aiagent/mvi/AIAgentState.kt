package com.cesoft.cesrunner.ui.aiagent.mvi

import com.adidas.mvi.LoggableState

sealed class AIAgentState: LoggableState {
    data object Loading: AIAgentState()
    data class Init(
        val prompt: String = "",
        val response: String = "",
        val error: Throwable? = null
    ): AIAgentState()
}