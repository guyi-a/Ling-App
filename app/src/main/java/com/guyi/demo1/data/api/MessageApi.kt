package com.guyi.demo1.data.api

import com.guyi.demo1.data.repository.ConversationHistory
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * 删除消息响应
 */
@Serializable
data class DeleteMessageResponse(
    val status: String,
    @SerialName("deleted_count")
    val deletedCount: Int,
    val message: String
)

/**
 * 消息 API 接口
 */
interface MessageApi {

    /**
     * 获取会话的对话历史
     */
    @GET("/api/messages/session/{session_id}/history")
    suspend fun getConversationHistory(
        @Path("session_id") sessionId: String,
        @Query("limit") limit: Int = 50
    ): ConversationHistory

    /**
     * 删除单条消息
     */
    @DELETE("/api/messages/{message_id}")
    suspend fun deleteMessage(@Path("message_id") messageId: String)

    /**
     * 删除该消息及之后的所有消息
     */
    @DELETE("/api/messages/session/{session_id}/after/{message_id}")
    suspend fun deleteMessagesAfter(
        @Path("session_id") sessionId: String,
        @Path("message_id") messageId: String,
        @Query("include_self") includeSelf: Boolean = true
    ): DeleteMessageResponse
}
