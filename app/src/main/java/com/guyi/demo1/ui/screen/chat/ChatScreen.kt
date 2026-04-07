package com.guyi.demo1.ui.screen.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.guyi.demo1.LingAgentApplication
import com.guyi.demo1.ui.components.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonPrimitive

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    sessionId: String? = null,
    initialMessage: String? = null,
    onBackClick: () -> Unit = {},
    onMenuClick: () -> Unit = {},
    onWorkspaceClick: () -> Unit = {}
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
    var showMenu by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

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
                    // 测试审批按钮（调试用）
                    TextButton(onClick = { viewModel.testApproval() }) {
                        Text("测试审批")
                    }

                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, "更多")
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("查看工作区") },
                                onClick = {
                                    showMenu = false
                                    onWorkspaceClick()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.FolderOpen, contentDescription = null)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("清空对话") },
                                onClick = {
                                    showMenu = false
                                    // TODO: 实现清空对话
                                },
                                leadingIcon = {
                                    Icon(Icons.Outlined.Delete, contentDescription = null)
                                }
                            )
                        }
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
                        MessageBubble(message)
                    }
                }

                // 停止生成浮动按钮
                if (uiState.isStreaming) {
                    StopGenerationButton(
                        onClick = { viewModel.stopStreaming() },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 16.dp)
                    )
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
                attachmentCount = attachments.size,
                onAttachClick = {
                    // TODO: 打开文件选择器
                },
                onSendClick = {
                    if (inputText.isNotBlank()) {
                        viewModel.sendMessage(inputText, attachments)
                        inputText = ""
                        attachments = emptyList()
                    }
                }
            )
        }
    }

    // 显示错误
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // TODO: 显示 Snackbar 错误提示
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
}

/**
 * 消息气泡
 */
@Composable
fun MessageBubble(message: com.guyi.demo1.data.model.Message) {
    val isUser = message.role == "user"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = if (isUser) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                // 渲染消息内容
                message.parts.forEach { part ->
                    when (part.type) {
                        "text" -> {
                            part.content?.let { content ->
                                Text(
                                    text = content,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isUser) {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                        }
                        "tool" -> {
                            part.toolName?.let { toolName ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    when (part.toolStatus) {
                                        "pending" -> CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp
                                        )
                                        "done" -> Text("✓", color = MaterialTheme.colorScheme.primary)
                                        else -> {}
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = toolName,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
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
}
