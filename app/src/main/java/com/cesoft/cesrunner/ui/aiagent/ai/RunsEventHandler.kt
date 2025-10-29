package com.cesoft.cesrunner.ui.aiagent.ai

import ai.koog.agents.features.eventHandler.feature.EventHandlerConfig

object RunsEventHandler {
    fun getEventHandlerConfig(
        onAgentCompleted: (String) -> Unit,
        onAgentExecutionFailed: (Throwable) -> Unit,
    ): EventHandlerConfig.() -> Unit = {
        onAgentCompleted { res ->
            android.util.Log.e("AIAgentVM", "onAgentCompleted------------- ${res.result} / ${res.eventType} / ${res.component1()} / ${res.component2()}")
            val response = res.result.toString()
            //cont.resume(AIAgentTransform.GoInit(prompt = prompt, response = response))
            onAgentCompleted(response)
        }
        onAgentExecutionFailed { fail ->
            android.util.Log.e("AIAgentVM", "onAgentExecutionFailed------------- ${fail.throwable} / ${fail.eventType} / ${fail.component1()} / ${fail.component2()}"
            )
            val error = fail.throwable
            //cont.resume(AIAgentTransform.GoInit(prompt = prompt, response = "", error = error))
            onAgentExecutionFailed(error)
        }

        onToolCallCompleted { res ->
            android.util.Log.e("AIAgentVM", "onToolCallCompleted------------- $res ")
        }
        onToolCallFailed { res ->
            android.util.Log.e("AIAgentVM", "onToolCallFailed------------- $res ")
        }

        onAgentClosing { res ->
            android.util.Log.e("AIAgentVM", "onAgentClosing------------- ${res.eventType} / ${res.component1()}")
        }
        onAgentStarting { res ->
            android.util.Log.e("AIAgentVM", "onAgentStarting------------- ${res.eventType} / ${res.component1()} / ${res.context.agentInput} / ${res.agent.agentConfig.prompt} / ${res.agent.getState()}")
        }

        onLLMCallCompleted { res ->
            android.util.Log.e("AIAgentVM", "onLLMCallCompleted------------- ${res.eventType} / ${res.component1()} / ${res.prompt}")
            for (r in res.responses) android.util.Log.e("AIAgentVM", "onLLMCallCompleted:responses--------- ${r.content} / ${r.role} / ${r.metaInfo.metadata}")
        }
        onLLMCallStarting { res ->
            android.util.Log.e("AIAgentVM", "onLLMCallStarting------------- ${res.eventType} / ${res.component1()} / ${res.prompt}")
        }
    }
}
