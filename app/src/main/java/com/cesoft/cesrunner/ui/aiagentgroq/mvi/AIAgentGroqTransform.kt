package com.cesoft.cesrunner.ui.aiagentgroq.mvi

import com.adidas.mvi.sideeffects.SideEffects
import com.adidas.mvi.transform.SideEffectTransform
import com.adidas.mvi.transform.ViewTransform
import com.cesoft.cesrunner.domain.entity.RunDto

object AIAgentGroqTransform {

    data class GoInit(
        val prompt: String = "",
        val response: String = "",
        val responseData: List<RunDto> = listOf(),
        val loading: Boolean = false,
        val error: Throwable? = null
    ): ViewTransform<AIAgentGroqState, AIAgentGroqSideEffect>() {
        override fun mutate(currentState: AIAgentGroqState): AIAgentGroqState {
            return AIAgentGroqState.Init(prompt, response, responseData, loading, error)
        }
    }

    data class AddSideEffect(
        val sideEffect: AIAgentGroqSideEffect
    ): SideEffectTransform<AIAgentGroqState, AIAgentGroqSideEffect>() {
        override fun mutate(sideEffects: SideEffects<AIAgentGroqSideEffect>): SideEffects<AIAgentGroqSideEffect> {
            return sideEffects.add(sideEffect)
        }
    }
}