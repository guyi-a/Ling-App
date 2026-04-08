package com.guyi.demo1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.guyi.demo1.ui.navigation.NavGraph
import com.guyi.demo1.ui.theme.Demo1Theme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appContainer = (application as LingAgentApplication).container
        enableEdgeToEdge()
        setContent {
            val darkModePref by appContainer.themeManager.isDarkMode.collectAsState(initial = null)
            val darkTheme = darkModePref ?: isSystemInDarkTheme()
            val themeColor by appContainer.themeManager.themeColor.collectAsState(initial = "BLUE")
            val fontSize by appContainer.themeManager.fontSize.collectAsState(initial = "MEDIUM")

            Demo1Theme(
                darkTheme = darkTheme,
                themeColor = themeColor,
                fontSizeName = fontSize
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavGraph(navController = navController)
                }
            }
        }
    }
}
