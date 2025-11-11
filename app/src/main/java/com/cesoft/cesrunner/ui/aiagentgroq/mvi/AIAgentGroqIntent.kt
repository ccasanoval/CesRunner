package com.cesoft.cesrunner.ui.aiagentgroq.mvi

import com.adidas.mvi.Intent

sealed class AIAgentGroqIntent: Intent {
    data object Back: AIAgentGroqIntent()
    data class ExecPrompt(
        val prompt: String
    ): AIAgentGroqIntent()
    data class GoToTrack(
        val idTrack: Long
    ): AIAgentGroqIntent()
}