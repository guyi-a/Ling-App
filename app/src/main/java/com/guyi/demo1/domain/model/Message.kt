package com.guyi.demo1.domain.model

data class Message(
    val messageId: String,
    val sessionId: String,
    val role: MessageRole,
    val content: String,
    val createdAt: String,
    val isStreaming: Boolean = false
)

enum class MessageRole {
    USER,
    ASSISTANT,
    SYSTEM
}
