package com.guyi.demo1.ui.screen.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Feedback
import androidx.compose.material.icons.outlined.FormatSize
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.guyi.demo1.LingAgentApplication
import com.guyi.demo1.ui.components.InfoDialog
import com.guyi.demo1.ui.components.LogoutConfirmDialog
import com.guyi.demo1.ui.theme.LingTheme
import kotlinx.coroutines.launch

/**
 * 设置页 — Warm Calm 重做
 *  · 自定义顶栏（圆形返回 + 标题）
 *  · 章节卡片：描边而非阴影
 *  · 行内项：圆形 accent 容器 + 图标
 *  · 主题颜色 dialog：Warm Calm 5 色板
 *  · 字体大小 dialog：带预览的自定义 radio
 */
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit = {},
    onLogout: () -> Unit = {},
    onChangePasswordClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val themeManager = (context.applicationContext as LingAgentApplication).container.themeManager
    val scope = rememberCoroutineScope()
    val cs = MaterialTheme.colorScheme
    val space = LingTheme.spacing

    var showLogoutDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showThemeColorDialog by remember { mutableStateOf(false) }
    var showFontSizeDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var showFeedbackDialog by remember { mutableStateOf(false) }

    val darkModePref by themeManager.isDarkMode.collectAsState(initial = null)
    val darkModeEnabled = darkModePref ?: false
    val themeColor by themeManager.themeColor.collectAsState(initial = "ORANGE")
    val fontSize by themeManager.fontSize.collectAsState(initial = "MEDIUM")
    val notificationsEnabled by themeManager.notifications.collectAsState(initial = true)

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
                IconCircleButton(icon = Icons.AutoMirrored.Filled.ArrowBack, contentDesc = "返回", onClick = onBackClick)
                Spacer(Modifier.width(space.sm))
                Text(
                    text = "设置",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = cs.onSurface
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = space.pageHorizontal)
            ) {
                Spacer(Modifier.height(space.md))

                // 外观
                SettingsSection(title = "外观") {
                    SettingsSwitchItem(
                        icon = if (darkModeEnabled) Icons.Outlined.DarkMode else Icons.Outlined.LightMode,
                        title = "深色模式",
                        description = "切换深色 / 浅色主题",
                        checked = darkModeEnabled,
                        onCheckedChange = { scope.launch { themeManager.setDarkMode(it) } }
                    )
                    ItemDivider()
                    SettingsSelectionItem(
                        icon = Icons.Outlined.Palette,
                        title = "主题颜色",
                        description = themeColorLabel(themeColor),
                        accent = themeColorSwatch(themeColor),
                        onClick = { showThemeColorDialog = true }
                    )
                    ItemDivider()
                    SettingsSelectionItem(
                        icon = Icons.Outlined.FormatSize,
                        title = "字体大小",
                        description = fontSizeLabel(fontSize),
                        accent = null,
                        onClick = { showFontSizeDialog = true }
                    )
                }

                Spacer(Modifier.height(space.lg))

                SettingsSection(title = "聊天") {
                    SettingsSwitchItem(
                        icon = Icons.Outlined.NotificationsNone,
                        title = "消息通知",
                        description = "接收新消息通知",
                        checked = notificationsEnabled,
                        onCheckedChange = { scope.launch { themeManager.setNotifications(it) } }
                    )
                }

                Spacer(Modifier.height(space.lg))

                SettingsSection(title = "账号与安全") {
                    SettingsItem(
                        icon = Icons.Outlined.Lock,
                        title = "修改密码",
                        description = "更改登录密码",
                        onClick = onChangePasswordClick
                    )
                    ItemDivider()
                    SettingsItem(
                        icon = Icons.AutoMirrored.Outlined.Logout,
                        title = "退出登录",
                        description = "登出当前账号",
                        onClick = { showLogoutDialog = true },
                        destructive = true
                    )
                }

                Spacer(Modifier.height(space.lg))

                SettingsSection(title = "关于") {
                    SettingsItem(
                        icon = Icons.Outlined.Info,
                        title = "版本信息",
                        description = "Ling Agent v2.1.0",
                        onClick = { showAboutDialog = true }
                    )
                    ItemDivider()
                    SettingsItem(
                        icon = Icons.Outlined.PrivacyTip,
                        title = "隐私政策",
                        description = "了解我们如何保护你的隐私",
                        onClick = { showPrivacyDialog = true }
                    )
                    ItemDivider()
                    SettingsItem(
                        icon = Icons.Outlined.Description,
                        title = "用户协议",
                        description = "查看服务条款",
                        onClick = { showTermsDialog = true }
                    )
                    ItemDivider()
                    SettingsItem(
                        icon = Icons.Outlined.Feedback,
                        title = "反馈与支持",
                        description = "向我们提建议",
                        onClick = { showFeedbackDialog = true }
                    )
                }

                Spacer(Modifier.height(space.xl))
                Spacer(Modifier.navigationBarsPadding())
            }
        }
    }

    if (showLogoutDialog) {
        LogoutConfirmDialog(
            onConfirm = { showLogoutDialog = false; onLogout() },
            onDismiss = { showLogoutDialog = false }
        )
    }
    if (showAboutDialog) {
        InfoDialog(
            title = "关于 Ling Agent",
            message = "Ling Agent v2.1.0\n\n" +
                    "基于多 Agent 协作架构的 AI 生产力工具，覆盖：\n" +
                    "对话 · 开发 · 数据 · 文档 · 身心健康\n\n" +
                    "开发者：guyi\n" +
                    "GitHub: github.com/guyi-a/Ling-Agent",
            onConfirm = { showAboutDialog = false }
        )
    }
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
    if (showPrivacyDialog) {
        InfoDialog(
            title = "隐私政策",
            message = "Ling Agent 重视你的隐私保护。\n\n" +
                    "1. 数据收集：我们仅收集提供服务所必需的信息，包括账号信息和对话内容。\n\n" +
                    "2. 数据使用：你的数据仅用于提供和改进服务，不会出售给第三方。\n\n" +
                    "3. 数据存储：对话数据存储在安全的服务器上，采用加密传输。\n\n" +
                    "4. 数据删除：你可以随时删除对话记录和账号。\n\n" +
                    "5. 第三方模型：Ling Agent 接入 DeepSeek、通义千问、智谱 GLM 等多家 LLM Provider，由你在配置中选择。对话内容会被发送到所选 Provider 处理，相应数据受其各自隐私政策约束。",
            onConfirm = { showPrivacyDialog = false }
        )
    }
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
    if (showFeedbackDialog) {
        InfoDialog(
            title = "反馈与支持",
            message = "感谢你使用 Ling Agent！\n\n" +
                    "遇到问题或有建议，欢迎通过以下方式联系：\n\n" +
                    "· 邮箱：2903988117@qq.com\n" +
                    "· GitHub Issues：github.com/guyi-a/Ling-Agent/issues\n\n" +
                    "我们会认真阅读每一条反馈，并尽快回复。",
            onConfirm = { showFeedbackDialog = false }
        )
    }
}

