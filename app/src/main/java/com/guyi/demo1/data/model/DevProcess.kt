package com.guyi.demo1.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DevProcess(
    val name: String,
    val status: String,
    val port: Int? = null,
    val pid: Int? = null,
    /** 后端返回的是数组，如 [".venv/bin/python", "-m", "uvicorn"] */
    val command: List<String>? = null,
    @SerialName("session_id")
    val sessionId: String? = null,
    @SerialName("session_title")
    val sessionTitle: String? = null
)

@Serializable
data class ProcessListResponse(
    @SerialName("session_id")
    val sessionId: String,
    val processes: List<DevProcess>
)

/** /api/dev/all 的返回（所有 session 的进程） */
@Serializable
data class AllProcessesResponse(
    val processes: List<DevProcess>
)

@Serializable
data class GenericResponse(
    val status: String,
    val message: String? = null
)

@Serializable
data class ProcessActionResponse(
    val status: String,
    val process: DevProcess? = null
)
