package com.guyi.demo1.ui.navigation

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.guyi.demo1.ui.components.DrawerContent
import com.guyi.demo1.ui.screen.auth.LoginScreen
import com.guyi.demo1.ui.screen.auth.RegisterScreen
import com.guyi.demo1.ui.screen.chat.ChatScreen
import com.guyi.demo1.ui.screen.home.WelcomeScreen
import com.guyi.demo1.ui.screen.profile.ProfileScreen
import com.guyi.demo1.ui.screen.settings.SettingsScreen
import com.guyi.demo1.ui.screen.workspace.WorkspaceScreen
import kotlinx.coroutines.launch

// 路由定义
sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object Welcome : Screen("welcome")
    data object Chat : Screen("chat")
    data object Workspace : Screen("workspace")
    data object Settings : Screen("settings")
    data object Profile : Screen("profile")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavGraph(
    navController: NavHostController
) {
    // 登录状态管理
    var isLoggedIn by remember { mutableStateOf(false) }

    // 抽屉状态
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // 根据登录状态决定起始页面
    val startDestination = if (isLoggedIn) Screen.Welcome.route else Screen.Login.route

    // 侧边抽屉
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                onSessionClick = { sessionId ->
                    scope.launch {
                        drawerState.close()
                    }
                    // 跳转到对应的历史会话
                    navController.navigate("chat/$sessionId")
                },
                onNewChatClick = {
                    scope.launch {
                        drawerState.close()
                    }
                    // 返回欢迎页或新建对话
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                },
                onSettingsClick = {
                    scope.launch {
                        drawerState.close()
                    }
                    navController.navigate(Screen.Settings.route)
                }
            )
        },
        gesturesEnabled = isLoggedIn // 只有登录后才能打开抽屉
    ) {
        NavHost(
            navController = navController,
            startDestination = startDestination
        ) {
            // 登录页面
            composable(Screen.Login.route) {
                LoginScreen(
                    onLoginSuccess = {
                        isLoggedIn = true
                        navController.navigate(Screen.Welcome.route) {
                            popUpTo(Screen.Login.route) {
                                inclusive = true
                            }
                        }
                    },
                    onRegisterClick = {
                        navController.navigate(Screen.Register.route)
                    }
                )
            }

            // 注册页面
            composable(Screen.Register.route) {
                RegisterScreen(
                    onRegisterSuccess = {
                        isLoggedIn = true
                        navController.navigate(Screen.Welcome.route) {
                            popUpTo(Screen.Login.route) {
                                inclusive = true
                            }
                        }
                    },
                    onBackToLogin = {
                        navController.popBackStack()
                    }
                )
            }

            // 欢迎页面
            composable(Screen.Welcome.route) {
                WelcomeScreen(
                    onStartChat = {
                        // 开始新对话
                        navController.navigate(Screen.Chat.route)
                    },
                    onMenuClick = {
                        // 打开侧边抽屉
                        scope.launch {
                            drawerState.open()
                        }
                    },
                    onExampleClick = { example ->
                        // 点击示例问题，带着问题进入聊天页
                        navController.navigate("${Screen.Chat.route}?message=$example")
                    }
                )
            }

            // 新对话页面
            composable(Screen.Chat.route) {
                ChatScreen(
                    sessionId = null,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onMenuClick = {
                        scope.launch {
                            drawerState.open()
                        }
                    },
                    onWorkspaceClick = {
                        navController.navigate("${Screen.Workspace.route}/new?title=新对话")
                    }
                )
            }

            // 带初始消息的新对话
            composable("${Screen.Chat.route}?message={message}") { backStackEntry ->
                val initialMessage = backStackEntry.arguments?.getString("message")
                ChatScreen(
                    sessionId = null,
                    initialMessage = initialMessage,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onMenuClick = {
                        scope.launch {
                            drawerState.open()
                        }
                    },
                    onWorkspaceClick = {
                        navController.navigate("${Screen.Workspace.route}/new?title=新对话")
                    }
                )
            }

            // 历史会话的聊天页面
            composable("chat/{sessionId}") { backStackEntry ->
                val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
                ChatScreen(
                    sessionId = sessionId,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onMenuClick = {
                        scope.launch {
                            drawerState.open()
                        }
                    },
                    onWorkspaceClick = {
                        navController.navigate("${Screen.Workspace.route}/$sessionId?title=会话 #$sessionId")
                    }
                )
            }

            // 工作区文件管理
            composable("${Screen.Workspace.route}/{sessionId}?title={title}") { backStackEntry ->
                val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
                val title = backStackEntry.arguments?.getString("title") ?: "当前会话"
                WorkspaceScreen(
                    sessionId = sessionId,
                    sessionTitle = title,
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }

            // 设置页面
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onLogout = {
                        isLoggedIn = false
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onProfileClick = {
                        navController.navigate(Screen.Profile.route)
                    }
                )
            }

            // 个人资料页面
            composable(Screen.Profile.route) {
                ProfileScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onSaveSuccess = {
                        // TODO: 显示保存成功提示
                    }
                )
            }
        }
    }
}
