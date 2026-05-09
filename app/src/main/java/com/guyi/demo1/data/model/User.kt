package com.guyi.demo1.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 用户信息
 */
@Serializable
data class User(
    @SerialName("user_id")
    val userId: String,
    val username: String,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("is_active")
    val isActive: Boolean = true
)
