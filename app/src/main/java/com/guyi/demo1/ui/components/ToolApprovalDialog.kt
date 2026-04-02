package com.guyi.demo1.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * 工具审批数据
 */
data class ToolApprovalRequest(
    val requestId: String,
    val toolName: String,
    val toolInput: Map<String, Any>,
    val description: String = ""
)

/**
 * 工具审批弹窗
 *
 * @param request 审批请求
 * @param onApprove 批准回调
 * @param onReject 拒绝回调
 */
@Composable
fun ToolApprovalDialog(
    request: ToolApprovalRequest,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onReject,
        icon = {
            Icon(
                getToolIcon(request.toolName),
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Column {
                Text(
                    text = "工具调用请求",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Agent 想要执行工具",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // 工具名称卡片
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = getToolEmoji(request.toolName),
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.padding(end = 12.dp)
                        )

                        Column {
                            Text(
                                text = request.toolName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.primary
                            )

                            if (request.description.isNotEmpty()) {
                                Text(
                                    text = request.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 参数展示
                if (request.toolInput.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "执行参数",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )

                        TextButton(
                            onClick = { isExpanded = !isExpanded }
                        ) {
                            Text(if (isExpanded) "收起" else "展开")
                        }
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = if (isExpanded) 300.dp else 120.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                                .padding(12.dp)
                        ) {
                            request.toolInput.forEach { (key, value) ->
                                ParameterItem(key, value.toString())
                                if (key != request.toolInput.keys.last()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 安全提示
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "⚠️",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(end = 8.dp)
                        )

                        Text(
                            text = getToolWarning(request.toolName),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onApprove,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    "允许执行",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onReject,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("拒绝")
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

/**
 * 参数项展示
 */
@Composable
private fun ParameterItem(
    key: String,
    value: String
) {
    Column {
        Text(
            text = key,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                    RoundedCornerShape(6.dp)
                )
                .padding(8.dp)
        )
    }
}

/**
 * 获取工具图标
 */
private fun getToolIcon(toolName: String): ImageVector {
    return when {
        toolName.contains("python", ignoreCase = true) -> Icons.Default.Code
        toolName.contains("command", ignoreCase = true) -> Icons.Default.Terminal
        toolName.contains("file", ignoreCase = true) -> Icons.Default.Description
        else -> Icons.Default.Code
    }
}

/**
 * 获取工具 Emoji
 */
private fun getToolEmoji(toolName: String): String {
    return when {
        toolName.contains("python", ignoreCase = true) -> "🐍"
        toolName.contains("command", ignoreCase = true) -> "💻"
        toolName.contains("file", ignoreCase = true) -> "📁"
        toolName.contains("write", ignoreCase = true) -> "✏️"
        toolName.contains("read", ignoreCase = true) -> "📖"
        toolName.contains("search", ignoreCase = true) -> "🔍"
        else -> "🔧"
    }
}

/**
 * 获取工具警告信息
 */
private fun getToolWarning(toolName: String): String {
    return when {
        toolName.contains("python", ignoreCase = true) ->
            "此操作将执行 Python 代码，请确认代码内容安全后再允许。"
        toolName.contains("command", ignoreCase = true) ->
            "此操作将执行系统命令，可能会修改文件或系统设置。"
        toolName.contains("write", ignoreCase = true) ->
            "此操作将写入文件，请确认操作内容正确。"
        else ->
            "请仔细检查参数内容后再允许执行。"
    }
}

/**
 * 工具执行中的状态卡片
 */
@Composable
fun ToolExecutingCard(
    toolName: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 3.dp,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = "正在执行工具",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = toolName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
