package com.guyi.demo1.data.remote.websocket

import com.google.gson.annotations.SerializedName

sealed class SseEvent {
    data class Session(val data: SessionData) : SseEvent()
    data class Token(val text: String) : SseEvent()
    data object ModelStart : SseEvent()
    data class ToolStart(val toolName: String) : SseEvent()
    data class ToolEnd(val toolName: String) : SseEvent()
    data class ApprovalRequired(
        val requestId: String,
        val toolName: String,
        val toolInput: Map<String, Any>
    ) : SseEvent()
    data class ApprovalRejected(val toolName: String) : SseEvent()
    data object Done : SseEvent()
    data class Error(val message: String) : SseEvent()
}

// SSE 会话数据
data class SessionData(
    @SerializedName("session_id")
    val sessionId: String,

    @SerializedName("user_message_id")
    val userMessageId: String,

    @SerializedName("is_new_session")
    val isNewSession: Boolean
)

// Token 数据
data class TokenData(
    val text: String
)

// 工具数据
data class ToolData(
    @SerializedName("tool_name")
    val toolName: String
)

// 审批数据
data class ApprovalData(
    @SerializedName("request_id")
    val requestId: String,

    @SerializedName("tool_name")
    val toolName: String,

    @SerializedName("tool_input")
    val toolInput: Map<String, Any>
)

// 错误数据
data class ErrorData(
    val message: String
)
