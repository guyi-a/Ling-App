package com.guyi.demo1.data.model

import kotlinx.serialization.Serializable

/**
 * 统一 API 响应包装
 */
@Serializable
data class ApiResponse<T>(
    val success: Boolean = true,
    val data: T? = null,
    val message: String? = null
)

/**
 * 通用错误响应
 */
@Serializable
data class ApiError(
    val detail: String
)
