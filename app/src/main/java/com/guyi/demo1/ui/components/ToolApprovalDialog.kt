package com.guyi.demo1.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.guyi.demo1.ui.theme.LingTheme

/** 工具审批数据 */
data class ToolApprovalRequest(
    val requestId: String,
    val toolName: String,
    val toolInput: Map<String, Any>,
    val description: String = ""
)

/**
 * 工具审批弹窗 — Warm Calm 重做
 *   · 顶部圆形 primary 容器 + 工具类型图标
 *   · 工具名（等宽字体）
 *   · 参数列表（可滚动，可展开折叠）
 *   · 自定义 checkbox：始终允许此工具
 *   · 安全警告（error 竖条 + 文案）
 *   · 拒绝（文字按钮）+ 允许（primary 实色按钮）
 */
@Composable
fun ToolApprovalDialog(
    request: ToolApprovalRequest,
    onApprove: (alwaysAllow: Boolean) -> Unit,
    onReject: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val shapes = LingTheme.shapes
    var alwaysAllow by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onReject,
        containerColor = cs.surface,
        shape = shapes.lg,
        icon = {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(cs.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getToolIcon(request.toolName),
                    contentDescription = null,
                    tint = cs.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
        },
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "工具调用请求",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = cs.onSurface
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (request.description.isNotBlank()) request.description else "Agent 想要执行下列工具",
                    style = MaterialTheme.typography.bodySmall.copy(lineHeight = 16.sp),
                    color = cs.onSurfaceVariant
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // 工具名称行
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(shapes.sm)
                        .background(cs.surfaceVariant.copy(alpha = 0.5f))
                        .border(
                            width = 1.dp,
                            color = cs.outlineVariant.copy(alpha = 0.6f),
                            shape = shapes.sm
                        )
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "工具",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.sp,
                            lineHeight = 12.sp
                        ),
                        color = cs.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = request.toolName,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = cs.primary
                    )
                }

                // 参数列表
                if (request.toolInput.isNotEmpty()) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "执行参数",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 12.sp
                        ),
                        color = cs.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Spacer(Modifier.height(6.dp))
                    Surface(
                        shape = shapes.sm,
                        color = cs.surface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 220.dp)
                            .border(
                                width = 1.dp,
                                color = cs.outlineVariant.copy(alpha = 0.6f),
                                shape = shapes.sm
                            )
                    ) {
                        Column(
                            modifier = Modifier
                                .verticalScroll(rememberScrollState())
                                .padding(12.dp)
                        ) {
                            request.toolInput.entries.forEachIndexed { idx, (k, v) ->
                                if (idx > 0) Spacer(Modifier.height(8.dp))
                                ParameterItem(k, v.toString())
                            }
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                // 始终允许 checkbox
                AlwaysAllowCheck(
                    checked = alwaysAllow,
                    onCheckedChange = { alwaysAllow = it }
                )

                Spacer(Modifier.height(10.dp))

                // 安全警告
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .size(width = 3.dp, height = 28.dp)
                            .background(cs.error.copy(alpha = 0.6f))
                    )
                    Spacer(Modifier.width(10.dp))
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Outlined.WarningAmber,
                            contentDescription = null,
                            tint = cs.error.copy(alpha = 0.8f),
                            modifier = Modifier.size(14.dp).padding(top = 2.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = getToolWarning(request.toolName),
                            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                            color = cs.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            Box(
                modifier = Modifier
                    .clip(shapes.pill)
                    .background(cs.primary)
                    .clickable { onApprove(alwaysAllow) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "允许执行",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = cs.onPrimary
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onReject) {
                Text(
                    text = "拒绝",
                    color = cs.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    )
}

@Composable
private fun ParameterItem(key: String, value: String) {
    val cs = MaterialTheme.colorScheme
    Column {
        Text(
            text = key,
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 12.sp
            ),
            color = cs.primary
        )
        Spacer(Modifier.height(4.dp))
        val display = if (value.length > 240) value.take(240) + "…" else value
        Text(
            text = display,
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = FontFamily.Monospace,
                lineHeight = 16.sp
            ),
            color = cs.onSurface.copy(alpha = 0.85f),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(cs.surfaceVariant.copy(alpha = 0.5f))
                .padding(horizontal = 8.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun AlwaysAllowCheck(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val borderColor by animateColorAsState(
        targetValue = if (checked) cs.primary else cs.outline,
        animationSpec = tween(160),
        label = "ckbBorder"
    )
    val fillColor by animateColorAsState(
        targetValue = if (checked) cs.primary else Color.Transparent,
        animationSpec = tween(160),
        label = "ckbFill"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(fillColor)
                .border(width = 1.5.dp, color = borderColor, shape = RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (checked) {
                Icon(
                    imageVector = Icons.Outlined.Check,
                    contentDescription = null,
                    tint = cs.onPrimary,
                    modifier = Modifier.size(10.dp)
                )
            }
        }
        Spacer(Modifier.width(10.dp))
        Text(
            text = "始终允许此工具",
            style = MaterialTheme.typography.bodySmall,
            color = cs.onSurface
        )
    }
}

/** 工具图标（按工具名前缀映射） */
private fun getToolIcon(toolName: String): ImageVector = when {
    toolName.contains("python", ignoreCase = true) -> Icons.Outlined.Code
    toolName.contains("command", ignoreCase = true) -> Icons.Outlined.Terminal
    toolName.contains("write", ignoreCase = true) -> Icons.Outlined.Edit
    toolName.contains("read", ignoreCase = true) -> Icons.Outlined.Description
    toolName.contains("search", ignoreCase = true) -> Icons.Outlined.Search
    toolName.contains("file", ignoreCase = true) -> Icons.Outlined.Description
    else -> Icons.Outlined.Build
}

private fun getToolWarning(toolName: String): String = when {
    toolName.contains("python", ignoreCase = true) ->
        "此操作将执行 Python 代码，请确认代码内容安全后再允许。"
    toolName.contains("command", ignoreCase = true) ->
        "此操作将执行系统命令，可能修改文件或系统设置。"
    toolName.contains("write", ignoreCase = true) ->
        "此操作将写入文件，请确认操作内容正确。"
    toolName.contains("dev_run", ignoreCase = true) ->
        "此操作将启动后台进程，会占用工作区端口。"
    else -> "请仔细检查参数内容后再允许执行。"
}

/** 工具执行中状态卡（保留接口兼容） */
@Composable
fun ToolExecutingCard(
    toolName: String,
    modifier: Modifier = Modifier
) {
    val cs = MaterialTheme.colorScheme
    val shapes = LingTheme.shapes
    Surface(
        shape = shapes.sm,
        color = cs.primary.copy(alpha = 0.08f),
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = cs.primary.copy(alpha = 0.25f),
                shape = shapes.sm
            )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(14.dp),
                strokeWidth = 2.dp,
                color = cs.primary
            )
            Spacer(Modifier.width(10.dp))
            Column {
                Text(
                    text = "正在执行",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.sp,
                        lineHeight = 12.sp
                    ),
                    color = cs.onSurfaceVariant.copy(alpha = 0.7f)
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = toolName,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = cs.primary
                )
            }
        }
    }
}
