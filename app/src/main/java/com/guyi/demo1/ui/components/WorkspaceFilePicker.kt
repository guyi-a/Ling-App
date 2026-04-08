package com.guyi.demo1.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.guyi.demo1.data.model.WorkspaceFile
import com.guyi.demo1.data.repository.WorkspaceRepository
import kotlinx.coroutines.launch

/**
 * 工作区文件选择对话框
 * 从工作区选择文件附加到消息
 */
@Composable
fun WorkspaceFilePickerDialog(
    sessionId: String,
    workspaceRepository: WorkspaceRepository,
    selectedPaths: Set<String>,
    onFileToggle: (WorkspaceFile) -> Unit,
    onDismiss: () -> Unit
) {
    var files by remember { mutableStateOf<List<WorkspaceFile>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(sessionId) {
        isLoading = true
        val result = workspaceRepository.getFiles(sessionId)
        result.onSuccess { response ->
            files = response.files
        }
        isLoading = false
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择工作区文件", fontWeight = FontWeight.Bold) },
        text = {
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (files.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "工作区暂无文件",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val uploads = files.filter { it.folder == "uploads" }
                    val outputs = files.filter { it.folder == "outputs" }

                    if (uploads.isNotEmpty()) {
                        item {
                            Text(
                                "上传文件",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                        items(uploads) { file ->
                            FilePickerItem(
                                file = file,
                                isSelected = selectedPaths.contains(file.path),
                                onClick = { onFileToggle(file) }
                            )
                        }
                    }

                    if (outputs.isNotEmpty()) {
                        item {
                            Text(
                                "生成文件",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                            )
                        }
                        items(outputs) { file ->
                            FilePickerItem(
                                file = file,
                                isSelected = selectedPaths.contains(file.path),
                                onClick = { onFileToggle(file) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("完成")
            }
        }
    )
}

@Composable
private fun FilePickerItem(
    file: WorkspaceFile,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val ext = file.name.substringAfterLast('.', "").lowercase()
    val icon = when (ext) {
        "png", "jpg", "jpeg", "gif", "webp" -> Icons.Default.Image
        "pdf" -> Icons.Default.PictureAsPdf
        "csv", "xlsx", "xls" -> Icons.Default.TableChart
        else -> Icons.Default.Description
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = file.name,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = file.folder,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (isSelected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
