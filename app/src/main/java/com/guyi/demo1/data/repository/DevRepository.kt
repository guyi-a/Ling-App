package com.guyi.demo1.data.repository

import com.guyi.demo1.data.api.DevApi
import com.guyi.demo1.data.model.DevProcess

class DevRepository(private val devApi: DevApi) {

    suspend fun getRunningApps(sessionId: String): Result<List<DevProcess>> {
        return try {
            val response = devApi.getProcesses(sessionId)
            val running = response.processes.filter { it.status == "running" && it.port != null }
            Result.success(running)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllProcesses(sessionId: String): Result<List<DevProcess>> {
        return try {
            val response = devApi.getProcesses(sessionId)
            Result.success(response.processes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** 列出当前用户所有 session 的进程（全局） */
    suspend fun listAllProcesses(): Result<List<DevProcess>> {
        return try {
            val response = devApi.listAllProcesses()
            Result.success(response.processes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun stopProcess(sessionId: String, name: String): Result<Unit> {
        return try {
            devApi.stopProcess(sessionId, name)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun restartProcess(sessionId: String, name: String): Result<DevProcess?> {
        return try {
            val response = devApi.restartProcess(sessionId, name)
            Result.success(response.process)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
