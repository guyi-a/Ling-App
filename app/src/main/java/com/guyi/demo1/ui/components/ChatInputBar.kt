package com.guyi.demo1.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.guyi.demo1.ui.theme.LingTheme

/**
 * 聊天输入栏 — Warm Calm 重做
 *   · 顶部细分隔线（取代 elevation 阴影）
 *   · 附件按钮：圆形 + 有附件时显示数字
 *   · 输入框：surfaceVariant 半透 + 18dp 圆角，聚焦时描边变 primary
 *   · 发送 / 停止按钮：40dp 圆形，按状态切色
 */
@Composable
fun ChatInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    isEnabled: Boolean = true,
    isStreaming: Boolean = false,
    attachmentCount: Int = 0,
    onAttachClick: () -> Unit = {},
    onSendClick: () -> Unit = {},
    onStopClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val cs = MaterialTheme.colorScheme
    val shapes = LingTheme.shapes
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(cs.background)
    ) {
        // 顶部细分隔线（取代 elevation）
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .size(1.dp)
                .background(cs.outlineVariant.copy(alpha = 0.5f))
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .navigationBarsPadding(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 附件按钮（圆形，带 badge）
            AttachButton(
                count = attachmentCount,
                enabled = isEnabled,
                onClick = onAttachClick
            )

            // 输入框
            Surface(
                shape = shapes.md,
                color = cs.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 44.dp, max = 140.dp)
                    .border(
                        width = 1.dp,
                        color = if (focused) cs.primary else cs.outlineVariant.copy(alpha = 0.6f),
                        shape = shapes.md
                    )
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp)
                ) {
                    BasicTextField(
                        value = text,
                        onValueChange = onTextChange,
                        enabled = !isStreaming,
                        interactionSource = interaction,
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            color = cs.onSurface,
                            lineHeight = 20.sp
                        ),
                        cursorBrush = SolidColor(cs.primary)
                    )
                    if (text.isEmpty()) {
                        Text(
                            text = if (isStreaming) "正在生成回答…" else "说点什么…",
                            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
                            color = cs.onSurfaceVariant.copy(alpha = 0.55f)
                        )
                    }
                }
            }

            // 发送 / 停止按钮
            if (isStreaming) {
                CircleActionButton(
                    icon = Icons.Outlined.Stop,
                    contentDesc = "停止",
                    bgColor = cs.error,
                    fgColor = cs.onError,
                    enabled = true,
                    onClick = onStopClick
                )
            } else {
                val canSend = text.isNotBlank()
                CircleActionButton(
                    icon = Icons.AutoMirrored.Filled.Send,
                    contentDesc = "发送",
                    bgColor = if (canSend) cs.primary else cs.surfaceVariant,
                    fgColor = if (canSend) cs.onPrimary else cs.onSurfaceVariant.copy(alpha = 0.5f),
                    enabled = canSend,
                    onClick = onSendClick
                )
            }
        }
    }
}

@Composable
private fun AttachButton(
    count: Int,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val hasAttach = count > 0
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(
                if (hasAttach) cs.primary.copy(alpha = 0.12f)
                else cs.surfaceVariant.copy(alpha = 0.5f)
            )
            .border(
                width = 1.dp,
                color = if (hasAttach) cs.primary.copy(alpha = 0.3f)
                else cs.outlineVariant.copy(alpha = 0.6f),
                shape = CircleShape
            )
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        if (hasAttach) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                    lineHeight = 14.sp
                ),
                color = cs.primary
            )
        } else {
            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = "附件",
                tint = if (enabled) cs.onSurfaceVariant else cs.onSurfaceVariant.copy(alpha = 0.38f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun CircleActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDesc: String,
    bgColor: Color,
    fgColor: Color,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(bgColor)
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDesc,
            tint = fgColor,
            modifier = Modifier.size(18.dp)
        )
    }
}
