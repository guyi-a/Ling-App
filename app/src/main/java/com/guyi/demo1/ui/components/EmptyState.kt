package com.guyi.demo1.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 空状态组件 — Warm Calm 风格
 *
 * 旧接口保持：icon 参数是 emoji 字符串。当 icon == "💬" 时用 ChatBubbleOutline，
 * 否则默认用 Inbox 图标。主要让聊天页空状态有精致视感。
 */
@Composable
fun EmptyState(
    icon: String = "📭",
    title: String,
    description: String,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val cs = MaterialTheme.colorScheme
    val iconVec = when (icon) {
        "💬" -> Icons.Outlined.ChatBubbleOutline
        else -> Icons.Outlined.Inbox
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 图标容器（圆形 primary 透明底 + 主色图标）
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(cs.primary.copy(alpha = 0.10f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = iconVec,
                contentDescription = null,
                tint = cs.primary,
                modifier = Modifier.size(26.dp)
            )
        }

        Spacer(Modifier.height(14.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            textAlign = TextAlign.Center,
            color = cs.onBackground
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
            textAlign = TextAlign.Center,
            color = cs.onSurfaceVariant.copy(alpha = 0.8f)
        )

        if (actionText != null && onAction != null) {
            Spacer(Modifier.height(16.dp))
            LingPrimaryButton(
                text = actionText,
                onClick = onAction,
                showArrow = true,
                modifier = Modifier.fillMaxWidth(0.7f)
            )
        }
    }
}

/**
 * 紧凑空状态 — 用于侧边栏等小范围场景
 */
@Composable
fun EmptyStateCompact(
    icon: String = "📭",
    message: String,
    modifier: Modifier = Modifier
) {
    val cs = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(cs.surfaceVariant.copy(alpha = 0.6f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Inbox,
                contentDescription = null,
                tint = cs.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
            textAlign = TextAlign.Center,
            color = cs.onSurfaceVariant.copy(alpha = 0.75f)
        )
    }
}
