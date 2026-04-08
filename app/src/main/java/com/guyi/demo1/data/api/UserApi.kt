package com.guyi.demo1.data.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

@Serializable
data class UserResponse(
    val id: Int,
    @SerialName("user_id") val userId: String,
    val username: String?,
    @SerialName("device_id") val deviceId: String,
    @SerialName("device_model") val deviceModel: String? = null,
    val preferences: String? = null,
    val avatar: String? = null,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("last_active_at") val lastActiveAt: String,
    @SerialName("is_active") val isActive: Boolean
)

@Serializable
data class AvatarResponse(
    val status: String,
    val avatar: String
)

@Serializable
data class UserUpdateRequest(
    val username: String? = null,
    @SerialName("device_model") val deviceModel: String? = null,
    val preferences: String? = null
)

interface UserApi {

    @GET("/api/users/{user_id}")
    suspend fun getUser(@Path("user_id") userId: String): UserResponse

    @PUT("/api/users/{user_id}")
    suspend fun updateUser(
        @Path("user_id") userId: String,
        @Body request: UserUpdateRequest
    ): UserResponse

    @Multipart
    @POST("/api/users/{user_id}/avatar")
    suspend fun uploadAvatar(
        @Path("user_id") userId: String,
        @Part file: MultipartBody.Part
    ): AvatarResponse
}
