package com.guyi.demo1.data.api

import com.guyi.demo1.data.model.Session
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * 创建会话请求
 */
@Serializable
data class CreateSessionRequest(
    val title: String
)

/**
 * 更新会话请求
 */
@Serializable
data class UpdateSessionRequest(
    val title: String? = null,
    val is_active: Boolean? = null
)

/**
 * 会话 API 接口
 */
interface SessionApi {

    /**
     * 获取用户的所有会话
     */
    @GET("/api/sessions/")
    suspend fun getSessions(
        @Query("skip") skip: Int = 0,
        @Query("limit") limit: Int = 100,
        @Query("is_active") isActive: Boolean? = null
    ): List<Session>

    /**
     * 获取单个会话
     */
    @GET("/api/sessions/{session_id}")
    suspend fun getSession(@Path("session_id") sessionId: String): Session

    /**
     * 创建新会话
     */
    @POST("/api/sessions/")
    suspend fun createSession(@Body request: CreateSessionRequest): Session

    /**
     * 更新会话
     */
    @PUT("/api/sessions/{session_id}")
    suspend fun updateSession(
        @Path("session_id") sessionId: String,
        @Body request: UpdateSessionRequest
    ): Session

    /**
     * 删除会话
     */
    @DELETE("/api/sessions/{session_id}")
    suspend fun deleteSession(
        @Path("session_id") sessionId: String,
        @Query("hard_delete") hardDelete: Boolean = false
    )
}
