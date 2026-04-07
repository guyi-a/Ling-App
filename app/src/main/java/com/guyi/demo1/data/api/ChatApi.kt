package com.guyi.demo1.data.api

import com.guyi.demo1.data.model.ApiResponse
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * 聊天 API 接口
 *
 * 注意：流式聊天接口 /api/chat/stream 使用 SSEManager，不在这里定义
 */
interface ChatApi {

    /**
     * 审批工具调用
     */
    @POST("/api/chat/approve")
    suspend fun approveToolUse(@Body request: ApprovalRequest): ApprovalResponse

    /**
     * 停止生成
     */
    @POST("/api/chat/{session_id}/stop")
    suspend fun stopGeneration(@Path("session_id") sessionId: String): ApiResponse<Unit>
}

/**
 * 审批请求
 */
@Serializable
data class ApprovalRequest(
    val request_id: String,
    val approved: Boolean
)

/**
 * 审批响应
 */
@Serializable
data class ApprovalResponse(
    val status: String,
    val approved: Boolean
)
