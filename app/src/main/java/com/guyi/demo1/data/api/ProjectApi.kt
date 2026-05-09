package com.guyi.demo1.data.api

import com.guyi.demo1.data.model.AdhocSession
import com.guyi.demo1.data.model.Project
import com.guyi.demo1.data.model.ProjectDetail
import kotlinx.serialization.Serializable
import retrofit2.http.*

interface ProjectApi {

    @GET("/api/projects/")
    suspend fun getProjects(): List<Project>

    @GET("/api/projects/adhoc")
    suspend fun getAdhocSessions(): List<AdhocSession>

    @GET("/api/projects/{project_id}")
    suspend fun getProjectDetail(@Path("project_id") projectId: Int): ProjectDetail

    @POST("/api/projects/")
    suspend fun createProject(@Body request: CreateProjectRequest): Project

    @PATCH("/api/projects/{project_id}")
    suspend fun updateProject(
        @Path("project_id") projectId: Int,
        @Body request: UpdateProjectRequest
    ): Project

    @DELETE("/api/projects/{project_id}")
    suspend fun deleteProject(@Path("project_id") projectId: Int)
}

@Serializable
data class CreateProjectRequest(
    val title: String? = null
)

@Serializable
data class UpdateProjectRequest(
    val title: String? = null,
    val description: String? = null,
    val icon: String? = null
)
