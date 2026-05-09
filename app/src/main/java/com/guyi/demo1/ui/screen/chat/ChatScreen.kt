package com.guyi.demo1.ui.screen.chat

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.guyi.demo1.LingAgentApplication
import com.guyi.demo1.ui.components.*
import dev.jeziellago.compose.markdowntext.MarkdownText
import kotlinx.coroutines.launch
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
    var isUploading by remember { mutableStateOf(false) }
    var editingMessage by remember { mutableStateOf<com.guyi.demo1.data.model.Message?>(null) }
    var editText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = androidx.compose.runtime.rememberCoroutineScope()

    // 上传文件启动器（从设备本地选文件 → 上传到工作区 → 加入附件列表）
    val fileUploadLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        val sid = uiState.currentSessionId
        if (uri != null && sid != null) {
            scope.launch {
                isUploading = true
                val result = appContainer.workspaceRepository.uploadFile(sid, uri)
                result.onSuccess { resp ->
                    val ext = resp.filename.substringAfterLast('.', "").lowercase()
                    val type = when (ext) {
                        "png", "jpg", "jpeg", "gif", "webp" -> AttachmentType.IMAGE
                        "pdf" -> AttachmentType.PDF
                        "csv", "xlsx", "xls" -> AttachmentType.CSV
                        else -> AttachmentType.FILE
                    }
                    attachments = attachments + Attachment(
                        id = resp.path,
                        type = type,
                        name = resp.filename,
                        path = resp.path,
                        size = resp.size
                    )
                }.onFailure { e ->
                    snackbarHostState.showSnackbar("上传失败：${e.message ?: "未知错误"}")
                }
                isUploading = false
            }
        }
    }

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
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // 自定义顶栏
            ChatTopBar(
                sessionId = uiState.currentSessionId,
                isNewChat = sessionId == null,
                onBackClick = onBackClick,
                onWorkspaceClick = {
                    uiState.currentSessionId?.let { onWorkspaceClick(it) }
                }
            )
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
                    val sid = uiState.currentSessionId
                    if (sid != null && !isUploading) {
                        fileUploadLauncher.launch("*/*")
                    } else if (sid == null) {
                        scope.launch {
                            snackbarHostState.showSnackbar("请先发一条消息建立会话后再上传文件")
                        }
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
            onApprove = { alwaysAllow ->
                viewModel.approveToolUse(approval.requestId, true, alwaysAllow)
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
            containerColor = MaterialTheme.colorScheme.surface,
            shape = com.guyi.demo1.ui.theme.LingTheme.shapes.lg,
            title = {
                Text(
                    "编辑消息",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                OutlinedTextField(
                    value = editText,
                    onValueChange = { editText = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 8,
                    shape = com.guyi.demo1.ui.theme.LingTheme.shapes.sm,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (editText.isNotBlank()) {
                        viewModel.editAndResend(msg, editText)
                    }
                    editingMessage = null
                }) {
                    Text(
                        "发送",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { editingMessage = null }) {
                    Text(
                        "取消",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
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

    val cs = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Box {
            if (isUser) {
                // 用户气泡：primary 实色 + 白字
                Surface(
                    shape = RoundedCornerShape(18.dp, 18.dp, 4.dp, 18.dp),
                    color = cs.primary,
                    modifier = Modifier
                        .widthIn(max = 280.dp)
                        .combinedClickable(
                            onClick = {},
                            onLongClick = { if (!isStreaming) showMenu = true }
                        )
                ) {
                    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                        message.parts.forEach { part ->
                            if (part.type == "text") {
                                part.content?.let { content ->
                                    Text(
                                        text = content,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = cs.onPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // AI 气泡：surface 底 + outlineVariant 描边（与工具卡视觉统一）
                Surface(
                    shape = RoundedCornerShape(4.dp, 18.dp, 18.dp, 18.dp),
                    color = cs.surface,
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .border(
                            width = 1.dp,
                            color = cs.outlineVariant,
                            shape = RoundedCornerShape(4.dp, 18.dp, 18.dp, 18.dp)
                        )
                        .combinedClickable(
                            onClick = {},
                            onLongClick = { if (!isStreaming && !message.isStreaming) showMenu = true }
                        )
                ) {
                    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                        message.parts.forEach { part ->
                            when (part.type) {
                                "text" -> {
                                    part.content?.trimEnd()?.takeIf { it.isNotEmpty() }?.let { content ->
                                        MarkdownText(
                                            markdown = content,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                color = cs.onSurface,
                                                lineHeight = 22.sp
                                            )
                                        )
                                    }
                                }
                                "tool" -> {
                                    part.toolName?.let {
                                        ToolCard(part = part)
                                    }
                                }
                                "handoff" -> {
                                    part.agentName?.let { agentName ->
                                        HandoffBadge(
                                            agentName = agentName,
                                            direction = part.handoffDirection
                                        )
                                    }
                                }
                            }
                        }

                        // 流式指示器
                        if (message.isStreaming) {
                            Spacer(Modifier.height(6.dp))
                            StreamingDots()
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

// =====================================================
//  工具卡片 — 支持展开查看输入 / 输出
// =====================================================

@Composable
private fun ToolCard(part: com.guyi.demo1.data.model.MessagePart) {
    val cs = MaterialTheme.colorScheme
    val toolName = part.toolName ?: return

    // Skill 的真实名取 toolInput["command"]（与 Web 端一致）；否则用 toolName
    val displayName = if (part.isSkill) {
        val skill = (part.toolInput?.get("command") as? kotlinx.serialization.json.JsonPrimitive)?.content
        skill ?: toolName
    } else {
        toolName
    }

    // done 状态的工具都可展开（即使入参和输出都为空，也给用户"空展示"确认）
    val canExpand = part.toolStatus == "done"
    var expanded by remember { mutableStateOf(false) }
    val arrowRotation by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (expanded) 90f else 0f,
        label = "toolArrow"
    )

    val statusText = when (part.toolStatus) {
        "pending" -> if (part.isSkill) "加载中..." else "执行中..."
        "generating" -> if (part.isSkill) "加载中..." else "生成中..."
        "done" -> if (part.isSkill) "加载完成" else "调用成功"
        "rejected" -> "等待审批"
        "cancelled" -> "已取消"
        else -> ""
    }

    Column(modifier = Modifier.padding(vertical = 2.dp)) {
        Surface(
            shape = RoundedCornerShape(9.dp),
            color = cs.surface,
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = cs.outlineVariant,
                    shape = RoundedCornerShape(9.dp)
                )
                .then(
                    if (canExpand) Modifier.clickable { expanded = !expanded } else Modifier
                )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val iconAccent = when (part.toolStatus) {
                    "rejected", "cancelled" -> cs.onSurfaceVariant
                    else -> cs.primary
                }
                when (part.toolStatus) {
                    "pending", "generating" -> CircularProgressIndicator(
                        modifier = Modifier.size(12.dp),
                        strokeWidth = 1.8.dp,
                        color = iconAccent
                    )
                    "done" -> Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = iconAccent
                    )
                    "rejected", "cancelled" -> Icon(
                        Icons.Outlined.Close,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = iconAccent
                    )
                    else -> Spacer(Modifier.size(14.dp))
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 16.sp
                    ),
                    color = cs.onSurface
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.labelSmall.copy(lineHeight = 14.sp),
                    color = cs.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                if (canExpand) {
                    Icon(
                        Icons.Outlined.ChevronRight,
                        contentDescription = if (expanded) "折叠" else "展开",
                        modifier = Modifier.size(13.dp).rotate(arrowRotation),
                        tint = cs.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }

        if (expanded && canExpand) {
            Spacer(Modifier.height(4.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = cs.surface,
                modifier = Modifier.fillMaxWidth().border(
                    width = 1.dp,
                    color = cs.outlineVariant.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(8.dp)
                )
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    val hasInput = part.toolInput != null && part.toolInput.isNotEmpty()
                    val rawOutput = part.toolOutput ?: ""

                    // 输入
                    Text(
                        text = "输入",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = cs.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Spacer(Modifier.height(4.dp))
                    if (hasInput) {
                        Text(
                            text = formatToolInput(part.toolInput!!),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                lineHeight = 16.sp
                            ),
                            color = cs.onSurface.copy(alpha = 0.85f)
                        )
                    } else {
                        Text(
                            text = "（无输入参数）",
                            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 16.sp),
                            color = cs.onSurfaceVariant.copy(alpha = 0.55f)
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    // 输出（总是显示，空时用占位）
                    Text(
                        text = "输出",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = cs.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Spacer(Modifier.height(4.dp))
                    val trimmed = rawOutput.trim()
                    if (trimmed.isEmpty()) {
                        Text(
                            text = "（工具未返回内容）",
                            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 16.sp),
                            color = cs.onSurfaceVariant.copy(alpha = 0.55f)
                        )
                    } else {
                        Text(
                            text = if (trimmed.length > 800) trimmed.take(800) + "\n…（已截断）" else trimmed,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                lineHeight = 16.sp
                            ),
                            color = cs.onSurface.copy(alpha = 0.85f)
                        )
                    }
                }
            }
        }
    }
}

private fun formatToolInput(input: kotlinx.serialization.json.JsonObject): String {
    return input.entries.joinToString("\n") { (k, v) ->
        val valueStr = when (v) {
            is kotlinx.serialization.json.JsonPrimitive -> v.content
            else -> v.toString()
        }
        val shown = if (valueStr.length > 200) valueStr.take(200) + "…" else valueStr
        "$k: $shown"
    }
}

// =====================================================
//  Chat 顶栏（自定义 Warm Calm 风格）
// =====================================================

@Composable
private fun ChatTopBar(
    sessionId: String?,
    isNewChat: Boolean,
    onBackClick: () -> Unit,
    onWorkspaceClick: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ChatIconButton(
            icon = Icons.AutoMirrored.Filled.ArrowBack,
            contentDesc = "返回",
            onClick = onBackClick
        )
        Spacer(Modifier.width(4.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (isNewChat) "新对话" else "对话中",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 18.sp
                ),
                color = cs.onSurface
            )
            if (sessionId != null) {
                Spacer(Modifier.height(1.dp))
                Text(
                    text = "# ${sessionId.take(8)}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.5.sp,
                        lineHeight = 12.sp
                    ),
                    color = cs.onSurfaceVariant.copy(alpha = 0.65f)
                )
            }
        }
        ChatIconButton(
            icon = Icons.Default.FolderOpen,
            contentDesc = "工作区",
            enabled = sessionId != null,
            onClick = onWorkspaceClick
        )
    }
    // 细分隔线
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(cs.outlineVariant.copy(alpha = 0.5f))
    )
}

// =====================================================
//  Handoff Badge — Agent 切换指示
// =====================================================

@Composable
private fun HandoffBadge(agentName: String, direction: String?) {
    val cs = MaterialTheme.colorScheme
    val label = when (agentName) {
        "general" -> "通用助手"
        "developer" -> "开发者"
        "psych" -> "心理顾问"
        "data" -> "数据分析"
        "document" -> "文档处理"
        "supervisor" -> "Supervisor"
        else -> agentName
    }
    val display = if (direction == "back") "← $label" else label
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = cs.tertiary.copy(alpha = 0.12f),
        modifier = Modifier
            .padding(vertical = 4.dp)
            .border(
                width = 1.dp,
                color = cs.tertiary.copy(alpha = 0.3f),
                shape = RoundedCornerShape(999.dp)
            )
    ) {
        Text(
            text = display,
            style = MaterialTheme.typography.labelSmall.copy(
                letterSpacing = 0.5.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 12.sp
            ),
            color = cs.tertiary,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

// =====================================================
//  流式生成指示器 — 三个跳动的点
// =====================================================

@Composable
private fun StreamingDots() {
    val cs = MaterialTheme.colorScheme
    val infinite = androidx.compose.animation.core.rememberInfiniteTransition(label = "dots")
    Row(verticalAlignment = Alignment.CenterVertically) {
        listOf(0, 200, 400).forEachIndexed { index, delay ->
            val alpha by infinite.animateFloat(
                initialValue = 0.25f,
                targetValue = 1f,
                animationSpec = androidx.compose.animation.core.infiniteRepeatable(
                    animation = androidx.compose.animation.core.tween(600, delayMillis = delay),
                    repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
                ),
                label = "dot$index"
            )
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(cs.primary.copy(alpha = alpha))
            )
            if (index < 2) Spacer(Modifier.width(4.dp))
        }
    }
}

@Composable
private fun ChatIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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
