package com.guyi.demo1.data.model

import kotlinx.serialization.Serializable

/**
 * 用户信息
 */
@Serializable
data class User(
    val userId: String,
    val username: String,
    val createdAt: String? = null
)
