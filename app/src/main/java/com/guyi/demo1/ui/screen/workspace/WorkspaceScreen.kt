package com.guyi.demo1.ui.screen.workspace

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.DriveFileMove
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.InsertDriveFile
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.TableChart
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.guyi.demo1.LingAgentApplication
import com.guyi.demo1.data.model.TreeEntry
import com.guyi.demo1.ui.components.DeleteConfirmDialog
import com.guyi.demo1.ui.theme.LingTheme

enum class FileType {
    IMAGE, PDF, CSV, TEXT, CODE, OTHER
}

/**
 * 工作区 — Warm Calm 重做
 *   · 自定义顶栏：返回 + 标题双行 + 刷新 / 上传圆形按钮
 *   · 面包屑：圆角胶囊 + 小人字线分隔
 *   · 目录 / 文件 item：描边卡片 + 圆形图标容器
 *   · 文件类型配色温润化（避免刺眼红绿）
 *   · 空目录：圆形容器 icon + 说明 + 上传 CTA
 *
 * 全部原交互保留（ViewModel、上传、下载、删除、导航）
 */
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
    var entryToDelete by remember { mutableStateOf<TreeEntry?>(null) }
    val cs = MaterialTheme.colorScheme
    val space = LingTheme.spacing

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? -> uri?.let { viewModel.uploadFile(it) } }

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

    Surface(color = cs.background, modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                // ─── 顶栏 ───
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconCircleButton(
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDesc = "返回",
                        onClick = onBackClick
                    )
                    Spacer(Modifier.width(4.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "工作区",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                lineHeight = 18.sp
                            ),
                            color = cs.onSurface
                        )
                        Spacer(Modifier.height(1.dp))
                        Text(
                            text = sessionTitle,
                            style = MaterialTheme.typography.labelSmall.copy(lineHeight = 12.sp),
                            color = cs.onSurfaceVariant.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconCircleButton(
                        icon = Icons.Outlined.Refresh,
                        contentDesc = "刷新",
                        enabled = !uiState.isLoading,
                        onClick = { viewModel.loadTree() }
                    )
                    Spacer(Modifier.width(4.dp))
                    UploadButton(
                        isUploading = uiState.isUploading,
                        onClick = { filePickerLauncher.launch(arrayOf("*/*")) }
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(cs.outlineVariant.copy(alpha = 0.5f))
                )

                // ─── 面包屑 ───
                Breadcrumb(
                    pathStack = uiState.pathStack,
                    onNavigateTo = { index -> viewModel.navigateTo(index) }
                )

                // ─── 主体 ───
                when {
                    uiState.isLoading && uiState.entries.isEmpty() -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                color = cs.primary,
                                strokeWidth = 2.5.dp,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                    uiState.entries.isEmpty() -> {
                        EmptyDirectory(
                            onUpload = { filePickerLauncher.launch(arrayOf("*/*")) }
                        )
                    }
                    else -> {
                        val dirs = uiState.entries.filter { it.type == "dir" }.sortedBy { it.name }
                        val files = uiState.entries.filter { it.type == "file" }.sortedBy { it.name }

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = space.pageHorizontal,
                                end = space.pageHorizontal,
                                top = space.sm,
                                bottom = space.xxl
                            ),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (uiState.pathStack.size > 1) {
                                item(key = "__parent__") {
                                    ParentDirectoryItem(onClick = { viewModel.navigateUp() })
                                }
                            }
                            items(dirs, key = { "dir_${it.path}" }) { dir ->
                                DirectoryItem(
                                    entry = dir,
                                    onClick = { viewModel.navigateInto(dir) }
                                )
                            }
                            items(files, key = { "file_${it.path}" }) { file ->
                                FileItem(
                                    entry = file,
                                    onDownloadClick = { viewModel.downloadFile(file) },
                                    onDeleteClick = { entryToDelete = file }
                                )
                            }
                            item { Spacer(Modifier.navigationBarsPadding()) }
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = space.md)
                    .navigationBarsPadding()
            ) {
                SnackbarHost(snackbarHostState)
            }
        }
    }

    if (entryToDelete != null) {
        DeleteConfirmDialog(
            itemName = entryToDelete!!.name,
            onConfirm = {
                entryToDelete?.let { viewModel.deleteFile(it) }
                entryToDelete = null
            },
            onDismiss = { entryToDelete = null }
        )
    }
}

