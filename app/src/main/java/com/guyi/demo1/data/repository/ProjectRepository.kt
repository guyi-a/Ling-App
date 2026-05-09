package com.guyi.demo1.data.repository

import com.guyi.demo1.data.api.CreateProjectRequest
import com.guyi.demo1.data.api.ProjectApi
import com.guyi.demo1.data.api.UpdateProjectRequest
import com.guyi.demo1.data.model.AdhocSession
import com.guyi.demo1.data.model.Project
import com.guyi.demo1.data.model.ProjectDetail

class ProjectRepository(
    private val projectApi: ProjectApi
) {

    suspend fun getProjects(): Result<List<Project>> {
        return try {
            val projects = projectApi.getProjects()
            Result.success(projects)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAdhocSessions(): Result<List<AdhocSession>> {
        return try {
            val sessions = projectApi.getAdhocSessions()
            Result.success(sessions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProjectDetail(projectId: Int): Result<ProjectDetail> {
        return try {
            val detail = projectApi.getProjectDetail(projectId)
            Result.success(detail)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createProject(title: String? = null): Result<Project> {
        return try {
            val project = projectApi.createProject(CreateProjectRequest(title))
            Result.success(project)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProject(
        projectId: Int,
        title: String? = null,
        description: String? = null,
        icon: String? = null
    ): Result<Project> {
        return try {
            val project = projectApi.updateProject(
                projectId,
                UpdateProjectRequest(title, description, icon)
            )
            Result.success(project)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteProject(projectId: Int): Result<Unit> {
        return try {
            projectApi.deleteProject(projectId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
