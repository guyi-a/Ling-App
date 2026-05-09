package com.guyi.demo1.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 工作区文件
 */
@Serializable
data class WorkspaceFile(
    val name: String,
    val path: String,
    val folder: String,  // "uploads" | "outputs"
    val size: Long,
    @SerialName("modified_at")
    val modifiedAt: Double = 0.0  // Unix timestamp from backend
)

/**
 * 文件列表响应（匹配后端 {session_id, files: [...]}）
 */
@Serializable
data class FileListResponse(
    @SerialName("session_id")
    val sessionId: String,
    val files: List<WorkspaceFile>
)

/**
 * 文件上传响应
 */
@Serializable
data class FileUploadResponse(
    val filename: String,
    val path: String,
    val size: Long,
    @SerialName("content_type")
    val contentType: String? = null
)

/**
 * 文件删除响应
 */
@Serializable
data class FileDeleteResponse(
    val status: String,
    val message: String
)

@Serializable
data class TreeEntry(
    val name: String,
    val path: String,
    val type: String,
    val size: Long = 0,
    val children: List<TreeEntry> = emptyList()
)

@Serializable
data class TreeResponse(
    @SerialName("session_id")
    val sessionId: String,
    val root: String,
    val entries: List<TreeEntry>
)
