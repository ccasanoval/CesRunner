package com.cesoft.cesrunner.ui.aiagent.mvi

import com.adidas.mvi.sideeffects.SideEffects
import com.adidas.mvi.transform.SideEffectTransform
import com.adidas.mvi.transform.ViewTransform
import com.cesoft.cesrunner.ui.aiagent.ai.RunEntity

object AIAgentTransform {

    data class GoInit(
        val prompt: String = "",
        val response: String = "",
        val responseData: List<RunEntity> = listOf(),
        val loading: Boolean = false,
        val error: Throwable? = null
    ): ViewTransform<AIAgentState, AIAgentSideEffect>() {
        override fun mutate(currentState: AIAgentState): AIAgentState {
            //android.util.Log.e("AIAgentTransform", "GoInit-------------------- $prompt -> $response / $loading / $error")
            return AIAgentState.Init(prompt, response, responseData, loading, error)
        }
    }

    data class AddSideEffect(
        val sideEffect: AIAgentSideEffect
    ): SideEffectTransform<AIAgentState, AIAgentSideEffect>() {
        override fun mutate(sideEffects: SideEffects<AIAgentSideEffect>): SideEffects<AIAgentSideEffect> {
            return sideEffects.add(sideEffect)
        }
    }
}