package com.guyi.demo1.data

import android.content.Context
import com.guyi.demo1.data.api.AuthApi
import com.guyi.demo1.data.api.ChatApi
import com.guyi.demo1.data.api.MessageApi
import com.guyi.demo1.data.api.SessionApi
import com.guyi.demo1.data.api.WorkspaceApi
import com.guyi.demo1.data.local.TokenManager
import com.guyi.demo1.data.network.RetrofitClient
import com.guyi.demo1.data.network.SSEManager
import com.guyi.demo1.data.repository.AuthRepository
import com.guyi.demo1.data.repository.SessionRepository
import com.guyi.demo1.data.repository.MessageRepository
import kotlinx.coroutines.runBlocking

/**
 * 应用依赖容器
 * 简单的手动依赖注入（DI）容器
 */
class AppContainer(context: Context) {

    // Token 管理器
    val tokenManager = TokenManager(context)

    // OkHttp 客户端
    private val okHttpClient = RetrofitClient.createOkHttpClient {
        runBlocking { tokenManager.getToken() }
    }

    // Retrofit 实例
    private val retrofit = RetrofitClient.createRetrofit(okHttpClient)

    // API 服务
    val authApi: AuthApi = RetrofitClient.createService(retrofit)
    val sessionApi: SessionApi = RetrofitClient.createService(retrofit)
    val chatApi: ChatApi = RetrofitClient.createService(retrofit)
    val messageApi: MessageApi = RetrofitClient.createService(retrofit)
    val workspaceApi: WorkspaceApi = RetrofitClient.createService(retrofit)

    // SSE 管理器
    val sseManager = SSEManager { runBlocking { tokenManager.getToken() } }

    // Repository
    val authRepository = AuthRepository(context, authApi, tokenManager)
    val sessionRepository = SessionRepository(sessionApi)
    val messageRepository = MessageRepository(messageApi)
}
