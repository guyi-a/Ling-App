package com.guyi.demo1.data.remote.dto

import com.google.gson.annotations.SerializedName

// 聊天请求
data class ChatRequest(
    val message: String,
    @SerializedName("session_id")
    val sessionId: String? = null
)

// 聊天响应（非流式）
data class ChatResponse(
    @SerializedName("session_id")
    val sessionId: String,

    @SerializedName("user_message_id")
    val userMessageId: String,

    @SerializedName("assistant_response")
    val assistantResponse: String,

    @SerializedName("is_new_session")
    val isNewSession: Boolean
)

// 历史消息响应
data class HistoryResponse(
    @SerializedName("session_id")
    val sessionId: String,

    val title: String,

    @SerializedName("message_count")
    val messageCount: Int,

    val messages: List<MessageDto>
)

// 消息 DTO
data class MessageDto(
    @SerializedName("message_id")
    val messageId: String,

    @SerializedName("session_id")
    val sessionId: String,

    val role: String, // "user" | "assistant"

    val content: String,

    @SerializedName("created_at")
    val createdAt: String
)

// 工具审批请求
data class ApprovalRequest(
    @SerializedName("request_id")
    val requestId: String,

    val approved: Boolean
)

// 工具审批响应
data class ApprovalResponse(
    val status: String,
    val approved: Boolean
)
