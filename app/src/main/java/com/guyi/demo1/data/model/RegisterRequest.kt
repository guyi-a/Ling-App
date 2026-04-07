package com.guyi.demo1.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 注册请求
 */
@Serializable
data class RegisterRequest(
    val username: String,
    val password: String,
    @SerialName("device_id")
    val deviceId: String,
    @SerialName("device_model")
    val deviceModel: String? = null
)