// =====================================================
//  通用元件
// =====================================================

@Composable
private fun IconCircleButton(icon: ImageVector, contentDesc: String, onClick: () -> Unit) {
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

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val shapes = LingTheme.shapes
    Column {
        // 章节标题（小、克制、letter-spacing）
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(12.dp)
                    .height(1.dp)
                    .background(cs.onSurfaceVariant.copy(alpha = 0.5f))
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(
                    letterSpacing = 2.sp,
                    lineHeight = 14.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = cs.onSurfaceVariant
            )
        }

        Surface(
            shape = shapes.md,
            color = cs.surface,
            modifier = Modifier
                .fillMaxWidth()
                .border(width = 1.dp, color = cs.outlineVariant, shape = shapes.md)
        ) {
            Column { content() }
        }
    }
}

@Composable
private fun ItemDivider() {
    val cs = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 60.dp)
            .height(0.5.dp)
            .background(cs.outlineVariant.copy(alpha = 0.5f))
    )
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
    destructive: Boolean = false
) {
    val cs = MaterialTheme.colorScheme
    val accent = if (destructive) cs.error else cs.onSurfaceVariant
    val titleColor = if (destructive) cs.error else cs.onSurface

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconHolder(icon = icon, accent = accent)
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = titleColor
            )
            Spacer(Modifier.height(1.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 16.sp),
                color = cs.onSurfaceVariant.copy(alpha = 0.75f)
            )
        }
        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = cs.onSurfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun SettingsSelectionItem(
    icon: ImageVector,
    title: String,
    description: String,
    accent: Color?,
    onClick: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconHolder(icon = icon, accent = cs.onSurfaceVariant)
        Spacer(Modifier.width(14.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
            color = cs.onSurface,
            modifier = Modifier.weight(1f)
        )

        // 当前值（带可选色块）
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (accent != null) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(accent)
                )
                Spacer(Modifier.width(6.dp))
            }
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = cs.onSurfaceVariant
            )
        }
        Spacer(Modifier.width(8.dp))
        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = cs.onSurfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val cs = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconHolder(icon = icon, accent = cs.onSurfaceVariant)
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = cs.onSurface
            )
            Spacer(Modifier.height(1.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 16.sp),
                color = cs.onSurfaceVariant.copy(alpha = 0.75f)
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = cs.onPrimary,
                checkedTrackColor = cs.primary,
                uncheckedThumbColor = cs.outline,
                uncheckedTrackColor = cs.surfaceVariant,
                uncheckedBorderColor = cs.outlineVariant
            )
        )
    }
}

