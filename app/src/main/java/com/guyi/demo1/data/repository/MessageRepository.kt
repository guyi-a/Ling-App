package com.guyi.demo1.data.repository

import com.guyi.demo1.data.api.MessageApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 消息历史响应
 */
@Serializable
data class ConversationHistory(
    @SerialName("session_id")
    val sessionId: String,
    val messages: List<HistoryMessage>
)

/**
 * 历史消息
 */
@Serializable
data class HistoryMessage(
    val role: String,
    val content: String,
    @SerialName("message_id")
    val messageId: String,
    @SerialName("extra_data")
    val extraData: String? = null
)

/**
 * 消息数据仓库
 */
class MessageRepository(
    private val messageApi: MessageApi
) {

    /**
     * 获取会话的对话历史
     */
    suspend fun getConversationHistory(
        sessionId: String,
        limit: Int = 50
    ): Result<ConversationHistory> {
        return try {
            val response = messageApi.getConversationHistory(sessionId, limit)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 删除单条消息
     */
    suspend fun deleteMessage(messageId: String): Result<Unit> {
        return try {
            messageApi.deleteMessage(messageId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 删除消息及之后的所有消息
     */
    suspend fun deleteMessagesAfter(
        sessionId: String,
        messageId: String,
        includeSelf: Boolean = true
    ): Result<Int> {
        return try {
            val response = messageApi.deleteMessagesAfter(sessionId, messageId, includeSelf)
            Result.success(response.deletedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
