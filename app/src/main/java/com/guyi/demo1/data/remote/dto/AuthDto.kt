package com.guyi.demo1.data.remote.dto

import com.google.gson.annotations.SerializedName

// 登录请求
data class LoginRequest(
    val username: String,
    val password: String
)

// 注册请求
data class RegisterRequest(
    val username: String,
    val password: String
)

// 认证响应
data class AuthResponse(
    @SerializedName("access_token")
    val accessToken: String,

    @SerializedName("token_type")
    val tokenType: String = "bearer",

    @SerializedName("user_id")
    val userId: String,

    val username: String
)
