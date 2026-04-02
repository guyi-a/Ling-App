package com.guyi.demo1.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

// 会话数据类
data class SessionItem(
    val id: String,
    val title: String,
    val lastMessage: String,
    val timestamp: String,
    val messageCount: Int,
    val timeGroup: TimeGroup = TimeGroup.OLDER
)

// 时间分组枚举
enum class TimeGroup(val label: String) {
    TODAY("今天"),
    YESTERDAY("昨天"),
    THIS_WEEK("本周"),
    OLDER("更早")
}

@Composable
fun DrawerContent(
    onSessionClick: (String) -> Unit = {},
    onNewChatClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    // 搜索关键词
    var searchQuery by remember { mutableStateOf("") }

    // 模拟会话列表数据
    val allSessions = remember {
        listOf(
            SessionItem(
                id = "1",
                title = "数据分析讨论",
                lastMessage = "帮我分析一下销售数据的趋势...",
                timestamp = "今天 10:30",
                messageCount = 15,
                timeGroup = TimeGroup.TODAY
            ),
            SessionItem(
                id = "2",
                title = "代码优化建议",
                lastMessage = "这段代码可以怎么优化？",
                timestamp = "今天 09:15",
                messageCount = 8,
                timeGroup = TimeGroup.TODAY
            ),
            SessionItem(
                id = "3",
                title = "报告生成",
                lastMessage = "生成一份季度销售报告",
                timestamp = "昨天 15:20",
                messageCount = 23,
                timeGroup = TimeGroup.YESTERDAY
            ),
            SessionItem(
                id = "4",
                title = "Python 编程问题",
                lastMessage = "如何使用 pandas 处理缺失值？",
                timestamp = "昨天 11:05",
                messageCount = 12,
                timeGroup = TimeGroup.YESTERDAY
            ),
            SessionItem(
                id = "5",
                title = "新闻搜索",
                lastMessage = "搜索最近的AI技术新闻",
                timestamp = "3天前",
                messageCount = 6,
                timeGroup = TimeGroup.THIS_WEEK
            ),
            SessionItem(
                id = "6",
                title = "创意头脑风暴",
                lastMessage = "帮我想一些营销创意",
                timestamp = "1周前",
                messageCount = 10,
                timeGroup = TimeGroup.OLDER
            ),
            SessionItem(
                id = "7",
                title = "机器学习入门",
                lastMessage = "推荐一些机器学习的学习资源",
                timestamp = "2周前",
                messageCount = 18,
                timeGroup = TimeGroup.OLDER
            )
        )
    }

    // 根据搜索过滤会话
    val filteredSessions = remember(searchQuery, allSessions) {
        if (searchQuery.isBlank()) {
            allSessions
        } else {
            allSessions.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                        it.lastMessage.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    // 按时间分组
    val groupedSessions = remember(filteredSessions) {
        filteredSessions.groupBy { it.timeGroup }
    }

    ModalDrawerSheet(
        modifier = Modifier.width(320.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 抽屉头部
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Card(
                            modifier = Modifier.size(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Ling Agent",
                                    modifier = Modifier.size(28.dp),
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "Ling Agent",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                "对话历史",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            // 搜索框
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        decorationBox = { innerTextField ->
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "搜索会话...",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                            innerTextField()
                        }
                    )

                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { searchQuery = "" },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = "清除",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // 新建对话按钮
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clickable(onClick = onNewChatClick),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.AddCircle,
                        contentDescription = "新建对话",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "新建对话",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            // 历史会话列表（分组显示）
            if (filteredSessions.isEmpty()) {
                // 空状态
                EmptyStateCompact(
                    icon = if (searchQuery.isBlank()) "📭" else "🔍",
                    message = if (searchQuery.isBlank()) "暂无会话记录" else "未找到匹配的会话"
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 按时间分组展示
                    TimeGroup.entries.forEach { group ->
                        val sessionsInGroup = groupedSessions[group] ?: emptyList()
                        if (sessionsInGroup.isNotEmpty()) {
                            // 分组标题
                            item(key = "header_$group") {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 4.dp, top = 8.dp, bottom = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${group.label} (${sessionsInGroup.size})",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // 分组内的会话列表
                            items(sessionsInGroup, key = { it.id }) { session ->
                                SessionDrawerItem(
                                    session = session,
                                    onClick = { onSessionClick(session.id) }
                                )
                            }
                        }
                    }
                }
            }

            HorizontalDivider()

            // 底部设置按钮
            TextButton(
                onClick = onSettingsClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = "设置",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("设置")
            }
        }
    }
}

@Composable
fun SessionDrawerItem(
    session: SessionItem,
    onClick: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = session.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = session.lastMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = session.timestamp,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = "${session.messageCount} 条",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            IconButton(
                onClick = { showDeleteDialog = true },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Outlined.Delete,
                    contentDescription = "删除",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }

    // 删除确认对话框
    if (showDeleteDialog) {
        DeleteConfirmDialog(
            itemName = session.title,
            onConfirm = {
                // TODO: 实际删除逻辑
                showDeleteDialog = false
            },
            onDismiss = {
                showDeleteDialog = false
            }
        )
    }
}
