package com.guyi.demo1.data.repository

import android.content.Context
import android.net.Uri
import com.guyi.demo1.data.api.WorkspaceApi
import com.guyi.demo1.data.model.FileDeleteResponse
import com.guyi.demo1.data.model.FileListResponse
import com.guyi.demo1.data.model.FileUploadResponse
import com.guyi.demo1.data.model.TreeResponse
import com.guyi.demo1.data.model.WorkspaceFile
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody

class WorkspaceRepository(
    private val workspaceApi: WorkspaceApi,
    private val context: Context
) {

    /**
     * 获取文件列表
     */
    suspend fun getFiles(sessionId: String, folder: String? = null): Result<FileListResponse> {
        return try {
            val response = workspaceApi.getFiles(sessionId, folder)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 上传文件（从 Uri）
     */
    suspend fun uploadFile(sessionId: String, uri: Uri): Result<FileUploadResponse> {
        return try {
            val contentResolver = context.contentResolver
            val mimeType = contentResolver.getType(uri) ?: "application/octet-stream"
            val fileName = getFileName(uri) ?: "file_${System.currentTimeMillis()}"

            val inputStream = contentResolver.openInputStream(uri)
                ?: return Result.failure(Exception("无法读取文件"))
            val bytes = inputStream.use { it.readBytes() }

            val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData("file", fileName, requestBody)

            val response = workspaceApi.uploadFile(sessionId, part)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 下载文件
     */
    suspend fun downloadFile(sessionId: String, folder: String, filename: String): Result<ResponseBody> {
        return try {
            val response = workspaceApi.downloadFile(sessionId, folder, filename)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 删除文件
     */
    suspend fun deleteFile(sessionId: String, folder: String, filename: String): Result<FileDeleteResponse> {
        return try {
            val response = workspaceApi.deleteFile(sessionId, folder, filename)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTree(sessionId: String, path: String = "."): Result<TreeResponse> {
        return try {
            val response = workspaceApi.getTree(sessionId, path)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun downloadByPath(sessionId: String, path: String): Result<ResponseBody> {
        return try {
            val response = workspaceApi.downloadByPath(sessionId, path)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteByPath(sessionId: String, path: String): Result<FileDeleteResponse> {
        return try {
            val response = workspaceApi.deleteByPath(sessionId, path)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 从 Uri 获取文件名
     */
    private fun getFileName(uri: Uri): String? {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        return cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0) it.getString(nameIndex) else null
            } else null
        }
    }
}
