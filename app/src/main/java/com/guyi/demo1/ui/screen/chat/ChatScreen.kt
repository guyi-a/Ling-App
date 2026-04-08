package com.guyi.demo1.ui.screen.chat

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.guyi.demo1.LingAgentApplication
import com.guyi.demo1.ui.components.*
import dev.jeziellago.compose.markdowntext.MarkdownText
import kotlinx.serialization.json.JsonPrimitive

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    sessionId: String? = null,
    initialMessage: String? = null,
    onBackClick: () -> Unit = {},
    onMenuClick: () -> Unit = {},
    onWorkspaceClick: (sessionId: String) -> Unit = {}
) {
    val context = LocalContext.current
    val appContainer = (context.applicationContext as LingAgentApplication).container
    val viewModel: ChatViewModel = viewModel(
        factory = ChatViewModelFactory(
            appContainer.chatApi,
            appContainer.sessionRepository,
            appContainer.messageRepository,
            appContainer.sseManager,
            sessionId
        )
    )

    val uiState by viewModel.uiState.collectAsState()
    var inputText by remember { mutableStateOf("") }
    var attachments by remember { mutableStateOf<List<Attachment>>(emptyList()) }
    var showFilePicker by remember { mutableStateOf(false) }
    var editingMessage by remember { mutableStateOf<com.guyi.demo1.data.model.Message?>(null) }
    var editText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }

    // 发送初始消息
    LaunchedEffect(initialMessage) {
        if (initialMessage != null && !uiState.isStreaming) {
            viewModel.sendMessage(initialMessage)
        }
    }

    // 自动滚动到最新消息
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            if (sessionId == null) "新对话" else "AI 助手",
                            fontWeight = FontWeight.Bold
                        )
                        uiState.currentSessionId?.let {
                            Text(
                                "会话 #${it.take(8)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            "返回"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { uiState.currentSessionId?.let { onWorkspaceClick(it) } },
                        enabled = uiState.currentSessionId != null
                    ) {
                        Icon(Icons.Default.FolderOpen, "工作区")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // 消息列表
            Box(modifier = Modifier.weight(1f)) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 空状态提示
                    if (uiState.messages.isEmpty()) {
                        item {
                            EmptyState(
                                icon = "💬",
                                title = "开始对话",
                                description = "向我提问任何问题，我会尽力帮助你！"
                            )
                        }
                    }

                    items(uiState.messages, key = { it.id }) { message ->
                        val isLastAssistant = message.role == "assistant" &&
                            message == uiState.messages.lastOrNull { it.role == "assistant" }
                        MessageBubble(
                            message = message,
                            isLastAssistantMessage = isLastAssistant,
                            isStreaming = uiState.isStreaming,
                            onCopy = { text ->
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("message", text))
                            },
                            onDelete = { msg ->
                                msg.messageId?.let { viewModel.deleteMessage(it) }
                            },
                            onRegenerate = {
                                viewModel.regenerateLastMessage()
                            },
                            onEdit = { msg ->
                                editingMessage = msg
                                editText = msg.parts
                                    .filter { it.type == "text" }
                                    .mapNotNull { it.content }
                                    .joinToString("\n")
                            }
                        )
                    }
                }

            }

            // 附件列表
            if (attachments.isNotEmpty()) {
                AttachmentList(
                    attachments = attachments,
                    onRemove = { attachment ->
                        attachments = attachments.filter { it.id != attachment.id }
                    }
                )
            }

            // 输入框
            ChatInputBar(
                text = inputText,
                onTextChange = { inputText = it },
                isEnabled = !uiState.isStreaming,
                isStreaming = uiState.isStreaming,
                attachmentCount = attachments.size,
                onAttachClick = {
                    if (uiState.currentSessionId != null) {
                        showFilePicker = true
                    }
                },
                onSendClick = {
                    if (inputText.isNotBlank()) {
                        val attachmentData = attachments.map { att ->
                            mapOf(
                                "type" to if (att.type == AttachmentType.IMAGE) "image" else "file",
                                "path" to att.path,
                                "size" to att.size.toString()
                            )
                        }
                        viewModel.sendMessage(inputText, attachmentData.ifEmpty { null })
                        inputText = ""
                        attachments = emptyList()
                    }
                },
                onStopClick = { viewModel.stopStreaming() }
            )
        }
    }

    // 显示错误
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    // 显示审批对话框
    val messagesWithApproval = uiState.messages.filter { it.approvalRequest != null }
    val pendingApproval = messagesWithApproval.lastOrNull()?.approvalRequest

    pendingApproval?.let { approval ->
        ToolApprovalDialog(
            request = ToolApprovalRequest(
                requestId = approval.requestId,
                toolName = approval.toolName,
                toolInput = approval.toolInput.mapValues { (_, value) ->
                    // 将 JsonElement 转换为字符串显示
                    when (value) {
                        is JsonPrimitive -> value.content
                        else -> value.toString()
                    }
                },
                description = "Agent 需要执行以下工具"
            ),
            onApprove = {
                viewModel.approveToolUse(approval.requestId, true)
            },
            onReject = {
                viewModel.approveToolUse(approval.requestId, false)
            }
        )
    }

    // 编辑消息对话框
    editingMessage?.let { msg ->
        AlertDialog(
            onDismissRequest = { editingMessage = null },
            title = { Text("编辑消息") },
            text = {
                OutlinedTextField(
                    value = editText,
                    onValueChange = { editText = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (editText.isNotBlank()) {
                        viewModel.editAndResend(msg, editText)
                    }
                    editingMessage = null
                }) {
                    Text("发送")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingMessage = null }) {
                    Text("取消")
                }
            }
        )
    }

    // 工作区文件选择对话框
    if (showFilePicker && uiState.currentSessionId != null) {
        WorkspaceFilePickerDialog(
            sessionId = uiState.currentSessionId!!,
            workspaceRepository = appContainer.workspaceRepository,
            selectedPaths = attachments.map { it.path }.toSet(),
            onFileToggle = { file ->
                val existing = attachments.find { it.path == file.path }
                if (existing != null) {
                    attachments = attachments.filter { it.path != file.path }
                } else {
                    val ext = file.name.substringAfterLast('.', "").lowercase()
                    val type = when (ext) {
                        "png", "jpg", "jpeg", "gif", "webp" -> AttachmentType.IMAGE
                        "pdf" -> AttachmentType.PDF
                        "csv", "xlsx", "xls" -> AttachmentType.CSV
                        else -> AttachmentType.FILE
                    }
                    attachments = attachments + Attachment(
                        id = file.path,
                        type = type,
                        name = file.name,
                        path = file.path,
                        size = file.size
                    )
                }
            },
            onDismiss = { showFilePicker = false }
        )
    }
}

