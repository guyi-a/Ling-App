package com.guyi.demo1.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.guyi.demo1.data.repository.AuthRepository
import com.guyi.demo1.ui.screen.auth.login.LoginScreen
import com.guyi.demo1.ui.screen.chat.ChatScreen
import com.guyi.demo1.ui.screen.session.SessionListScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    authRepository: AuthRepository
) {
    // 暂时跳过登录验证，直接进入会话列表
    // TODO: 后续恢复登录验证
    val startDestination = Screen.SessionList.route

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // 登录页面
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.SessionList.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        // 注册页面（暂时跳转回登录）
        composable(Screen.Register.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.SessionList.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = { }
            )
        }

        // 会话列表页面
        composable(Screen.SessionList.route) {
            SessionListScreen(
                onSessionClick = { sessionId ->
                    navController.navigate("chat/$sessionId")
                },
                onNewSessionClick = {
                    navController.navigate("chat/new")
                },
                onSettingsClick = {
                    // TODO: 跳转到设置页面
                }
            )
        }

        // 聊天页面
        composable("chat/{sessionId}") {
            ChatScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}