@Composable
private fun IconHolder(icon: ImageVector, accent: Color) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(accent.copy(alpha = 0.10f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accent,
            modifier = Modifier.size(16.dp)
        )
    }
}

// =====================================================
//  主题颜色 dialog
// =====================================================

@Composable
private fun ThemeColorDialog(
    currentColor: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val shapes = LingTheme.shapes
    val swatches = listOf(
        "ORANGE" to Pair("焦糖橙", Color(0xFFB5663A)),
        "GREEN" to Pair("苔绿", Color(0xFF5A7A48)),
        "BLUE" to Pair("雾蓝", Color(0xFF4A6E7E)),
        "PURPLE" to Pair("陶土紫", Color(0xFF84576E)),
        "RED" to Pair("砖窑红", Color(0xFF9C4E40))
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = cs.surface,
        shape = shapes.lg,
        title = {
            Text(
                "主题颜色",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium),
                color = cs.onSurface
            )
        },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                swatches.forEach { (key, pair) ->
                    val (label, color) = pair
                    val isSelected = key == currentColor
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(shapes.sm)
                            .clickable { onSelect(key) }
                            .padding(vertical = 6.dp, horizontal = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(color)
                                .then(
                                    if (isSelected) {
                                        Modifier.border(2.5.dp, cs.onSurface, CircleShape)
                                    } else {
                                        Modifier.border(
                                            1.dp,
                                            cs.outlineVariant.copy(alpha = 0.6f),
                                            CircleShape
                                        )
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Outlined.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall.copy(lineHeight = 12.sp),
                            color = if (isSelected) cs.onSurface else cs.onSurfaceVariant,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("完成", color = cs.primary, fontWeight = FontWeight.Medium)
            }
        }
    )
}

// =====================================================
//  字体大小 dialog
// =====================================================

@Composable
private fun FontSizeDialog(
    currentSize: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val shapes = LingTheme.shapes
    val sizes = listOf(
        Triple("SMALL", "小", 14),
        Triple("MEDIUM", "中（默认）", 16),
        Triple("LARGE", "大", 18),
        Triple("EXTRA_LARGE", "超大", 21)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = cs.surface,
        shape = shapes.lg,
        title = {
            Text(
                "字体大小",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium),
                color = cs.onSurface
            )
        },
        text = {
            Column {
                sizes.forEach { (key, label, previewSize) ->
                    val isSelected = key == currentSize
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(shapes.sm)
                            .clickable { onSelect(key) }
                            .padding(horizontal = 8.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 自定义 radio
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .border(
                                    width = 1.5.dp,
                                    color = if (isSelected) cs.primary else cs.outline,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(cs.primary)
                                )
                            }
                        }
                        Spacer(Modifier.width(14.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = previewSize.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            ),
                            color = if (isSelected) cs.onSurface else cs.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("完成", color = cs.primary, fontWeight = FontWeight.Medium)
            }
        }
    )
}

// =====================================================
//  Helpers
// =====================================================

private fun themeColorLabel(key: String): String = when (key) {
    "PURPLE" -> "陶土紫"
    "GREEN" -> "苔绿"
    "BLUE" -> "雾蓝"
    "RED" -> "砖窑红"
    else -> "焦糖橙"
}

private fun themeColorSwatch(key: String): Color = when (key) {
    "PURPLE" -> Color(0xFF84576E)
    "GREEN" -> Color(0xFF5A7A48)
    "BLUE" -> Color(0xFF4A6E7E)
    "RED" -> Color(0xFF9C4E40)
    else -> Color(0xFFB5663A)
}

private fun fontSizeLabel(key: String): String = when (key) {
    "SMALL" -> "小"
    "LARGE" -> "大"
    "EXTRA_LARGE" -> "超大"
    else -> "中"
}
