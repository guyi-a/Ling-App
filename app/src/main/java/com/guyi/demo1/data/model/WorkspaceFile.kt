package com.guyi.demo1.data.model

import kotlinx.serialization.Serializable

/**
 * 工作区文件
 */
@Serializable
data class WorkspaceFile(
    val name: String,
    val path: String,
    val size: Long,
    val type: String,  // "image", "pdf", "csv", "file"
    val createdAt: String,
    val folder: String  // "uploads" | "outputs"
)

/**
 * 文件上传响应
 */
@Serializable
data class FileUploadResponse(
    val filename: String,
    val path: String,
    val size: Long
)

/**
 * 文件列表响应
 */
@Serializable
data class FileListResponse(
    val uploads: List<WorkspaceFile>,
    val outputs: List<WorkspaceFile>
)
