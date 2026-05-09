package com.guyi.demo1.data

import android.content.Context
import com.guyi.demo1.data.api.AuthApi
import com.guyi.demo1.data.api.ChatApi
import com.guyi.demo1.data.api.DevApi
import com.guyi.demo1.data.api.MessageApi
import com.guyi.demo1.data.api.ProjectApi
import com.guyi.demo1.data.api.SessionApi
import com.guyi.demo1.data.api.UserApi
import com.guyi.demo1.data.api.WorkspaceApi
import com.guyi.demo1.data.local.ThemeManager
import com.guyi.demo1.data.local.TokenManager
import com.guyi.demo1.data.network.RetrofitClient
import com.guyi.demo1.data.network.SSEManager
import com.guyi.demo1.data.repository.AuthRepository
import com.guyi.demo1.data.repository.DevRepository
import com.guyi.demo1.data.repository.ProjectRepository
import com.guyi.demo1.data.repository.SessionRepository
import com.guyi.demo1.data.repository.MessageRepository
import com.guyi.demo1.data.repository.WorkspaceRepository
import kotlinx.coroutines.runBlocking

class AppContainer(context: Context) {

    val tokenManager = TokenManager(context)
    val themeManager = ThemeManager(context)

    @Volatile
    var onSessionExpired: (() -> Unit)? = null

    private val okHttpClient = RetrofitClient.createOkHttpClient(
        tokenProvider = { runBlocking { tokenManager.getToken() } },
        refreshTokenProvider = { runBlocking { tokenManager.getRefreshToken() } },
        onTokenRefreshed = { access, refresh ->
            runBlocking {
                tokenManager.saveToken(access)
                tokenManager.saveRefreshToken(refresh)
            }
        },
        onRefreshFailed = { onSessionExpired?.invoke() }
    )

    private val retrofit = RetrofitClient.createRetrofit(okHttpClient)

    val authApi: AuthApi = RetrofitClient.createService(retrofit)
    val sessionApi: SessionApi = RetrofitClient.createService(retrofit)
    val projectApi: ProjectApi = RetrofitClient.createService(retrofit)
    val chatApi: ChatApi = RetrofitClient.createService(retrofit)
    val messageApi: MessageApi = RetrofitClient.createService(retrofit)
    val userApi: UserApi = RetrofitClient.createService(retrofit)
    val workspaceApi: WorkspaceApi = RetrofitClient.createService(retrofit)
    val devApi: DevApi = RetrofitClient.createService(retrofit)

    val sseManager = SSEManager { runBlocking { tokenManager.getToken() } }

    val authRepository = AuthRepository(context, authApi, tokenManager)
    val sessionRepository = SessionRepository(sessionApi)
    val projectRepository = ProjectRepository(projectApi)
    val messageRepository = MessageRepository(messageApi)
    val workspaceRepository = WorkspaceRepository(workspaceApi, context)
    val devRepository = DevRepository(devApi)
}
