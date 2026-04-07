package com.guyi.demo1.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
    val type: String,  // "text" | "tool"
    val content: String? = null,
    val toolName: String? = null,
    val toolStatus: String? = null  // "pending" | "done" | "rejected"
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