// =====================================================
//  顶栏按钮
// =====================================================

@Composable
private fun IconCircleButton(
    icon: ImageVector,
    contentDesc: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDesc,
            tint = if (enabled) cs.onSurface else cs.onSurface.copy(alpha = 0.35f),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun UploadButton(isUploading: Boolean, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(cs.primary.copy(alpha = 0.12f))
            .then(if (!isUploading) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        if (isUploading) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
                color = cs.primary
            )
        } else {
            Icon(
                imageVector = Icons.Outlined.CloudUpload,
                contentDescription = "上传文件",
                tint = cs.primary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// =====================================================
//  面包屑
// =====================================================

@Composable
private fun Breadcrumb(
    pathStack: List<String>,
    onNavigateTo: (Int) -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val scrollState = rememberScrollState()

    LaunchedEffect(pathStack.size) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(cs.surfaceVariant.copy(alpha = 0.35f))
            .horizontalScroll(scrollState)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        pathStack.forEachIndexed { index, path ->
            if (index > 0) {
                Icon(
                    imageVector = Icons.Outlined.KeyboardArrowRight,
                    contentDescription = null,
                    tint = cs.onSurfaceVariant.copy(alpha = 0.35f),
                    modifier = Modifier
                        .size(14.dp)
                        .padding(horizontal = 2.dp)
                )
            }
            val label = if (path == ".") "根目录" else path.substringAfterLast("/")
            val isLast = index == pathStack.lastIndex
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = if (isLast) FontWeight.SemiBold else FontWeight.Normal,
                    lineHeight = 16.sp
                ),
                color = if (isLast) cs.onSurface else cs.primary,
                modifier = if (!isLast) {
                    Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onNavigateTo(index) }
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                } else {
                    Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                }
            )
        }
    }
}

// =====================================================
//  列表项
// =====================================================

@Composable
private fun ParentDirectoryItem(onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val shapes = LingTheme.shapes
    Surface(
        shape = shapes.sm,
        color = cs.surfaceVariant.copy(alpha = 0.35f),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = cs.outlineVariant.copy(alpha = 0.6f),
                shape = shapes.sm
            )
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(cs.primary.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.ArrowUpward,
                    contentDescription = null,
                    tint = cs.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Text(
                text = "返回上级",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = cs.primary
            )
        }
    }
}

@Composable
private fun DirectoryItem(entry: TreeEntry, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val shapes = LingTheme.shapes
    Surface(
        shape = shapes.sm,
        color = cs.surface,
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = cs.outlineVariant, shape = shapes.sm)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(cs.primary.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Folder,
                    contentDescription = null,
                    tint = cs.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.name,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                        lineHeight = 18.sp
                    ),
                    color = cs.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val childCount = entry.children.size
                if (childCount > 0) {
                    val dirCount = entry.children.count { it.type == "dir" }
                    val fileCount = entry.children.count { it.type == "file" }
                    val parts = mutableListOf<String>()
                    if (dirCount > 0) parts.add("$dirCount 个文件夹")
                    if (fileCount > 0) parts.add("$fileCount 个文件")
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = parts.joinToString(" · "),
                        style = MaterialTheme.typography.labelSmall.copy(lineHeight = 12.sp),
                        color = cs.onSurfaceVariant.copy(alpha = 0.65f)
                    )
                }
            }
            Icon(
                imageVector = Icons.Outlined.KeyboardArrowRight,
                contentDescription = null,
                tint = cs.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun FileItem(
    entry: TreeEntry,
    onDownloadClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val shapes = LingTheme.shapes
    val fileType = getFileType(entry.name)
    val iconTint = fileIconTint(fileType, cs)
    var showMenu by remember { mutableStateOf(false) }

    Surface(
        shape = shapes.sm,
        color = cs.surface,
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = cs.outlineVariant, shape = shapes.sm)
            .clickable { showMenu = true }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconTint.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getFileIcon(fileType),
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.name,
                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 18.sp),
                    color = cs.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (entry.size > 0) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = formatFileSize(entry.size),
                        style = MaterialTheme.typography.labelSmall.copy(lineHeight = 12.sp),
                        color = cs.onSurfaceVariant.copy(alpha = 0.65f)
                    )
                }
            }
            Box {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .clickable { showMenu = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.MoreVert,
                        contentDescription = "操作",
                        tint = cs.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    shape = shapes.md,
                    containerColor = cs.surface
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                "下载",
                                style = MaterialTheme.typography.bodyMedium,
                                color = cs.onSurface
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.FileDownload,
                                contentDescription = null,
                                tint = cs.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        onClick = {
                            showMenu = false
                            onDownloadClick()
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                "删除",
                                style = MaterialTheme.typography.bodyMedium,
                                color = cs.error
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = null,
                                tint = cs.error,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        onClick = {
                            showMenu = false
                            onDeleteClick()
                        }
                    )
                }
            }
        }
    }
}

