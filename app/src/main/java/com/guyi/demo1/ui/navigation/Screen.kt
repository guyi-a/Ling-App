package com.guyi.demo1.ui.navigation

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object Chat : Screen("chat/{sessionId}") {
        fun createRoute(sessionId: String? = null) =
            if (sessionId != null) "chat/$sessionId" else "chat/new"
    }
    data object SessionList : Screen("session_list")
}
