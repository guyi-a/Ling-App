package com.guyi.demo1.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 消息历史响应
 */
@Serializable
data class MessageHistory(
    @SerialName("session_id")
    val sessionId: String,
    val messages: List<HistoryMessage>
)

/**
 * 历史消息（后端格式）
 */
@Serializable
data class HistoryMessage(
    val role: String,
    val content: String,
    @SerialName("message_id")
    val messageId: String? = null,
    @SerialName("extra_data")
    val extraData: ExtraData? = null
)

/**
 * 消息扩展数据
 */
@Serializable
data class ExtraData(
    @SerialName("tool_calls")
    val toolCalls: List<ToolCall>? = null,
    val attachments: List<Attachment>? = null
)

/**
 * 工具调用
 */
@Serializable
data class ToolCall(
    val name: String,
    val args: Map<String, String>? = null
)

/**
 * 附件
 */
@Serializable
data class Attachment(
    val type: String,  // "image" | "file"
    val path: String,
    val size: Long
)
