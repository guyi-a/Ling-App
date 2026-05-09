package com.guyi.demo1.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.TableChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.guyi.demo1.ui.theme.LingTheme

/** 附件类型 */
enum class AttachmentType {
    IMAGE, PDF, CSV, FILE
}

/** 附件数据 */
data class Attachment(
    val id: String,
    val type: AttachmentType,
    val name: String,
    val path: String,
    val size: Long = 0,
    val thumbnailUrl: String? = null
)

/**
 * 紧凑型附件 chip — 输入区上方横向展示
 *  · 圆角胶囊 + 类型 accent 图标 + 文件名 + 删除按钮
 */
@Composable
fun AttachmentCard(
    attachment: Attachment,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cs = MaterialTheme.colorScheme
    val shapes = LingTheme.shapes
    val accent = attachmentAccent(attachment.type, cs)

    Surface(
        shape = shapes.sm,
        color = cs.surface,
        modifier = modifier
            .height(40.dp)
            .border(width = 1.dp, color = cs.outlineVariant, shape = shapes.sm)
    ) {
        Row(
            modifier = Modifier.padding(start = 8.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getFileIcon(attachment.type),
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(13.dp)
                )
            }
            Spacer(Modifier.width(8.dp))
            Column {
                Text(
                    text = attachment.name,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Medium,
                        lineHeight = 14.sp
                    ),
                    color = cs.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.widthIn(max = 140.dp)
                )
                if (attachment.size > 0) {
                    Text(
                        text = formatFileSize(attachment.size),
                        style = MaterialTheme.typography.labelSmall.copy(lineHeight = 11.sp),
                        color = cs.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
            Spacer(Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onRemove),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "移除",
                    tint = cs.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

/** 附件横向滚动列表（输入框上方） */
@Composable
fun AttachmentList(
    attachments: List<Attachment>,
    onRemove: (Attachment) -> Unit,
    modifier: Modifier = Modifier
) {
    if (attachments.isEmpty()) return
    val scroll = rememberScrollState()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scroll)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        attachments.forEach { att ->
            AttachmentCard(
                attachment = att,
                onRemove = { onRemove(att) }
            )
        }
    }
}

/**
 * 消息内附件预览 — 列表项风格（点击查看）
 */
@Composable
fun MessageAttachmentPreview(
    attachment: Attachment,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cs = MaterialTheme.colorScheme
    val shapes = LingTheme.shapes
    val accent = attachmentAccent(attachment.type, cs)

    Surface(
        shape = shapes.sm,
        color = cs.surface,
        modifier = modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = cs.outlineVariant, shape = shapes.sm)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accent.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getFileIcon(attachment.type),
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = attachment.name,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = cs.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (attachment.size > 0) {
                    Text(
                        text = formatFileSize(attachment.size),
                        style = MaterialTheme.typography.labelSmall.copy(lineHeight = 12.sp),
                        color = cs.onSurfaceVariant.copy(alpha = 0.65f)
                    )
                }
            }
            Text(
                text = "查看",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = cs.primary
            )
        }
    }
}

// =====================================================
//  helpers
// =====================================================

private fun getFileIcon(type: AttachmentType): ImageVector = when (type) {
    AttachmentType.IMAGE -> Icons.Outlined.Image
    AttachmentType.PDF -> Icons.Outlined.PictureAsPdf
    AttachmentType.CSV -> Icons.Outlined.TableChart
    AttachmentType.FILE -> Icons.Outlined.Description
}

private fun attachmentAccent(
    type: AttachmentType,
    cs: androidx.compose.material3.ColorScheme
): Color = when (type) {
    AttachmentType.IMAGE -> cs.primary
    AttachmentType.PDF -> cs.tertiary
    AttachmentType.CSV -> Color(0xFF5F8F5F)
    AttachmentType.FILE -> cs.onSurfaceVariant
}

private fun formatFileSize(bytes: Long): String = when {
    bytes < 1024 -> "$bytes B"
    bytes < 1024 * 1024 -> "${bytes / 1024} KB"
    bytes < 1024L * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
    else -> "${bytes / (1024L * 1024 * 1024)} GB"
}
