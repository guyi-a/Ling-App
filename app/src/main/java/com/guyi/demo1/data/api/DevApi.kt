package com.guyi.demo1.data.api

import com.guyi.demo1.data.model.AllProcessesResponse
import com.guyi.demo1.data.model.GenericResponse
import com.guyi.demo1.data.model.ProcessActionResponse
import com.guyi.demo1.data.model.ProcessListResponse
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface DevApi {
    /** 列出当前用户所有 session 的后台进程（按 workspace 去重） */
    @GET("/api/dev/all")
    suspend fun listAllProcesses(): AllProcessesResponse

    @GET("/api/dev/{session_id}/processes")
    suspend fun getProcesses(
        @Path("session_id") sessionId: String
    ): ProcessListResponse

    @POST("/api/dev/{session_id}/stop/{name}")
    suspend fun stopProcess(
        @Path("session_id") sessionId: String,
        @Path("name") name: String
    ): GenericResponse

    @POST("/api/dev/{session_id}/restart/{name}")
    suspend fun restartProcess(
        @Path("session_id") sessionId: String,
        @Path("name") name: String
    ): ProcessActionResponse
}
