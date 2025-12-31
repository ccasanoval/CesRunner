package com.cesoft.cesrunner.ui.aiagentgroq.mvi

import com.adidas.mvi.Intent

sealed class AIAgentGroqIntent: Intent {
    data class ExecPrompt(val prompt: String): AIAgentGroqIntent()
}