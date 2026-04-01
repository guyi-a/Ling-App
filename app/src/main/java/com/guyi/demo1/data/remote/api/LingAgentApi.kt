package com.guyi.demo1.data.remote.api

import com.guyi.demo1.data.remote.dto.*
import retrofit2.http.*

interface LingAgentApi {

    // ==================== 认证 ====================

    @POST("/api/auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    @POST("/api/auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    // ==================== 聊天 ====================

    // 非流式聊天（备用）
    @POST("/api/chat")
    suspend fun sendMessage(@Body request: ChatRequest): ChatResponse

    // 工具审批
    @POST("/api/chat/approve")
    suspend fun approveTool(@Body request: ApprovalRequest): ApprovalResponse

    // 停止生成
    @POST("/api/chat/{sessionId}/stop")
    suspend fun stopGeneration(@Path("sessionId") sessionId: String)

    // 获取历史记录
    @GET("/api/chat/{sessionId}/history")
    suspend fun getHistory(
        @Path("sessionId") sessionId: String,
        @Query("limit") limit: Int = 50
    ): HistoryResponse

    // ==================== 会话管理 ====================

    @GET("/api/session")
    suspend fun getSessions(): List<SessionDto>

    @POST("/api/session")
    suspend fun createSession(@Body request: CreateSessionRequest): SessionDto

    @DELETE("/api/session/{sessionId}")
    suspend fun deleteSession(@Path("sessionId") sessionId: String)

    @PATCH("/api/session/{sessionId}")
    suspend fun updateSession(
        @Path("sessionId") sessionId: String,
        @Body request: Map<String, String>
    ): SessionDto
}
