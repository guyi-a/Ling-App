package com.guyi.demo1.ui.screen.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.guyi.demo1.ui.components.*
import kotlinx.coroutines.launch

// 消息数据类
data class ChatMessage(
    val id: String,
    val content: String,
    val isUser: Boolean,
    val timestamp: String = "12:30"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    sessionId: String? = null,
    initialMessage: String? = null,
    onBackClick: () -> Unit = {},
    onMenuClick: () -> Unit = {},
    onWorkspaceClick: () -> Unit = {}
) {
    // 初始消息
    val initialMessages = remember(sessionId, initialMessage) {
        buildList {
            if (sessionId != null) {
                // 加载历史会话的消息
                add(ChatMessage("1", "之前的对话内容...", false))
            } else {
                // 新对话
                add(ChatMessage("1", "你好！我是 Ling Agent，有什么可以帮你的吗？", false))
                // 如果有初始消息，添加用户消息
                initialMessage?.let {
                    add(ChatMessage("2", it, true))
                }
            }
        }
    }

    var messages by remember { mutableStateOf<List<ChatMessage>>(initialMessages) }
    var inputText by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }
    var attachments by remember { mutableStateOf<List<Attachment>>(emptyList()) }
    var toolApprovalRequest by remember { mutableStateOf<ToolApprovalRequest?>(null) }
    var showMenu by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // 如果有初始消息，模拟 AI 回复
    LaunchedEffect(initialMessage) {
        if (initialMessage != null) {
            kotlinx.coroutines.delay(1000)
            val aiMessage = ChatMessage(
                id = "3",
                content = "收到！让我帮你处理「${initialMessage}」这个问题。\n\n我可以：\n• 提供详细的分析\n• 生成相关报告\n• 执行相关任务",
                isUser = false
            )
            messages = messages + aiMessage
            listState.animateScrollToItem(messages.size - 1)
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
                        if (sessionId != null) {
                            Text(
                                "会话 #$sessionId",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = if (sessionId != null) onBackClick else onMenuClick) {
                        Icon(
                            if (sessionId != null) Icons.AutoMirrored.Filled.ArrowBack else Icons.Default.Menu,
                            if (sessionId != null) "返回" else "菜单"
                        )
                    }
                },
                actions = {
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
                                    messages = emptyList()
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
                    if (messages.isEmpty()) {
                        item {
                            EmptyState(
                                icon = "💬",
                                title = "开始对话",
                                description = "向我提问任何问题，我会尽力帮助你！"
                            )
                        }
                    }

                    items(messages, key = { it.id }) { message ->
                        MessageCard(message)
                    }

                    // 生成中提示
                    if (isGenerating) {
                        item {
                            GeneratingIndicator(
                                message = "正在生成回复...",
                                onStop = {
                                    isGenerating = false
                                    // TODO: 调用停止 API
                                }
                            )
                        }
                    }
                }

                // 停止生成浮动按钮
                if (isGenerating) {
                    StopGenerationButton(
                        onClick = {
                            isGenerating = false
                            // TODO: 调用停止 API
                        },
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
                isEnabled = !isGenerating,
                attachmentCount = attachments.size,
                onAttachClick = {
                    // TODO: 打开文件选择器
                    // 模拟添加附件
                    val newAttachment = Attachment(
                        id = System.currentTimeMillis().toString(),
                        type = AttachmentType.IMAGE,
                        name = "example_${attachments.size + 1}.png",
                        path = "uploads/example.png",
                        size = 1024 * 234
                    )
                    attachments = attachments + newAttachment
                },
                onSendClick = {
                    if (inputText.isNotBlank()) {
                        val userMessage = ChatMessage(
                            id = System.currentTimeMillis().toString(),
                            content = inputText,
                            isUser = true
                        )
                        messages = messages + userMessage
                        inputText = ""
                        attachments = emptyList()
                        isGenerating = true

                        scope.launch {
                            listState.animateScrollToItem(messages.size - 1)
                        }

                        // 模拟 AI 回复
                        scope.launch {
                            kotlinx.coroutines.delay(2000)
                            isGenerating = false
                            val aiMessage = ChatMessage(
                                id = System.currentTimeMillis().toString(),
                                content = "收到！这是对「${userMessage.content}」的回复。\n\n我可以帮你：\n• 数据分析\n• 生成报告\n• 代码执行\n• 信息搜索",
                                isUser = false
                            )
                            messages = messages + aiMessage
                            listState.animateScrollToItem(messages.size - 1)
                        }

                        // 模拟工具审批请求（演示用）
                        scope.launch {
                            kotlinx.coroutines.delay(3000)
                            if (userMessage.content.contains("代码", ignoreCase = true)) {
                                toolApprovalRequest = ToolApprovalRequest(
                                    requestId = "req_${System.currentTimeMillis()}",
                                    toolName = "python_repl",
                                    toolInput = mapOf(
                                        "code" to "import pandas as pd\ndf = pd.read_csv('data.csv')\nprint(df.head())"
                                    ),
                                    description = "执行 Python 代码进行数据分析"
                                )
                            }
                        }
                    }
                }
            )
        }
    }

    // 工具审批弹窗
    toolApprovalRequest?.let { request ->
        ToolApprovalDialog(
            request = request,
            onApprove = {
                toolApprovalRequest = null
                // TODO: 调用审批 API
            },
            onReject = {
                toolApprovalRequest = null
                // TODO: 调用审批 API
            }
        )
    }
}

@Composable
fun MessageCard(message: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier.widthIn(max = 300.dp),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (message.isUser) 16.dp else 4.dp,
                bottomEnd = if (message.isUser) 4.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (message.isUser) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (message.isUser) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = message.timestamp,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (message.isUser) {
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    },
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

@Composable
fun ChatInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSendClick: () -> Unit,
    isEnabled: Boolean = true,
    attachmentCount: Int = 0,
    onAttachClick: () -> Unit = {}
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            // 附件按钮
            IconButton(
                onClick = onAttachClick,
                enabled = isEnabled,
                modifier = Modifier.size(48.dp)
            ) {
                BadgedBox(
                    badge = {
                        if (attachmentCount > 0) {
                            Badge(
                                containerColor = MaterialTheme.colorScheme.primary
                            ) {
                                Text(attachmentCount.toString())
                            }
                        }
                    }
                ) {
                    Icon(
                        Icons.Default.AttachFile,
                        contentDescription = "添加附件",
                        tint = if (isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // 输入框
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                BasicTextField(
                    value = text,
                    onValueChange = onTextChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    enabled = isEnabled,
                    maxLines = 5,
                    decorationBox = { innerTextField ->
                        if (text.isEmpty()) {
                            Text(
                                text = if (isEnabled) "输入消息..." else "AI 正在生成中...",
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                        innerTextField()
                    }
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // 发送按钮
            FilledTonalButton(
                onClick = onSendClick,
                enabled = text.isNotBlank() && isEnabled,
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    contentDescription = "发送",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
