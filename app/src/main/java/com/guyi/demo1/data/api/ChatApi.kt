package com.guyi.demo1.data.api

import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface ChatApi {

    @POST("/api/chat/approve")
    suspend fun approveToolUse(@Body request: ApprovalRequest): ApprovalResponse

    @POST("/api/chat/{session_id}/stop")
    suspend fun stopGeneration(@Path("session_id") sessionId: String): StopResponse
}

@Serializable
data class ApprovalRequest(
    val request_id: String,
    val approved: Boolean,
    val always_allow: Boolean = false,
    val tool_name: String? = null
)

@Serializable
data class ApprovalResponse(
    val status: String,
    val approved: Boolean
)

@Serializable
data class StopResponse(
    val status: String,
    val message: String = ""
)
