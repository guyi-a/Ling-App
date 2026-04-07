package com.guyi.demo1.data.network

import okhttp3.Interceptor
import okhttp3.Response

/**
 * JWT Token 拦截器
 * 自动为所有请求添加 Authorization header
 */
class AuthInterceptor(
    private val tokenProvider: () -> String?
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // 获取 Token
        val token = tokenProvider()

        // 如果 Token 存在，添加到请求头
        val newRequest = if (token != null) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }

        return chain.proceed(newRequest)
    }
}
