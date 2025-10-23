package com.cesoft.cesrunner.ui.aiagent.mvi

sealed class AIAgentSideEffect {
    data object Close : AIAgentSideEffect()
}