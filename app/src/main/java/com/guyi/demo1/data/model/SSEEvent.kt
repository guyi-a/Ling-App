package com.guyi.demo1.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * SSE 事件基类
 */
sealed class SSEEvent {
    /**
     * 会话信息事件
     */
    @Serializable
    data class SessionEvent(
        @SerialName("session_id")
        val sessionId: String,
        @SerialName("is_new_session")
        val isNewSession: Boolean = false,
        @SerialName("user_message_id")
        val userMessageId: String? = null
    ) : SSEEvent()

    /**
     * Token 流式输出事件
     */
    @Serializable
    data class TokenEvent(
        val text: String
    ) : SSEEvent()

    /**
     * 工具开始执行事件
     */
    @Serializable
    data class ToolStartEvent(
        @SerialName("tool_name")
        val toolName: String,
        @SerialName("tool_input")
        val toolInput: Map<String, String>? = null
    ) : SSEEvent()

    /**
     * 工具执行完成事件
     */
    @Serializable
    data class ToolEndEvent(
        @SerialName("tool_name")
        val toolName: String
    ) : SSEEvent()

    /**
     * 需要审批事件
     */
    @Serializable
    data class ApprovalRequiredEvent(
        @SerialName("request_id")
        val requestId: String,
        @SerialName("tool_name")
        val toolName: String,
        @SerialName("tool_input")
        val toolInput: kotlinx.serialization.json.JsonObject = kotlinx.serialization.json.JsonObject(emptyMap())
    ) : SSEEvent()

    /**
     * 审批被拒绝事件
     */
    @Serializable
    data class ApprovalRejectedEvent(
        @SerialName("tool_name")
        val toolName: String
    ) : SSEEvent()

    /**
     * 生成完成事件
     */
    @Serializable
    data class DoneEvent(
        @SerialName("assistant_message_id")
        val assistantMessageId: String
    ) : SSEEvent()

    /**
     * 取消事件
     */
    object CancelledEvent : SSEEvent()

    /**
     * 错误事件
     */
    @Serializable
    data class ErrorEvent(
        val message: String
    ) : SSEEvent()
}
