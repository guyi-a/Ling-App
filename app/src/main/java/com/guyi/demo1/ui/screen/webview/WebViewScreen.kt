package com.guyi.demo1.ui.screen.webview

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.guyi.demo1.LingAgentApplication
import com.guyi.demo1.data.network.ApiConfig
import kotlinx.coroutines.runBlocking

/**
 * WebView 预览 — Warm Calm 重做
 *   · 自定义顶栏：返回 + 标题 + 端口胶囊 + 刷新圆按钮
 *   · 细进度条（primary 色）
 *   · WebView 配置保持不变（JS / DOM / Zoom / Auth Header）
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewScreen(
    port: Int? = null,
    url: String? = null,
    title: String = "应用预览",
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val appContainer = (context.applicationContext as LingAgentApplication).container
    val token = runBlocking { appContainer.tokenManager.getToken() }
    val cs = MaterialTheme.colorScheme

    var webView by remember { mutableStateOf<WebView?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var progress by remember { mutableIntStateOf(0) }
    var pageTitle by remember { mutableStateOf(title) }

    val previewUrl = url ?: "${ApiConfig.BASE_URL}/api/preview/$port/"

    Surface(color = cs.background, modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ─── 自定义顶栏 ───
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircleIconButton(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDesc = "返回",
                    onClick = onBackClick
                )
                Spacer(Modifier.width(4.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = pageTitle,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 18.sp
                        ),
                        color = cs.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (port != null) {
                        Spacer(Modifier.height(1.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(cs.primary)
                            )
                            Spacer(Modifier.width(5.dp))
                            Text(
                                text = ":$port",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    lineHeight = 12.sp
                                ),
                                color = cs.onSurfaceVariant.copy(alpha = 0.75f)
                            )
                        }
                    }
                }
                CircleIconButton(
                    icon = Icons.Outlined.Refresh,
                    contentDesc = "刷新",
                    onClick = { webView?.reload() }
                )
            }

            // 进度条（加载时显示细线）
            if (isLoading) {
                LinearProgressIndicator(
                    progress = { progress / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp),
                    color = cs.primary,
                    trackColor = cs.outlineVariant.copy(alpha = 0.5f)
                )
            } else {
                // 顶栏与 WebView 间的一条细分隔线
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(cs.outlineVariant.copy(alpha = 0.5f))
                )
            }

            // ─── WebView ───
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.allowFileAccess = false
                        settings.useWideViewPort = true
                        settings.loadWithOverviewMode = true
                        settings.setSupportZoom(true)
                        settings.builtInZoomControls = true
                        settings.displayZoomControls = false

                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                isLoading = true
                            }
                            override fun onPageFinished(view: WebView?, url: String?) {
                                isLoading = false
                            }
                            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                return false
                            }
                        }

                        webChromeClient = object : WebChromeClient() {
                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                progress = newProgress
                            }
                            override fun onReceivedTitle(view: WebView?, t: String?) {
                                t?.let { if (it.isNotBlank()) pageTitle = it }
                            }
                        }

                        val headers = mutableMapOf<String, String>()
                        if (token != null) {
                            headers["Authorization"] = "Bearer $token"
                        }
                        loadUrl(previewUrl, headers)
                        webView = this
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun CircleIconButton(
    icon: ImageVector,
    contentDesc: String,
    onClick: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDesc,
            tint = cs.onSurface,
            modifier = Modifier.size(20.dp)
        )
    }
}
