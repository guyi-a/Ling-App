package com.guyi.demo1.ui.screen.workspace

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.guyi.demo1.ui.components.*

/**
 * 工作区文件数据类
 */
data class WorkspaceFile(
    val id: String,
    val name: String,
    val path: String,
    val folder: String, // "uploads" or "outputs"
    val size: Long,
    val modifiedAt: String,
    val type: FileType
)

enum class FileType {
    IMAGE, PDF, CSV, TEXT, OTHER
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkspaceScreen(
    sessionId: String,
    sessionTitle: String = "当前会话",
    onBackClick: () -> Unit = {}
) {
    var showUploadDialog by remember { mutableStateOf(false) }
    var selectedFile by remember { mutableStateOf<WorkspaceFile?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // 模拟文件数据
    val uploadedFiles = remember {
        listOf(
            WorkspaceFile(
                id = "1",
                name = "sales_data.csv",
                path = "uploads/sales_data.csv",
                folder = "uploads",
                size = 15678,
                modifiedAt = "今天 10:23",
                type = FileType.CSV
            ),
            WorkspaceFile(
                id = "2",
                name = "chart_screenshot.png",
                path = "uploads/chart_screenshot.png",
                folder = "uploads",
                size = 239872,
                modifiedAt = "今天 10:25",
                type = FileType.IMAGE
            ),
            WorkspaceFile(
                id = "3",
                name = "document.pdf",
                path = "uploads/document.pdf",
                folder = "uploads",
                size = 1024567,
                modifiedAt = "昨天 15:30",
                type = FileType.PDF
            )
        )
    }

    val generatedFiles = remember {
        listOf(
            WorkspaceFile(
                id = "4",
                name = "sales_trend.png",
                path = "outputs/sales_trend.png",
                folder = "outputs",
                size = 467890,
                modifiedAt = "今天 10:30",
                type = FileType.IMAGE
            ),
            WorkspaceFile(
                id = "5",
                name = "analysis_report.pdf",
                path = "outputs/analysis_report.pdf",
                folder = "outputs",
                size = 1234567,
                modifiedAt = "今天 10:35",
                type = FileType.PDF
            ),
            WorkspaceFile(
                id = "6",
                name = "cleaned_data.csv",
                path = "outputs/cleaned_data.csv",
                folder = "outputs",
                size = 23456,
                modifiedAt = "今天 11:00",
                type = FileType.CSV
            ),
            WorkspaceFile(
                id = "7",
                name = "summary.txt",
                path = "outputs/summary.txt",
                folder = "outputs",
                size = 2345,
                modifiedAt = "今天 11:15",
                type = FileType.TEXT
            ),
            WorkspaceFile(
                id = "8",
                name = "visualization.png",
                path = "outputs/visualization.png",
                folder = "outputs",
                size = 567890,
                modifiedAt = "今天 11:20",
                type = FileType.IMAGE
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "工作区文件",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "会话: $sessionTitle",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { showUploadDialog = true }) {
                        Icon(Icons.Default.CloudUpload, "上传文件")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 上传文件部分
            item {
                SectionHeader(
                    icon = "📤",
                    title = "上传文件",
                    count = uploadedFiles.size
                )
            }

            if (uploadedFiles.isEmpty()) {
                item {
                    EmptyStateCompact(
                        icon = "📁",
                        message = "暂无上传文件"
                    )
                }
            } else {
                items(uploadedFiles, key = { it.id }) { file ->
                    WorkspaceFileCard(
                        file = file,
                        onPreviewClick = {
                            selectedFile = file
                            // TODO: 显示预览
                        },
                        onDownloadClick = {
                            // TODO: 下载文件
                        },
                        onDeleteClick = {
                            selectedFile = file
                            showDeleteDialog = true
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }

            // 生成文件部分
            item {
                SectionHeader(
                    icon = "📥",
                    title = "生成文件",
                    count = generatedFiles.size
                )
            }

            if (generatedFiles.isEmpty()) {
                item {
                    EmptyStateCompact(
                        icon = "📂",
                        message = "暂无生成文件"
                    )
                }
            } else {
                items(generatedFiles, key = { it.id }) { file ->
                    WorkspaceFileCard(
                        file = file,
                        onPreviewClick = {
                            selectedFile = file
                            // TODO: 显示预览
                        },
                        onDownloadClick = {
                            // TODO: 下载文件
                        },
                        onDeleteClick = {
                            selectedFile = file
                            showDeleteDialog = true
                        }
                    )
                }
            }

            // 底部上传按钮
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { showUploadDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "上传文件",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    // 删除确认对话框
    if (showDeleteDialog && selectedFile != null) {
        DeleteConfirmDialog(
            itemName = selectedFile!!.name,
            onConfirm = {
                // TODO: 实际删除逻辑
                showDeleteDialog = false
                selectedFile = null
            },
            onDismiss = {
                showDeleteDialog = false
                selectedFile = null
            }
        )
    }

    // 上传文件对话框
    if (showUploadDialog) {
        InfoDialog(
            title = "上传文件",
            message = "文件上传功能将在后续版本中实现。\n\n支持的文件类型：\n• 图片（PNG, JPG）\n• 数据（CSV, Excel）\n• 文档（PDF, TXT）",
            confirmText = "知道了",
            onConfirm = { showUploadDialog = false }
        )
    }
}

@Composable
fun SectionHeader(
    icon: String,
    title: String,
    count: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = icon,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(end = 8.dp)
        )

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.width(8.dp))

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
fun WorkspaceFileCard(
    file: WorkspaceFile,
    onPreviewClick: () -> Unit,
    onDownloadClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 文件图标
                Icon(
                    getFileIcon(file.type),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = getFileIconColor(file.type)
                )

                Spacer(modifier = Modifier.width(16.dp))

                // 文件信息
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = file.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatFileSize(file.size),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = " • ",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = file.modifiedAt,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 操作按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 预览按钮
                if (file.type == FileType.IMAGE || file.type == FileType.PDF) {
                    OutlinedButton(
                        onClick = onPreviewClick,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            Icons.Default.RemoveRedEye,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("预览")
                    }
                }

                // 下载按钮
                OutlinedButton(
                    onClick = onDownloadClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        Icons.Default.FileDownload,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("下载")
                }

                // 删除按钮
                OutlinedButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        Icons.Outlined.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("删除")
                }
            }
        }
    }
}

/**
 * 根据文件类型获取图标
 */
private fun getFileIcon(type: FileType) = when (type) {
    FileType.IMAGE -> Icons.Default.Image
    FileType.PDF -> Icons.Default.PictureAsPdf
    FileType.CSV -> Icons.Default.TableChart
    FileType.TEXT -> Icons.Default.Description
    FileType.OTHER -> Icons.Default.InsertDriveFile
}

/**
 * 根据文件类型获取图标颜色
 */
@Composable
private fun getFileIconColor(type: FileType) = when (type) {
    FileType.IMAGE -> MaterialTheme.colorScheme.primary
    FileType.PDF -> MaterialTheme.colorScheme.error
    FileType.CSV -> MaterialTheme.colorScheme.tertiary
    FileType.TEXT -> MaterialTheme.colorScheme.secondary
    FileType.OTHER -> MaterialTheme.colorScheme.onSurfaceVariant
}

/**
 * 格式化文件大小
 */
private fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> String.format("%.1f KB", bytes / 1024.0)
        bytes < 1024 * 1024 * 1024 -> String.format("%.1f MB", bytes / (1024.0 * 1024.0))
        else -> String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0))
    }
}
