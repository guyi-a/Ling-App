package com.guyi.demo1.data.remote.dto

import com.google.gson.annotations.SerializedName

// 会话 DTO
data class SessionDto(
    @SerializedName("session_id")
    val sessionId: String,

    val title: String,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("updated_at")
    val updatedAt: String,

    @SerializedName("message_count")
    val messageCount: Int? = null
)

// 创建会话请求
data class CreateSessionRequest(
    val title: String
)
