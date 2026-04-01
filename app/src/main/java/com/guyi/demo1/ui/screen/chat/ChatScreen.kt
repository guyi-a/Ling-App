package com.guyi.demo1.ui.screen.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
    onMenuClick: () -> Unit = {}
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
                    IconButton(onClick = { /* TODO: 显示菜单 */ }) {
                        Icon(Icons.Default.MoreVert, "更多")
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
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(messages, key = { it.id }) { message ->
                    MessageCard(message)
                }
            }

            // 输入框
            ChatInputBar(
                text = inputText,
                onTextChange = { inputText = it },
                onSendClick = {
                    if (inputText.isNotBlank()) {
                        val userMessage = ChatMessage(
                            id = System.currentTimeMillis().toString(),
                            content = inputText,
                            isUser = true
                        )
                        messages = messages + userMessage
                        inputText = ""

                        scope.launch {
                            listState.animateScrollToItem(messages.size - 1)
                        }

                        // 模拟 AI 回复
                        scope.launch {
                            kotlinx.coroutines.delay(1000)
                            val aiMessage = ChatMessage(
                                id = System.currentTimeMillis().toString(),
                                content = "收到！这是对「${userMessage.content}」的回复。\n\n我可以帮你：\n• 数据分析\n• 生成报告\n• 代码执行\n• 信息搜索",
                                isUser = false
                            )
                            messages = messages + aiMessage
                            listState.animateScrollToItem(messages.size - 1)
                        }
                    }
                }
            )
        }
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
    onSendClick: () -> Unit
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
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                TextField(
                    value = text,
                    onValueChange = onTextChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("输入消息...") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    ),
                    maxLines = 5
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            FilledTonalButton(
                onClick = onSendClick,
                enabled = text.isNotBlank(),
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
