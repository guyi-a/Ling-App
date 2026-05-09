package com.guyi.demo1.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 项目信息
 */
@Serializable
data class Project(
    val id: Int,
    val slug: String? = null,
    val title: String? = null,
    val description: String? = null,
    val icon: String? = null,
    @SerialName("user_id")
    val userId: String,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String? = null,
    @SerialName("session_count")
    val sessionCount: Int = 0,
    @SerialName("last_active_at")
    val lastActiveAt: String? = null
)

/**
 * 项目详情（含会话列表）
 */
@Serializable
data class ProjectDetail(
    val id: Int,
    val slug: String? = null,
    val title: String? = null,
    val description: String? = null,
    val icon: String? = null,
    @SerialName("user_id")
    val userId: String,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String? = null,
    @SerialName("session_count")
    val sessionCount: Int = 0,
    @SerialName("last_active_at")
    val lastActiveAt: String? = null,
    val sessions: List<SessionBrief> = emptyList()
)

/**
 * 会话简要信息（用于项目详情）
 */
@Serializable
data class SessionBrief(
    @SerialName("session_id")
    val sessionId: String,
    val title: String? = null,
    @SerialName("updated_at")
    val updatedAt: String,
    @SerialName("is_pinned")
    val isPinned: Boolean = false
)

/**
 * 临时对话（adhoc session）
 */
@Serializable
data class AdhocSession(
    @SerialName("session_id")
    val sessionId: String,
    @SerialName("project_id")
    val projectId: Int,
    val title: String? = null,
    @SerialName("updated_at")
    val updatedAt: String
)
