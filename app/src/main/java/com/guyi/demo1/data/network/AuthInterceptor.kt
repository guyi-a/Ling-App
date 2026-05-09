package com.guyi.demo1.data.network

import com.guyi.demo1.data.api.RefreshTokenRequest
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response

class AuthInterceptor(
    private val tokenProvider: () -> String?,
    private val refreshTokenProvider: () -> String?,
    private val onTokenRefreshed: (accessToken: String, refreshToken: String) -> Unit,
    private val onRefreshFailed: () -> Unit
) : Interceptor {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    @Volatile
    private var isRefreshing = false

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val token = tokenProvider()
        val request = if (token != null) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }

        val response = chain.proceed(request)

        // 如果不是 401 或者是 refresh 请求本身，直接返回
        if (response.code != 401 || originalRequest.url.encodedPath.contains("/auth/refresh")) {
            return response
        }

        // 尝试 refresh token
        val refreshToken = refreshTokenProvider() ?: run {
            onRefreshFailed()
            return response
        }

        synchronized(this) {
            if (isRefreshing) return response
            isRefreshing = true
        }

        try {
            val refreshBody = json.encodeToString(
                RefreshTokenRequest.serializer(),
                RefreshTokenRequest(refreshToken)
            )
            val refreshRequest = Request.Builder()
                .url("${ApiConfig.BASE_URL}/api/auth/refresh")
                .post(refreshBody.toRequestBody("application/json".toMediaType()))
                .build()

            val refreshResponse = chain.proceed(refreshRequest)
            if (refreshResponse.isSuccessful) {
                val body = refreshResponse.body?.string() ?: ""
                refreshResponse.close()
                val tokenResponse = json.decodeFromString<TokenRefreshResult>(body)
                onTokenRefreshed(tokenResponse.access_token, tokenResponse.refresh_token)

                // 用新 token 重发原始请求
                response.close()
                val newRequest = originalRequest.newBuilder()
                    .header("Authorization", "Bearer ${tokenResponse.access_token}")
                    .build()
                return chain.proceed(newRequest)
            } else {
                refreshResponse.close()
                onRefreshFailed()
            }
        } catch (_: Exception) {
            onRefreshFailed()
        } finally {
            synchronized(this) { isRefreshing = false }
        }

        return response
    }
}

@kotlinx.serialization.Serializable
private data class TokenRefreshResult(
    val access_token: String,
    val refresh_token: String
)
