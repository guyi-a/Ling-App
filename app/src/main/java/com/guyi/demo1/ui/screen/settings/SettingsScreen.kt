package com.guyi.demo1.ui.screen.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.guyi.demo1.LingAgentApplication
import com.guyi.demo1.ui.components.InfoDialog
import com.guyi.demo1.ui.components.LogoutConfirmDialog
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit = {},
    onLogout: () -> Unit = {},
    onChangePasswordClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val themeManager = (context.applicationContext as LingAgentApplication).container.themeManager
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var showLogoutDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showThemeColorDialog by remember { mutableStateOf(false) }
    var showFontSizeDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var showFeedbackDialog by remember { mutableStateOf(false) }

    // 从 DataStore 读取所有设置
    val darkModePref by themeManager.isDarkMode.collectAsState(initial = null)
    val darkModeEnabled = darkModePref ?: false
    val themeColor by themeManager.themeColor.collectAsState(initial = "BLUE")
    val fontSize by themeManager.fontSize.collectAsState(initial = "MEDIUM")
    val notificationsEnabled by themeManager.notifications.collectAsState(initial = true)

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("设置", fontWeight = FontWeight.Bold) },
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
            // 外观设置
            SettingsSection(title = "外观设置") {
                SettingsSwitchItem(
                    icon = if (darkModeEnabled) Icons.Default.DarkMode else Icons.Outlined.LightMode,
                    title = "深色模式",
                    description = "切换深色/浅色主题",
                    checked = darkModeEnabled,
                    onCheckedChange = { scope.launch { themeManager.setDarkMode(it) } }
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                SettingsSelectionItem(
                    icon = Icons.Default.Palette,
                    title = "主题颜色",
                    description = themeColorLabel(themeColor),
                    onClick = { showThemeColorDialog = true }
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                SettingsSelectionItem(
                    icon = Icons.Default.FormatSize,
                    title = "字体大小",
                    description = fontSizeLabel(fontSize),
                    onClick = { showFontSizeDialog = true }
                )
            }

            // 聊天设置
            SettingsSection(title = "聊天设置") {
                SettingsSwitchItem(
                    icon = Icons.Default.Notifications,
                    title = "消息通知",
                    description = "接收新消息通知",
                    checked = notificationsEnabled,
                    onCheckedChange = { scope.launch { themeManager.setNotifications(it) } }
                )
            }

            // 账号与安全
            SettingsSection(title = "账号与安全") {
                SettingsItem(
                    icon = Icons.Default.Lock,
                    title = "修改密码",
                    description = "更改登录密码",
                    onClick = onChangePasswordClick
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

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
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "版本信息",
                    description = "Ling Agent v1.1.0",
                    onClick = { showAboutDialog = true }
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                SettingsItem(
                    icon = Icons.Default.PrivacyTip,
                    title = "隐私政策",
                    description = "了解我们如何保护你的隐私",
                    onClick = { showPrivacyDialog = true }
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                SettingsItem(
                    icon = Icons.Default.Description,
                    title = "用户协议",
                    description = "查看服务条款",
                    onClick = { showTermsDialog = true }
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                SettingsItem(
                    icon = Icons.Default.Feedback,
                    title = "反馈与支持",
                    description = "向我们提出建议和问题",
                    onClick = { showFeedbackDialog = true }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // 退出登录确认
    if (showLogoutDialog) {
        LogoutConfirmDialog(
            onConfirm = { showLogoutDialog = false; onLogout() },
            onDismiss = { showLogoutDialog = false }
        )
    }

    // 关于
    if (showAboutDialog) {
        InfoDialog(
            title = "关于 Ling Agent",
            message = "Ling Agent v1.1.0\n\n你的智能 AI 对话助手\n\n开发者：guyi",
            onConfirm = { showAboutDialog = false }
        )
    }

    // 主题颜色选择
    if (showThemeColorDialog) {
        ThemeColorDialog(
            currentColor = themeColor,
            onSelect = { color ->
                showThemeColorDialog = false
                scope.launch { themeManager.setThemeColor(color) }
            },
            onDismiss = { showThemeColorDialog = false }
        )
    }

    // 字体大小选择
    if (showFontSizeDialog) {
        FontSizeDialog(
            currentSize = fontSize,
            onSelect = { size ->
                showFontSizeDialog = false
                scope.launch { themeManager.setFontSize(size) }
            },
            onDismiss = { showFontSizeDialog = false }
        )
    }

    // 隐私政策
    if (showPrivacyDialog) {
        InfoDialog(
            title = "隐私政策",
            message = "Ling Agent 重视你的隐私保护。\n\n" +
                    "1. 数据收集：我们仅收集提供服务所必需的信息，包括账号信息和对话内容。\n\n" +
                    "2. 数据使用：你的数据仅用于提供和改进服务，不会出售给第三方。\n\n" +
                    "3. 数据存储：对话数据存储在安全的服务器上，采用加密传输。\n\n" +
                    "4. 数据删除：你可以随时删除对话记录和账号。\n\n" +
                    "5. 第三方服务：我们使用通义千问 AI 模型处理对话，相关数据受阿里云隐私政策保护。",
            onConfirm = { showPrivacyDialog = false }
        )
    }

    // 用户协议
    if (showTermsDialog) {
        InfoDialog(
            title = "用户协议",
            message = "欢迎使用 Ling Agent！\n\n" +
                    "1. 服务说明：Ling Agent 是一款 AI 智能助手，提供对话、数据分析等功能。\n\n" +
                    "2. 使用规范：请合法合规使用本服务，不得利用 AI 生成违法内容。\n\n" +
                    "3. 知识产权：AI 生成的内容供你个人使用，请注意核实内容准确性。\n\n" +
                    "4. 免责声明：AI 回答仅供参考，不构成专业建议。\n\n" +
                    "5. 服务变更：我们可能会更新服务内容和条款，届时将通知用户。",
            onConfirm = { showTermsDialog = false }
        )
    }

    // 反馈与支持
    if (showFeedbackDialog) {
        InfoDialog(
            title = "反馈与支持",
            message = "感谢你使用 Ling Agent！\n\n" +
                    "如果你在使用过程中遇到问题，或者有任何建议和想法，欢迎通过以下方式联系我们：\n\n" +
                    "邮箱：2903988117@qq.com\n\n" +
                    "我们会认真阅读每一条反馈，并尽快回复。你的意见是我们持续改进的动力！",
            onConfirm = { showFeedbackDialog = false }
        )
    }
}

// ============ 主题颜色选择弹窗 ============

@Composable
fun ThemeColorDialog(
    currentColor: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = listOf(
        "BLUE" to Pair("蓝色", Color(0xFF1565C0)),
        "PURPLE" to Pair("紫色", Color(0xFF6650a4)),
        "GREEN" to Pair("绿色", Color(0xFF2E7D32)),
        "ORANGE" to Pair("橙色", Color(0xFFE65100)),
        "RED" to Pair("红色", Color(0xFFC62828))
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("选择主题颜色", fontWeight = FontWeight.Bold)
        },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                colors.forEach { (key, pair) ->
                    val (label, color) = pair
                    val isSelected = key == currentColor
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSelect(key) }
                            .padding(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(color)
                                .then(
                                    if (isSelected) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                    else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    Icons.Default.Check,
                                    null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

// ============ 字体大小选择弹窗 ============

@Composable
fun FontSizeDialog(
    currentSize: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sizes = listOf(
        "SMALL" to "小",
        "MEDIUM" to "中（默认）",
        "LARGE" to "大",
        "EXTRA_LARGE" to "超大"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("选择字体大小", fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                sizes.forEach { (key, label) ->
                    val isSelected = key == currentSize
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSelect(key) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { onSelect(key) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            label,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

// ============ 工具函数 ============

private fun themeColorLabel(key: String): String = when (key) {
    "PURPLE" -> "紫色"
    "GREEN" -> "绿色"
    "ORANGE" -> "橙色"
    "RED" -> "红色"
    else -> "蓝色"
}

private fun fontSizeLabel(key: String): String = when (key) {
    "SMALL" -> "小"
    "LARGE" -> "大"
    "EXTRA_LARGE" -> "超大"
    else -> "中"
}

// ============ 通用组件 ============

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
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
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
            imageVector = icon, contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium,
                color = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
            Text(text = description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(
            Icons.Default.ChevronRight, null,
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
        Icon(icon, null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text(
                    description, style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
        Icon(Icons.Default.ChevronRight, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
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
        Icon(icon, null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
