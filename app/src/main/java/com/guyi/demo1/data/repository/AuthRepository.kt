package com.guyi.demo1.data.repository

import android.content.Context
import com.guyi.demo1.data.api.AuthApi
import com.guyi.demo1.data.local.TokenManager
import com.guyi.demo1.data.model.LoginRequest
import com.guyi.demo1.data.model.RegisterRequest
import com.guyi.demo1.data.model.User
import com.guyi.demo1.utils.DeviceUtils

/**
 * 认证数据仓库
 */
class AuthRepository(
    private val context: Context,
    private val authApi: AuthApi,
    private val tokenManager: TokenManager
) {

    /**
     * 用户登录
     */
    suspend fun login(username: String, password: String): Result<User> {
        return try {
            val response = authApi.login(LoginRequest(username, password))
            // 保存 Token
            tokenManager.saveToken(response.accessToken)
            // 将响应转换为 User 对象
            val user = User(
                userId = response.userId,
                username = response.username,
                createdAt = null
            )
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(parseException(e))
        }
    }

    /**
     * 用户注册（不自动登录）
     */
    suspend fun register(username: String, password: String): Result<User> {
        return try {
            val deviceId = DeviceUtils.getDeviceId(context)
            val deviceModel = DeviceUtils.getDeviceModel()

            val response = authApi.register(
                RegisterRequest(
                    username = username,
                    password = password,
                    deviceId = deviceId,
                    deviceModel = deviceModel
                )
            )
            // 注册成功，但不保存 Token（需要用户再次登录）
            val user = User(
                userId = response.userId,
                username = response.username,
                createdAt = null
            )
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(parseException(e))
        }
    }

    /**
     * 获取当前用户信息
     */
    suspend fun getCurrentUser(): Result<User> {
        return try {
            val user = authApi.getCurrentUser()
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 登出
     */
    suspend fun logout() {
        tokenManager.clearToken()
    }

    /**
     * 检查是否已登录
     */
    suspend fun isLoggedIn(): Boolean {
        return tokenManager.hasToken()
    }

    /**
     * 解析异常为友好的错误消息
     */
    private fun parseException(e: Exception): Exception {
        val message = when {
            e is retrofit2.HttpException -> {
                when (e.code()) {
                    401 -> "用户名或密码错误"
                    400 -> "请求参数错误"
                    404 -> "服务未找到"
                    422 -> "数据验证失败，请检查输入格式"
                    500 -> "服务器错误，请稍后重试"
                    else -> "网络请求失败：${e.code()}"
                }
            }
            e is java.net.UnknownHostException -> "无法连接到服务器，请检查网络"
            e is java.net.SocketTimeoutException -> "连接超时，请重试"
            e is java.net.ConnectException -> "无法连接到服务器"
            else -> e.message ?: "未知错误"
        }
        return Exception(message)
    }
}
