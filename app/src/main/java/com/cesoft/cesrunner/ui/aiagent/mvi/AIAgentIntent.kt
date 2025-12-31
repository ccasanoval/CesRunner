package com.cesoft.cesrunner.ui.aiagent.mvi

import com.adidas.mvi.Intent

sealed class AIAgentIntent: Intent {
    //data object Back: AIAgentIntent()
    data class ExecPrompt(val prompt: String): AIAgentIntent()
    //data class GoToTrack(val idTrack: Long): AIAgentIntent()
}