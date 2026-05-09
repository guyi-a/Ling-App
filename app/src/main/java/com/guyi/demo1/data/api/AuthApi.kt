package com.guyi.demo1.data.api

import com.guyi.demo1.data.model.LoginResponse
import com.guyi.demo1.data.model.LoginRequest
import com.guyi.demo1.data.model.RegisterRequest
import com.guyi.demo1.data.model.User
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApi {

    @POST("/api/auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("/api/auth/register")
    suspend fun register(@Body request: RegisterRequest): LoginResponse

    @POST("/api/auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): LoginResponse

    @GET("/api/auth/me")
    suspend fun getCurrentUser(): User

    @POST("/api/auth/change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): ChangePasswordResponse
}

@Serializable
data class ChangePasswordRequest(
    val old_password: String,
    val new_password: String
)

@Serializable
data class ChangePasswordResponse(
    val status: String,
    val message: String = ""
)

@Serializable
data class RefreshTokenRequest(
    @SerialName("refresh_token")
    val refreshToken: String
)
