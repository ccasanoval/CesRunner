package com.cesoft.cesrunner.ui.aiagent.ai

import ai.koog.agents.core.agent.entity.AIAgentGraphStrategy
import ai.koog.agents.core.dsl.builder.forwardTo
import ai.koog.agents.core.dsl.builder.strategy
import ai.koog.agents.core.dsl.extension.nodeExecuteTool
import ai.koog.agents.core.dsl.extension.nodeLLMRequest
import ai.koog.agents.core.dsl.extension.nodeLLMSendToolResult
import ai.koog.agents.core.dsl.extension.onAssistantMessage
import ai.koog.agents.core.dsl.extension.onToolCall
import ai.koog.agents.core.tools.ToolResult

val strategyDto: AIAgentGraphStrategy<String, Result<List<RunEntity>>> = strategy("Run finder") {
    val nodeCallLLM by nodeLLMRequest("mllRequest")
    val nodeExecuteTool by nodeExecuteTool("executeTool")
    val nodeSendToolResult by nodeLLMSendToolResult("sendToolResult")
    val nodeTransOutput by node<String, Result<List<RunEntity>>>("transformOutput") {
        android.util.Log.e("RunsAgent", "nodeTransOutput------ $it")
        Result.failure(Throwable(it))
    }
    val nodeTransToolOutput by node<ToolResult, Result<List<RunEntity>>>("transToolOutput") {
        val a : ToolResult = it
        android.util.Log.e("RunsAgent", "nodeTransOutput------")
        Result.failure(Throwable(it.toString()))
    }

    edge(nodeStart forwardTo nodeCallLLM)

    edge(nodeCallLLM forwardTo nodeTransOutput onAssistantMessage { true })
    edge(nodeTransOutput forwardTo nodeFinish)

    edge(nodeCallLLM forwardTo nodeExecuteTool onToolCall { true } )
    edge(nodeExecuteTool forwardTo nodeSendToolResult)

    edge(nodeSendToolResult forwardTo nodeTransOutput onAssistantMessage { true })
    edge(nodeTransOutput forwardTo nodeFinish)

    edge(nodeSendToolResult forwardTo nodeExecuteTool onToolCall { true })
}

val strategyStr: AIAgentGraphStrategy<String, String> = strategy("Run finder") {
    val nodeCallLLM by nodeLLMRequest("mllRequest")
    val nodeExecuteTool by nodeExecuteTool("executeTool")
    val nodeSendToolResult by nodeLLMSendToolResult("sendToolResult")

    edge(nodeStart forwardTo nodeCallLLM)

    edge(nodeCallLLM forwardTo nodeFinish onAssistantMessage { true })

    edge(nodeCallLLM forwardTo nodeExecuteTool onToolCall { true } )
    edge(nodeExecuteTool forwardTo nodeSendToolResult)

    edge(nodeSendToolResult forwardTo nodeFinish onAssistantMessage { true })

    edge(nodeSendToolResult forwardTo nodeExecuteTool onToolCall { true })
}
