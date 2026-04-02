package com.guyi.demo1.ui.screen.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.guyi.demo1.ui.components.DeleteConfirmDialog
import com.guyi.demo1.ui.components.InfoDialog
import com.guyi.demo1.ui.components.LogoutConfirmDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit = {},
    onLogout: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showClearCacheDialog by remember { mutableStateOf(false) }

    // 设置项状态
    var darkModeEnabled by remember { mutableStateOf(false) }
    var autoSaveEnabled by remember { mutableStateOf(true) }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var autoCleanupEnabled by remember { mutableStateOf(false) }

    // 主题颜色选择
    var selectedThemeColor by remember { mutableStateOf(ThemeColor.BLUE) }

    // 字体大小选择
    var selectedFontSize by remember { mutableStateOf(FontSize.MEDIUM) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "设置",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 用户信息卡片
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onProfileClick),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 用户头像
                    Card(
                        modifier = Modifier.size(60.dp),
                        shape = CircleShape,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "头像",
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "用户名",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "查看和编辑个人资料",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // 外观设置
            SettingsSection(title = "外观设置") {
                // 深色模式
                SettingsSwitchItem(
                    icon = if (darkModeEnabled) Icons.Default.DarkMode else Icons.Outlined.LightMode,
                    title = "深色模式",
                    description = "切换深色/浅色主题",
                    checked = darkModeEnabled,
                    onCheckedChange = { darkModeEnabled = it }
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                // 主题颜色
                SettingsSelectionItem(
                    icon = Icons.Default.Palette,
                    title = "主题颜色",
                    description = selectedThemeColor.label,
                    onClick = { /* TODO: 显示颜色选择器 */ }
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                // 字体大小
                SettingsSelectionItem(
                    icon = Icons.Default.FormatSize,
                    title = "字体大小",
                    description = selectedFontSize.label,
                    onClick = { /* TODO: 显示字体大小选择器 */ }
                )
            }

            // 聊天设置
            SettingsSection(title = "聊天设置") {
                // 自动保存
                SettingsSwitchItem(
                    icon = Icons.Default.Save,
                    title = "自动保存会话",
                    description = "自动保存所有对话内容",
                    checked = autoSaveEnabled,
                    onCheckedChange = { autoSaveEnabled = it }
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                // 通知
                SettingsSwitchItem(
                    icon = Icons.Default.Notifications,
                    title = "消息通知",
                    description = "接收新消息通知",
                    checked = notificationsEnabled,
                    onCheckedChange = { notificationsEnabled = it }
                )
            }

            // 工作区设置
            SettingsSection(title = "工作区设置") {
                // 自动清理
                SettingsSwitchItem(
                    icon = Icons.Default.CleaningServices,
                    title = "自动清理",
                    description = "定期清理超过30天的文件",
                    checked = autoCleanupEnabled,
                    onCheckedChange = { autoCleanupEnabled = it }
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                // 存储空间
                SettingsItem(
                    icon = Icons.Default.Storage,
                    title = "存储空间",
                    description = "已使用 128 MB / 1 GB",
                    onClick = { /* TODO: 显示存储详情 */ }
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                // 清除缓存
                SettingsItem(
                    icon = Icons.Default.DeleteSweep,
                    title = "清除缓存",
                    description = "清理临时文件和缓存",
                    onClick = { showClearCacheDialog = true }
                )
            }

            // 账号与安全
            SettingsSection(title = "账号与安全") {
                // 修改密码
                SettingsItem(
                    icon = Icons.Default.Lock,
                    title = "修改密码",
                    description = "更改登录密码",
                    onClick = { /* TODO: 导航到修改密码页面 */ }
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                // 退出登录
                SettingsItem(
                    icon = Icons.AutoMirrored.Filled.Logout,
                    title = "退出登录",
                    description = "登出当前账号",
                    onClick = { showLogoutDialog = true },
                    isDestructive = true
                )
            }

            // 关于
            SettingsSection(title = "关于") {
                // 版本信息
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "版本信息",
                    description = "Ling Agent v1.0.0",
                    onClick = { showAboutDialog = true }
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                // 隐私政策
                SettingsItem(
                    icon = Icons.Default.PrivacyTip,
                    title = "隐私政策",
                    description = "了解我们如何保护你的隐私",
                    onClick = { /* TODO: 显示隐私政策 */ }
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                // 用户协议
                SettingsItem(
                    icon = Icons.Default.Description,
                    title = "用户协议",
                    description = "查看服务条款",
                    onClick = { /* TODO: 显示用户协议 */ }
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                // 反馈与支持
                SettingsItem(
                    icon = Icons.Default.Feedback,
                    title = "反馈与支持",
                    description = "向我们提出建议和问题",
                    onClick = { /* TODO: 打开反馈页面 */ }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // 退出登录确认对话框
    if (showLogoutDialog) {
        LogoutConfirmDialog(
            onConfirm = {
                showLogoutDialog = false
                onLogout()
            },
            onDismiss = {
                showLogoutDialog = false
            }
        )
    }

    // 关于对话框
    if (showAboutDialog) {
        InfoDialog(
            title = "关于 Ling Agent",
            message = "Ling Agent v1.0.0\n\n你的智能对话助手\n\nCopyright © 2024 Ling Team\n保留所有权利",
            onConfirm = { showAboutDialog = false }
        )
    }

    // 清除缓存确认对话框
    if (showClearCacheDialog) {
        DeleteConfirmDialog(
            itemName = "所有缓存",
            onConfirm = {
                // TODO: 实际清除缓存逻辑
                showClearCacheDialog = false
            },
            onDismiss = {
                showClearCacheDialog = false
            }
        )
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = if (isDestructive) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = if (isDestructive) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }
}

@Composable
fun SettingsSelectionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }
}

@Composable
fun SettingsSwitchItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

// 主题颜色枚举
enum class ThemeColor(val label: String) {
    BLUE("蓝色"),
    GREEN("绿色"),
    PURPLE("紫色"),
    ORANGE("橙色"),
    RED("红色")
}

// 字体大小枚举
enum class FontSize(val label: String) {
    SMALL("小"),
    MEDIUM("中"),
    LARGE("大"),
    EXTRA_LARGE("超大")
}
