package com.guyi.demo1.data.model

import kotlinx.serialization.Serializable

/**
 * 登录请求
 */
@Serializable
data class LoginRequest(
    val username: String,
    val password: String
)