/**
 * 消息气泡
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageBubble(
    message: com.guyi.demo1.data.model.Message,
    isLastAssistantMessage: Boolean = false,
    isStreaming: Boolean = false,
    onCopy: (String) -> Unit = {},
    onDelete: (com.guyi.demo1.data.model.Message) -> Unit = {},
    onRegenerate: () -> Unit = {},
    onEdit: (com.guyi.demo1.data.model.Message) -> Unit = {}
) {
    val isUser = message.role == "user"
    var showMenu by remember { mutableStateOf(false) }

    val textContent = message.parts
        .filter { it.type == "text" }
        .mapNotNull { it.content }
        .joinToString("\n")

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Box {
            if (isUser) {
                // 用户消息：右对齐小气泡
                Surface(
                    shape = RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier
                        .widthIn(max = 280.dp)
                        .combinedClickable(
                            onClick = {},
                            onLongClick = { if (!isStreaming) showMenu = true }
                        )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        message.parts.forEach { part ->
                            if (part.type == "text") {
                                part.content?.let { content ->
                                    Text(
                                        text = content,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // AI 消息：左对齐宽布局，浅色背景
                Surface(
                    shape = RoundedCornerShape(4.dp, 16.dp, 16.dp, 16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .combinedClickable(
                            onClick = {},
                            onLongClick = { if (!isStreaming && !message.isStreaming) showMenu = true }
                        )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        message.parts.forEach { part ->
                            when (part.type) {
                                "text" -> {
                                    part.content?.trimEnd()?.takeIf { it.isNotEmpty() }?.let { content ->
                                        MarkdownText(
                                            markdown = content,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        )
                                    }
                                }
                                "tool" -> {
                                    part.toolName?.let { toolName ->
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.surface,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 2.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                            ) {
                                                when (part.toolStatus) {
                                                    "pending" -> CircularProgressIndicator(
                                                        modifier = Modifier.size(16.dp),
                                                        strokeWidth = 2.dp,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                    "done" -> Icon(
                                                        Icons.Filled.CheckCircle,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(16.dp),
                                                        tint = Color(0xFF4CAF50)
                                                    )
                                                    else -> {}
                                                }
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = toolName,
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        fontFamily = FontFamily.Monospace,
                                                        fontWeight = FontWeight.Medium
                                                    ),
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = when (part.toolStatus) {
                                                        "pending" -> "执行中..."
                                                        "done" -> "完成"
                                                        "rejected" -> "已拒绝"
                                                        else -> ""
                                                    },
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // 流式生成指示器
                        if (message.isStreaming) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(12.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "生成中...",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // 长按操作菜单
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                // 复制
                DropdownMenuItem(
                    text = { Text("复制") },
                    leadingIcon = { Icon(Icons.Default.ContentCopy, null) },
                    onClick = {
                        onCopy(textContent)
                        showMenu = false
                    }
                )
                // 编辑（仅用户消息）
                if (isUser) {
                    DropdownMenuItem(
                        text = { Text("编辑") },
                        leadingIcon = { Icon(Icons.Default.Edit, null) },
                        onClick = {
                            onEdit(message)
                            showMenu = false
                        }
                    )
                }
                // 重新生成（仅最后一条 AI 消息）
                if (!isUser && isLastAssistantMessage) {
                    DropdownMenuItem(
                        text = { Text("重新生成") },
                        leadingIcon = { Icon(Icons.Default.Refresh, null) },
                        onClick = {
                            onRegenerate()
                            showMenu = false
                        }
                    )
                }
                // 删除
                if (message.messageId != null) {
                    DropdownMenuItem(
                        text = { Text("删除", color = MaterialTheme.colorScheme.error) },
                        leadingIcon = {
                            Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
                        },
                        onClick = {
                            onDelete(message)
                            showMenu = false
                        }
                    )
                }
            }
        }
    }
}
