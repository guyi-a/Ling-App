package com.guyi.demo1.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed class SSEEvent {

    @Serializable
    data class SessionEvent(
        @SerialName("session_id")
        val sessionId: String,
        @SerialName("is_new_session")
        val isNewSession: Boolean = false,
        @SerialName("user_message_id")
        val userMessageId: String? = null
    ) : SSEEvent()

    @Serializable
    data class TokenEvent(
        val text: String
    ) : SSEEvent()

    object ModelStartEvent : SSEEvent()

    @Serializable
    data class ToolStartEvent(
        @SerialName("tool_name")
        val toolName: String,
        @SerialName("tool_input")
        val toolInput: kotlinx.serialization.json.JsonObject? = null
    ) : SSEEvent()

    @Serializable
    data class ToolEndEvent(
        @SerialName("tool_name")
        val toolName: String,
        @SerialName("tool_output")
        val toolOutput: String? = null
    ) : SSEEvent()

    @Serializable
    data class ToolGeneratingEvent(
        @SerialName("tool_name")
        val toolName: String
    ) : SSEEvent()

    @Serializable
    data class ApprovalRequiredEvent(
        @SerialName("request_id")
        val requestId: String,
        @SerialName("tool_name")
        val toolName: String,
        @SerialName("tool_input")
        val toolInput: kotlinx.serialization.json.JsonObject = kotlinx.serialization.json.JsonObject(emptyMap())
    ) : SSEEvent()

    @Serializable
    data class ApprovalRejectedEvent(
        @SerialName("tool_name")
        val toolName: String
    ) : SSEEvent()

    @Serializable
    data class HandoffEvent(
        val to: String,
        val direction: String = "to"
    ) : SSEEvent()

    object CompactingEvent : SSEEvent()

    object CompactingDoneEvent : SSEEvent()

    @Serializable
    data class DoneEvent(
        @SerialName("assistant_message_id")
        val assistantMessageId: String? = null
    ) : SSEEvent()

    object CancelledEvent : SSEEvent()

    @Serializable
    data class ErrorEvent(
        val message: String
    ) : SSEEvent()
}
