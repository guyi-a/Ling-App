package com.guyi.demo1.data.repository

import com.guyi.demo1.data.api.CreateSessionRequest
import com.guyi.demo1.data.api.SessionApi
import com.guyi.demo1.data.api.UpdateSessionRequest
import com.guyi.demo1.data.model.Session

/**
 * 会话数据仓库
 */
class SessionRepository(
    private val sessionApi: SessionApi
) {

    /**
     * 获取所有会话
     */
    suspend fun getAllSessions(
        skip: Int = 0,
        limit: Int = 100,
        isActive: Boolean? = null
    ): Result<List<Session>> {
        return try {
            val sessions = sessionApi.getSessions(skip, limit, isActive)
            Result.success(sessions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 获取单个会话
     */
    suspend fun getSession(sessionId: String): Result<Session> {
        return try {
            val session = sessionApi.getSession(sessionId)
            Result.success(session)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 创建新会话
     */
    suspend fun createSession(title: String): Result<Session> {
        return try {
            val session = sessionApi.createSession(CreateSessionRequest(title))
            Result.success(session)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 更新会话（重命名）
     */
    suspend fun updateSession(
        sessionId: String,
        title: String? = null,
        isActive: Boolean? = null
    ): Result<Session> {
        return try {
            val request = UpdateSessionRequest(title, isActive)
            val session = sessionApi.updateSession(sessionId, request)
            Result.success(session)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 删除会话
     */
    suspend fun deleteSession(sessionId: String, hardDelete: Boolean = false): Result<Unit> {
        return try {
            sessionApi.deleteSession(sessionId, hardDelete)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
