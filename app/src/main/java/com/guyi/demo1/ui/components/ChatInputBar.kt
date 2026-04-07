package com.guyi.demo1.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp

/**
 * 聊天输入框组件
 *
 * @param text 当前输入文本
 * @param onTextChange 文本变化回调
 * @param isEnabled 是否启用（生成时禁用）
 * @param attachmentCount 附件数量
 * @param onAttachClick 点击附加按钮回调
 * @param onSendClick 点击发送按钮回调
 * @param modifier 修饰符
 */
@Composable
fun ChatInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    isEnabled: Boolean = true,
    attachmentCount: Int = 0,
    onAttachClick: () -> Unit = {},
    onSendClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 附件按钮
            IconButton(
                onClick = onAttachClick,
                enabled = isEnabled,
                modifier = Modifier.size(40.dp)
            ) {
                Badge(
                    modifier = Modifier.offset(x = 8.dp, y = (-8).dp),
                    containerColor = if (attachmentCount > 0) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                ) {
                    if (attachmentCount > 0) {
                        Text(
                            text = attachmentCount.toString(),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
                Icon(
                    Icons.Default.AttachFile,
                    contentDescription = "附件",
                    tint = if (isEnabled) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    }
                )
            }

            // 输入框
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 0.dp
            ) {
                BasicTextField(
                    value = text,
                    onValueChange = onTextChange,
                    enabled = isEnabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    decorationBox = { innerTextField ->
                        if (text.isEmpty()) {
                            Text(
                                text = "输入消息...",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                        innerTextField()
                    }
                )
            }

            // 发送按钮
            IconButton(
                onClick = onSendClick,
                enabled = isEnabled && text.isNotBlank(),
                modifier = Modifier.size(40.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = if (text.isNotBlank()) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                    contentColor = if (text.isNotBlank()) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                    }
                )
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    contentDescription = "发送"
                )
            }
        }
    }
}
