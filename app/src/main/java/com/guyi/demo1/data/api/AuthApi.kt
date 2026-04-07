package com.guyi.demo1.data.api

import com.guyi.demo1.data.model.LoginRequest
import com.guyi.demo1.data.model.LoginResponse
import com.guyi.demo1.data.model.RegisterRequest
import com.guyi.demo1.data.model.User
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * 认证 API 接口
 */
interface AuthApi {

    /**
     * 用户登录
     */
    @POST("/api/auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    /**
     * 用户注册
     */
    @POST("/api/auth/register")
    suspend fun register(@Body request: RegisterRequest): LoginResponse

    /**
     * 获取当前用户信息
     */
    @GET("/api/users/me")
    suspend fun getCurrentUser(): User
}
