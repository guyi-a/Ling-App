package com.guyi.demo1.data.repository

import com.guyi.demo1.data.local.TokenManager
import com.guyi.demo1.data.remote.api.LingAgentApi
import com.guyi.demo1.data.remote.dto.LoginRequest
import com.guyi.demo1.data.remote.dto.RegisterRequest
import com.guyi.demo1.domain.model.User
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val api: LingAgentApi,
    private val tokenManager: TokenManager
) {

    suspend fun login(username: String, password: String): Result<User> {
        return try {
            val response = api.login(LoginRequest(username, password))

            // 保存 token 和用户信息
            tokenManager.saveToken(response.accessToken)
            tokenManager.saveUserInfo(response.userId, response.username)

            Result.success(
                User(
                    userId = response.userId,
                    username = response.username,
                    token = response.accessToken
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(username: String, password: String): Result<User> {
        return try {
            val response = api.register(RegisterRequest(username, password))

            // 注册成功后自动保存 token
            tokenManager.saveToken(response.accessToken)
            tokenManager.saveUserInfo(response.userId, response.username)

            Result.success(
                User(
                    userId = response.userId,
                    username = response.username,
                    token = response.accessToken
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout() {
        tokenManager.clearToken()
    }

    fun isLoggedIn() = tokenManager.isLoggedIn()
}
