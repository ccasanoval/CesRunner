package com.cesoft.cesrunner.ui.aiagent.mvi

sealed class AIAgentSideEffect {
    data object Back : AIAgentSideEffect()
}