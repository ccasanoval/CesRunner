package com.cesoft.cesrunner.ui.aiagent.ai

import ai.koog.agents.features.eventHandler.feature.EventHandlerConfig
import android.util.Log

object RunsEventHandler {
    private const val TAG = "RunsEventHandler"

    fun getEventHandlerConfig(
        onAgentCompleted: (String) -> Unit,
        onAgentExecutionFailed: (Throwable) -> Unit,
    ): EventHandlerConfig.() -> Unit = {
        onAgentCompleted { res ->
            onAgentCompleted(res.result.toString())
        }
        onAgentExecutionFailed { fail ->
            Log.e(TAG, "onAgentExecutionFailed: ${fail.throwable}")
            onAgentExecutionFailed(fail.throwable)
        }
        onToolCallCompleted { res -> Log.d(TAG, "onToolCallCompleted: $res") }
        onToolCallFailed { res -> Log.e(TAG, "onToolCallFailed: $res") }
        onLLMCallCompleted { res ->
            for(r in res.responses) Log.d(TAG, "onLLMCallCompleted: ${r.role}: ${r.content}")
        }
    }
}
