package com.cesoft.cesrunner.ui.aiagentgroq.mvi

import com.adidas.mvi.LoggableState
import com.cesoft.cesrunner.data.groq.GroqRunDto

sealed class AIAgentGroqState: LoggableState {
    data class Init(
        val prompt: String = "",
        val response: String = "",
        val responseData: List<GroqRunDto> = listOf(),
        val loading: Boolean = false,
        val error: Throwable? = null
    ): AIAgentGroqState()
}