// =====================================================
//  空状态
// =====================================================

@Composable
private fun EmptyDirectory(onUpload: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val shapes = LingTheme.shapes
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(cs.primary.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.FolderOpen,
                    contentDescription = null,
                    tint = cs.primary,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(Modifier.height(14.dp))
            Text(
                text = "空目录",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = cs.onBackground
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "这里还没有文件，上传一个试试",
                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                color = cs.onSurfaceVariant.copy(alpha = 0.75f)
            )
            Spacer(Modifier.height(18.dp))
            Row(
                modifier = Modifier
                    .clip(shapes.pill)
                    .background(cs.primary)
                    .clickable(onClick = onUpload)
                    .padding(horizontal = 18.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.CloudUpload,
                    contentDescription = null,
                    tint = cs.onPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "上传文件",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = cs.onPrimary
                )
            }
        }
    }
}

// =====================================================
//  文件类型辅助
// =====================================================

private fun getFileType(fileName: String): FileType {
    val ext = fileName.substringAfterLast('.', "").lowercase()
    return when (ext) {
        "png", "jpg", "jpeg", "gif", "webp", "bmp", "svg" -> FileType.IMAGE
        "pdf" -> FileType.PDF
        "csv", "xlsx", "xls" -> FileType.CSV
        "txt", "md", "log", "yaml", "yml", "toml", "ini", "cfg" -> FileType.TEXT
        "py", "kt", "java", "js", "ts", "html", "css", "json", "xml", "sh", "sql", "go", "rs", "c", "cpp", "h" -> FileType.CODE
        else -> FileType.OTHER
    }
}

private fun getFileIcon(type: FileType): ImageVector = when (type) {
    FileType.IMAGE -> Icons.Outlined.Image
    FileType.PDF -> Icons.Outlined.PictureAsPdf
    FileType.CSV -> Icons.Outlined.TableChart
    FileType.TEXT -> Icons.Outlined.Description
    FileType.CODE -> Icons.Outlined.Code
    FileType.OTHER -> Icons.Outlined.InsertDriveFile
}

/** 温润化的文件类型配色：不用刺眼红绿 */
private fun fileIconTint(
    type: FileType,
    cs: androidx.compose.material3.ColorScheme
): Color = when (type) {
    FileType.IMAGE -> cs.primary          // 焦糖橙
    FileType.PDF -> cs.tertiary           // 陶土（不用刺眼红）
    FileType.CSV -> Color(0xFF5F8F5F)     // 苔藓绿
    FileType.TEXT -> cs.secondary
    FileType.CODE -> Color(0xFF4A6E7E)    // 雾蓝
    FileType.OTHER -> cs.onSurfaceVariant
}

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> String.format("%.1f KB", bytes / 1024.0)
        bytes < 1024L * 1024 * 1024 -> String.format("%.1f MB", bytes / (1024.0 * 1024.0))
        else -> String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0))
    }
}
