package com.guyi.demo1.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * 通用确认对话框
 *
 * @param title 标题
 * @param message 消息内容
 * @param confirmText 确认按钮文字，默认"确定"
 * @param dismissText 取消按钮文字，默认"取消"
 * @param onConfirm 确认回调
 * @param onDismiss 取消回调
 * @param isDanger 是否为危险操作（红色按钮），默认 false
 */
@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmText: String = "确定",
    dismissText: String = "取消",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isDanger: Boolean = false
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape = RoundedCornerShape(8.dp),
                colors = if (isDanger) {
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                } else {
                    ButtonDefaults.buttonColors()
                }
            ) {
                Text(confirmText, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(dismissText)
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

/**
 * 带图标的确认对话框
 */
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
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Text(
                text = icon,
                style = MaterialTheme.typography.displaySmall
            )
        },
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape = RoundedCornerShape(8.dp),
                colors = if (isDanger) {
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                } else {
                    ButtonDefaults.buttonColors()
                }
            ) {
                Text(confirmText, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(dismissText)
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

/**
 * 删除确认对话框 - 快捷封装
 */
@Composable
fun DeleteConfirmDialog(
    itemName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    ConfirmDialogWithIcon(
        icon = "🗑️",
        title = "删除确认",
        message = "确定要删除「$itemName」吗？此操作无法撤销。",
        confirmText = "删除",
        dismissText = "取消",
        onConfirm = onConfirm,
        onDismiss = onDismiss,
        isDanger = true
    )
}

/**
 * 退出登录确认对话框 - 快捷封装
 */
@Composable
fun LogoutConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    ConfirmDialogWithIcon(
        icon = "👋",
        title = "退出登录",
        message = "确定要退出登录吗？",
        confirmText = "退出",
        dismissText = "取消",
        onConfirm = onConfirm,
        onDismiss = onDismiss,
        isDanger = true
    )
}

/**
 * 信息提示对话框 - 只有一个确认按钮
 */
@Composable
fun InfoDialog(
    title: String,
    message: String,
    confirmText: String = "知道了",
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onConfirm,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(confirmText, fontWeight = FontWeight.SemiBold)
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

/**
 * 警告对话框
 */
@Composable
fun WarningDialog(
    title: String,
    message: String,
    confirmText: String = "我知道了",
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onConfirm,
        icon = {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp)
            )
        },
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(confirmText, fontWeight = FontWeight.SemiBold)
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}
