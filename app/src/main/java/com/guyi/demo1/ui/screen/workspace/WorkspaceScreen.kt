package com.guyi.demo1.ui.screen.workspace

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.guyi.demo1.LingAgentApplication
import com.guyi.demo1.data.model.WorkspaceFile
import com.guyi.demo1.ui.components.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    val context = LocalContext.current
    val appContainer = (context.applicationContext as LingAgentApplication).container
    val viewModel: WorkspaceViewModel = viewModel(
        factory = WorkspaceViewModelFactory(
            appContainer.workspaceRepository,
            context,
            sessionId
        )
    )

    val uiState by viewModel.uiState.collectAsState()
    var selectedFile by remember { mutableStateOf<WorkspaceFile?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // 文件选择器
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { viewModel.uploadFile(it) }
    }

    // 显示提示
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSuccessMessage()
        }
    }
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                    IconButton(
                        onClick = { viewModel.loadFiles() },
                        enabled = !uiState.isLoading
                    ) {
                        Icon(Icons.Default.Refresh, "刷新")
                    }
                    IconButton(
                        onClick = { filePickerLauncher.launch(arrayOf("*/*")) },
                        enabled = !uiState.isUploading
                    ) {
                        if (uiState.isUploading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.CloudUpload, "上传文件")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading && uiState.uploads.isEmpty() && uiState.outputs.isEmpty()) {
            // 初始加载
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
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
                        count = uiState.uploads.size
                    )
                }

                if (uiState.uploads.isEmpty()) {
                    item {
                        EmptyStateCompact(
                            icon = "📁",
                            message = "暂无上传文件"
                        )
                    }
                } else {
                    items(uiState.uploads, key = { it.path }) { file ->
                        WorkspaceFileCard(
                            file = file,
                            onDownloadClick = {
                                viewModel.downloadFile(file)
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
                        count = uiState.outputs.size
                    )
                }

                if (uiState.outputs.isEmpty()) {
                    item {
                        EmptyStateCompact(
                            icon = "📂",
                            message = "暂无生成文件"
                        )
                    }
                } else {
                    items(uiState.outputs, key = { it.path }) { file ->
                        WorkspaceFileCard(
                            file = file,
                            onDownloadClick = {
                                viewModel.downloadFile(file)
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
                        onClick = { filePickerLauncher.launch(arrayOf("*/*")) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        enabled = !uiState.isUploading
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (uiState.isUploading) "上传中..." else "上传文件",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    // 删除确认对话框
    if (showDeleteDialog && selectedFile != null) {
        DeleteConfirmDialog(
            itemName = selectedFile!!.name,
            onConfirm = {
                selectedFile?.let { viewModel.deleteFile(it) }
                showDeleteDialog = false
                selectedFile = null
            },
            onDismiss = {
                showDeleteDialog = false
                selectedFile = null
            }
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
    onDownloadClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val fileType = getFileType(file.name)

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
                    getFileIcon(fileType),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = getFileIconColor(fileType)
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
                            text = formatTimestamp(file.modifiedAt),
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
 * 根据文件名判断类型
 */
private fun getFileType(fileName: String): FileType {
    val ext = fileName.substringAfterLast('.', "").lowercase()
    return when (ext) {
        "png", "jpg", "jpeg", "gif", "webp", "bmp" -> FileType.IMAGE
        "pdf" -> FileType.PDF
        "csv", "xlsx", "xls" -> FileType.CSV
        "txt", "md", "json", "xml", "log" -> FileType.TEXT
        else -> FileType.OTHER
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

/**
 * 格式化 Unix 时间戳
 */
private fun formatTimestamp(timestamp: Double): String {
    if (timestamp == 0.0) return ""
    val date = Date((timestamp * 1000).toLong())
    val format = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    return format.format(date)
}
