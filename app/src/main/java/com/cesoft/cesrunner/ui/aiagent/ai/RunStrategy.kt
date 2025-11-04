package com.cesoft.cesrunner.ui.aiagent.ai

import ai.koog.agents.core.agent.entity.AIAgentGraphStrategy
import ai.koog.agents.core.dsl.builder.forwardTo
import ai.koog.agents.core.dsl.builder.strategy
import ai.koog.agents.core.dsl.extension.nodeExecuteTool
import ai.koog.agents.core.dsl.extension.nodeLLMRequest
import ai.koog.agents.core.dsl.extension.nodeLLMSendToolResult
import ai.koog.agents.core.dsl.extension.onAssistantMessage
import ai.koog.agents.core.dsl.extension.onToolCall

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
