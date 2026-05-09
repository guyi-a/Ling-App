package com.guyi.demo1.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * 消息
 */
@Serializable
data class Message(
    val id: String,
    @SerialName("message_id")
    val messageId: String? = null,
    val role: String,  // "user" | "assistant"
    val parts: List<MessagePart> = emptyList(),
    val isStreaming: Boolean = false,
    val approvalRequest: ApprovalRequest? = null
)

/**
 * 消息片段
 */
@Serializable
data class MessagePart(
    val type: String,  // "text" | "tool" | "handoff"
    val content: String? = null,
    val toolName: String? = null,  // tool: 显示名（Skill 会替换为真实 skill 名）
    val toolStatus: String? = null,  // "pending" | "generating" | "done" | "rejected" | "cancelled"
    val toolInput: JsonObject? = null,  // tool: 输入参数（展开时显示）
    val toolOutput: String? = null,  // tool: 输出文本（展开时显示）
    val isSkill: Boolean = false,  // tool: 是否 Skill 加载（影响状态文案）
    val agentName: String? = null,
    val handoffDirection: String? = null  // "to" | "back"
)

/**
 * 工具审批请求
 */
@Serializable
data class ApprovalRequest(
    val requestId: String,
    val toolName: String,
    val toolInput: kotlinx.serialization.json.JsonObject = kotlinx.serialization.json.JsonObject(emptyMap()),
    val remaining: Int = 60
)
