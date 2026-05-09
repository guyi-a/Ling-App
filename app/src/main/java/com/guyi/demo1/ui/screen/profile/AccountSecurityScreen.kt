package com.guyi.demo1.ui.screen.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.guyi.demo1.LingAgentApplication
import com.guyi.demo1.ui.theme.LingTheme

/**
 * 账号安全 — Warm Calm 重做
 * 保留：userApi.getUser + 展示 deviceId / deviceModel / createdAt / lastActiveAt / isActive
 */
@Composable
fun AccountSecurityScreen(
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val appContainer = (context.applicationContext as LingAgentApplication).container
    val tokenManager = appContainer.tokenManager
    val userApi = appContainer.userApi
    val cs = MaterialTheme.colorScheme
    val space = LingTheme.spacing
    val shapes = LingTheme.shapes

    val storedUserId by tokenManager.getUserIdFlow().collectAsState(initial = null)

    var isLoading by remember { mutableStateOf(true) }
    var deviceId by remember { mutableStateOf("") }
    var deviceModel by remember { mutableStateOf("") }
    var createdAt by remember { mutableStateOf("") }
    var lastActiveAt by remember { mutableStateOf("") }
    var isActive by remember { mutableStateOf(true) }

    LaunchedEffect(storedUserId) {
        storedUserId?.let { userId ->
            try {
                val user = userApi.getUser(userId)
                deviceId = user.deviceId
                deviceModel = user.deviceModel ?: "未知"
                createdAt = user.createdAt.replace("T", " ").take(19)
                lastActiveAt = user.lastActiveAt.replace("T", " ").take(19)
                isActive = user.isActive
            } catch (_: Exception) {}
            isLoading = false
        }
    }

    Surface(color = cs.background, modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 顶栏
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = space.md, vertical = space.xs),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onBackClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "返回",
                        tint = cs.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.width(space.sm))
                Text(
                    text = "账号安全",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = cs.onSurface
                )
            }

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = cs.primary,
                        strokeWidth = 2.5.dp,
                        modifier = Modifier.size(32.dp)
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = space.pageHorizontal)
                ) {
                    Spacer(Modifier.height(space.md))

                    // 账号状态卡片
                    val statusAccent = if (isActive) cs.primary else cs.error
                    Surface(
                        shape = shapes.md,
                        color = statusAccent.copy(alpha = 0.08f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                color = statusAccent.copy(alpha = 0.25f),
                                shape = shapes.md
                            )
                    ) {
                        Row(
                            modifier = Modifier.padding(
                                horizontal = space.lg,
                                vertical = space.md
                            ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(statusAccent),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isActive) Icons.Outlined.CheckCircle
                                    else Icons.Outlined.ErrorOutline,
                                    contentDescription = null,
                                    tint = if (isActive) cs.onPrimary else cs.onError,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isActive) "账号正常" else "账号已禁用",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = cs.onBackground
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = if (isActive) "你的账号状态良好" else "请联系管理员",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = cs.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(space.xl))

                    // 安全信息
                    SectionLabel("安全信息")
                    Spacer(Modifier.height(space.sm))
                    Surface(
                        shape = shapes.md,
                        color = cs.surface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(width = 1.dp, color = cs.outlineVariant, shape = shapes.md)
                    ) {
                        Column {
                            SecurityRow(
                                icon = Icons.Outlined.PhoneAndroid,
                                label = "登录设备",
                                value = deviceModel
                            )
                            RowDivider()
                            SecurityRow(
                                icon = Icons.Outlined.Fingerprint,
                                label = "设备 ID",
                                value = if (deviceId.length > 16) deviceId.take(16) + "…" else deviceId
                            )
                            RowDivider()
                            SecurityRow(
                                icon = Icons.Outlined.CalendarToday,
                                label = "注册时间",
                                value = createdAt
                            )
                            RowDivider()
                            SecurityRow(
                                icon = Icons.Outlined.Schedule,
                                label = "最后活跃",
                                value = lastActiveAt
                            )
                        }
                    }

                    Spacer(Modifier.height(space.xxl))
                    Spacer(Modifier.navigationBarsPadding())
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    val cs = MaterialTheme.colorScheme
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(start = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .width(12.dp)
                .height(1.dp)
                .background(cs.onSurfaceVariant.copy(alpha = 0.5f))
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium.copy(
                letterSpacing = 2.sp,
                lineHeight = 14.sp,
                fontWeight = FontWeight.Medium
            ),
            color = cs.onSurfaceVariant
        )
    }
}

@Composable
private fun SecurityRow(icon: ImageVector, label: String, value: String) {
    val cs = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(cs.onSurfaceVariant.copy(alpha = 0.10f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = cs.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 1.sp,
                    lineHeight = 12.sp
                ),
                color = cs.onSurfaceVariant.copy(alpha = 0.7f)
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = value.ifBlank { "—" },
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = cs.onSurface
            )
        }
    }
}

@Composable
private fun RowDivider() {
    val cs = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 60.dp)
            .height(0.5.dp)
            .background(cs.outlineVariant.copy(alpha = 0.5f))
    )
}
