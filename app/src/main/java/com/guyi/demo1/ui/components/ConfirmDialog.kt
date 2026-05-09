package com.guyi.demo1.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.guyi.demo1.ui.theme.LingTheme

/**
 * Warm Calm 风格通用确认对话框
 *   · Surface 底 + lg 圆角
 *   · 可选左侧圆形图标徽章
 *   · isDanger 让确认按钮显示为 error 色
 */
@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmText: String = "确定",
    dismissText: String = "取消",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isDanger: Boolean = false,
    icon: ImageVector? = null,
    iconTint: Color? = null
) {
    val cs = MaterialTheme.colorScheme
    val accent = iconTint ?: if (isDanger) cs.error else cs.primary

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = cs.surface,
        shape = LingTheme.shapes.lg,
        title = {
            if (icon != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(accent.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = accent,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = cs.onSurface
                    )
                }
            } else {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = cs.onSurface
                )
            }
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
                color = cs.onSurfaceVariant
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = confirmText,
                    color = if (isDanger) cs.error else cs.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = dismissText,
                    color = cs.onSurfaceVariant
                )
            }
        }
    )
}

/** 带图标的确认对话框 — 保留旧签名兼容 */
@Composable
fun ConfirmDialogWithIcon(
    icon: String,
    title: String,
    message: String,
    confirmText: String = "确定",
    dismissText: String = "取消",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isDanger: Boolean = false
) {
    // emoji 参数保留签名，但不再渲染 emoji（Warm Calm 不用 emoji 图标），
    // 根据 isDanger 自动选一个合适的 Material 图标
    val materialIcon = if (isDanger) Icons.Outlined.ErrorOutline else Icons.Outlined.Info
    ConfirmDialog(
        title = title,
        message = message,
        confirmText = confirmText,
        dismissText = dismissText,
        onConfirm = onConfirm,
        onDismiss = onDismiss,
        isDanger = isDanger,
        icon = materialIcon
    )
}

/** 删除确认 */
@Composable
fun DeleteConfirmDialog(
    itemName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    ConfirmDialog(
        title = "删除",
        message = "确定要删除「$itemName」吗？\n此操作无法撤销。",
        confirmText = "删除",
        dismissText = "取消",
        onConfirm = onConfirm,
        onDismiss = onDismiss,
        isDanger = true,
        icon = Icons.Outlined.Delete
    )
}

/** 退出登录确认 */
@Composable
fun LogoutConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    ConfirmDialog(
        title = "退出登录",
        message = "确定要退出当前账号吗？",
        confirmText = "退出",
        dismissText = "取消",
        onConfirm = onConfirm,
        onDismiss = onDismiss,
        isDanger = true,
        icon = Icons.AutoMirrored.Outlined.Logout
    )
}

/** 信息提示（单按钮） */
@Composable
fun InfoDialog(
    title: String,
    message: String,
    confirmText: String = "知道了",
    onConfirm: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    AlertDialog(
        onDismissRequest = onConfirm,
        containerColor = cs.surface,
        shape = LingTheme.shapes.lg,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = cs.onSurface
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
                color = cs.onSurfaceVariant
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = confirmText,
                    color = cs.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    )
}

/** 警告对话框（error 色图标徽章） */
@Composable
fun WarningDialog(
    title: String,
    message: String,
    confirmText: String = "我知道了",
    onConfirm: () -> Unit
) {
    ConfirmDialog(
        title = title,
        message = message,
        confirmText = confirmText,
        dismissText = "",
        onConfirm = onConfirm,
        onDismiss = onConfirm,
        isDanger = true,
        icon = Icons.Outlined.ErrorOutline
    )
}
