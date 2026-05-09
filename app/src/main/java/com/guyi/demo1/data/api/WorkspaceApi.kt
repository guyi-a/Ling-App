package com.guyi.demo1.data.api

import com.guyi.demo1.data.model.FileDeleteResponse
import com.guyi.demo1.data.model.FileListResponse
import com.guyi.demo1.data.model.FileUploadResponse
import com.guyi.demo1.data.model.TreeResponse
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming

/**
 * 工作区 API 接口
 */
interface WorkspaceApi {

    /**
     * 上传文件
     */
    @Multipart
    @POST("/api/workspace/{session_id}/upload")
    suspend fun uploadFile(
        @Path("session_id") sessionId: String,
        @Part file: MultipartBody.Part
    ): FileUploadResponse

    /**
     * 获取工作区文件列表
     */
    @GET("/api/workspace/{session_id}/files")
    suspend fun getFiles(
        @Path("session_id") sessionId: String,
        @Query("folder") folder: String? = null
    ): FileListResponse

    /**
     * 下载文件
     */
    @Streaming
    @GET("/api/workspace/{session_id}/files/{folder}/{filename}")
    suspend fun downloadFile(
        @Path("session_id") sessionId: String,
        @Path("folder") folder: String,
        @Path("filename") filename: String
    ): ResponseBody

    /**
     * 删除文件
     */
    @DELETE("/api/workspace/{session_id}/files/{folder}/{filename}")
    suspend fun deleteFile(
        @Path("session_id") sessionId: String,
        @Path("folder") folder: String,
        @Path("filename") filename: String
    ): FileDeleteResponse

    /**
     * 获取目录树
     */
    @GET("/api/workspace/{session_id}/tree")
    suspend fun getTree(
        @Path("session_id") sessionId: String,
        @Query("path") path: String = "."
    ): TreeResponse

    /**
     * 通过路径下载文件
     */
    @Streaming
    @GET("/api/workspace/{session_id}/download")
    suspend fun downloadByPath(
        @Path("session_id") sessionId: String,
        @Query("path") path: String
    ): ResponseBody

    /**
     * 通过路径删除文件
     */
    @DELETE("/api/workspace/{session_id}/delete")
    suspend fun deleteByPath(
        @Path("session_id") sessionId: String,
        @Query("path") path: String
    ): FileDeleteResponse
}
