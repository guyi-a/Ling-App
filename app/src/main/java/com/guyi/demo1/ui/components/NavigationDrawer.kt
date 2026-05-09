package com.guyi.demo1.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.guyi.demo1.LingAgentApplication
import com.guyi.demo1.data.model.AdhocSession
import com.guyi.demo1.data.model.Project
import com.guyi.demo1.ui.theme.LingTheme
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * 侧边抽屉 — Warm Calm 重做
 *
 * 视觉骨架：
 *   · 顶部：Logo + 应用名 + 副标题（无大色块，纯净）
 *   · 搜索：自定义 inline 输入框（surfaceVariant 半透 + 圆角）
 *   · 新建对话：列表项风格（不用大色块按钮）
 *   · 项目区 / 临时对话区：可折叠 SectionHeader + 卡片列表
 *   · 底部：设置（普通列表项）
 */
@Composable
fun DrawerContent(
    onSessionClick: (String) -> Unit = {},
    onNewChatClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onProjectClick: ((Int) -> Unit)? = null
) {
    val context = LocalContext.current
    val appContainer = (context.applicationContext as LingAgentApplication).container
    val projectRepository = appContainer.projectRepository
    val sessionRepository = appContainer.sessionRepository
    val scope = rememberCoroutineScope()
    val cs = MaterialTheme.colorScheme
    val space = LingTheme.spacing
    val shapes = LingTheme.shapes

    var projects by remember { mutableStateOf<List<Project>>(emptyList()) }
    var adhocSessions by remember { mutableStateOf<List<AdhocSession>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    var projectsExpanded by remember { mutableStateOf(true) }
    var adhocExpanded by remember { mutableStateOf(true) }

    fun loadData() {
        scope.launch {
            isLoading = true
            projectRepository.getProjects().onSuccess { list ->
                projects = list.sortedByDescending { it.lastActiveAt ?: it.updatedAt ?: it.createdAt }
            }
            projectRepository.getAdhocSessions().onSuccess { list ->
                adhocSessions = list.sortedByDescending { it.updatedAt }
            }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) { loadData() }

    fun deleteAdhocSession(sessionId: String) {
        scope.launch {
            sessionRepository.deleteSession(sessionId, hardDelete = true)
            adhocSessions = adhocSessions.filter { it.sessionId != sessionId }
        }
    }

    val filteredProjects = remember(searchQuery, projects) {
        if (searchQuery.isBlank()) projects
        else projects.filter { (it.title ?: "").contains(searchQuery, ignoreCase = true) }
    }
    val filteredAdhoc = remember(searchQuery, adhocSessions) {
        if (searchQuery.isBlank()) adhocSessions
        else adhocSessions.filter { (it.title ?: "").contains(searchQuery, ignoreCase = true) }
    }

    ModalDrawerSheet(
        modifier = Modifier.width(320.dp),
        drawerContainerColor = cs.background,
        drawerTonalElevation = 0.dp,
        drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ─── 头部 ───
            DrawerHeader()

            Spacer(Modifier.height(space.md))

            // ─── 搜索框 ───
            DrawerSearchField(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                modifier = Modifier.padding(horizontal = space.md)
            )

            Spacer(Modifier.height(space.xs))

            // ─── 新建对话（列表项风格）───
            DrawerActionRow(
                icon = Icons.Outlined.Add,
                label = "新建对话",
                accent = cs.primary,
                onClick = onNewChatClick,
                modifier = Modifier.padding(horizontal = space.xs)
            )

            Spacer(Modifier.height(space.xs))
            ThinDivider(modifier = Modifier.padding(horizontal = space.md))

            // ─── 主列表 ───
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(28.dp),
                            color = cs.primary,
                            strokeWidth = 2.5.dp
                        )
                    }
                }
                filteredProjects.isEmpty() && filteredAdhoc.isEmpty() -> {
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        DrawerEmptyState(
                            isSearching = searchQuery.isNotBlank()
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(
                            start = space.xs,
                            end = space.xs,
                            top = space.xs,
                            bottom = space.md
                        ),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        if (filteredProjects.isNotEmpty()) {
                            item(key = "projects_header") {
                                SectionHeader(
                                    title = "项目",
                                    count = filteredProjects.size,
                                    expanded = projectsExpanded,
                                    onToggle = { projectsExpanded = !projectsExpanded }
                                )
                            }
                            if (projectsExpanded) {
                                items(filteredProjects, key = { "project_${it.id}" }) { project ->
                                    ProjectDrawerItem(
                                        project = project,
                                        onClick = { onProjectClick?.invoke(project.id) }
                                    )
                                }
                            }
                        }

                        if (filteredAdhoc.isNotEmpty()) {
                            item(key = "adhoc_header") {
                                Spacer(Modifier.height(space.xs))
                                SectionHeader(
                                    title = "临时对话",
                                    count = filteredAdhoc.size,
                                    expanded = adhocExpanded,
                                    onToggle = { adhocExpanded = !adhocExpanded }
                                )
                            }
                            if (adhocExpanded) {
                                items(filteredAdhoc, key = { "adhoc_${it.sessionId}" }) { session ->
                                    AdhocSessionDrawerItem(
                                        session = session,
                                        onClick = { onSessionClick(session.sessionId) },
                                        onDelete = { deleteAdhocSession(session.sessionId) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            ThinDivider()

            // ─── 底部设置 ───
            DrawerActionRow(
                icon = Icons.Outlined.Settings,
                label = "设置",
                accent = cs.onSurfaceVariant,
                onClick = onSettingsClick,
                modifier = Modifier.padding(
                    horizontal = space.xs,
                    vertical = space.xs
                )
            )
        }
    }
}

// =====================================================
//  头部
// =====================================================

@Composable
private fun DrawerHeader() {
    val cs = MaterialTheme.colorScheme
    val space = LingTheme.spacing
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = space.lg, vertical = space.md)
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            LingLogo(size = 32.dp)
            Spacer(Modifier.width(4.dp))
            Text(
                text = "ing",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Normal,
                    letterSpacing = 0.5.sp
                ),
                color = cs.onBackground,
                modifier = Modifier.padding(bottom = 2.dp)
            )
        }
        Spacer(Modifier.height(2.dp))
        Text(
            text = "你的工作空间",
            style = MaterialTheme.typography.bodySmall.copy(letterSpacing = 1.sp),
            color = cs.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

// =====================================================
//  搜索框
// =====================================================

@Composable
private fun DrawerSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val cs = MaterialTheme.colorScheme
    val shapes = LingTheme.shapes
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .background(cs.surfaceVariant.copy(alpha = 0.5f), shapes.sm)
            .border(width = 1.dp, color = cs.outlineVariant.copy(alpha = 0.6f), shape = shapes.sm)
            .padding(horizontal = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                tint = cs.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(10.dp))
            Box(modifier = Modifier.weight(1f)) {
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    cursorBrush = SolidColor(cs.primary),
                    textStyle = LocalTextStyle.current.copy(
                        color = cs.onSurface,
                        fontSize = 14.sp
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                if (query.isEmpty()) {
                    Text(
                        text = "搜索项目或对话",
                        style = MaterialTheme.typography.bodyMedium,
                        color = cs.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
            if (query.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .clickable { onQueryChange("") },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Clear,
                        contentDescription = "清除",
                        tint = cs.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

// =====================================================
//  动作行 — 用于「新建对话」/「设置」/「退出登录」
// =====================================================

@Composable
private fun DrawerActionRow(
    icon: ImageVector,
    label: String,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cs = MaterialTheme.colorScheme
    val shapes = LingTheme.shapes
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shapes.sm)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(accent.copy(alpha = 0.10f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = accent,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
            color = cs.onSurface
        )
    }
}

// =====================================================
//  Section header
// =====================================================

@Composable
private fun SectionHeader(
    title: String,
    count: Int,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val shapes = LingTheme.shapes
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shapes.sm)
            .clickable(onClick = onToggle)
            .padding(start = 8.dp, end = 8.dp, top = 12.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (expanded) Icons.Outlined.ExpandMore else Icons.Outlined.ChevronRight,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = cs.onSurfaceVariant.copy(alpha = 0.6f)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium.copy(
                letterSpacing = 1.5.sp,
                lineHeight = 14.sp,
                fontWeight = FontWeight.SemiBold
            ),
            color = cs.onSurfaceVariant
        )
        Spacer(Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(cs.surfaceVariant.copy(alpha = 0.6f))
                .padding(horizontal = 7.dp, vertical = 1.dp)
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelSmall.copy(lineHeight = 12.sp),
                color = cs.onSurfaceVariant.copy(alpha = 0.85f)
            )
        }
    }
}

// =====================================================
//  项目卡片
// =====================================================

@Composable
fun ProjectDrawerItem(
    project: Project,
    onClick: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val shapes = LingTheme.shapes
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shapes.sm)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 项目图标
        val iconText = project.icon?.takeIf { !it.startsWith("__img__") } ?: ""
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(cs.primary.copy(alpha = 0.10f)),
            contentAlignment = Alignment.Center
        ) {
            if (iconText.isNotEmpty()) {
                Text(
                    text = iconText,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.wrapContentSize(Alignment.Center)
                )
            } else {
                Icon(
                    imageVector = Icons.Outlined.FolderOpen,
                    contentDescription = null,
                    tint = cs.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = project.title ?: "未命名项目",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = cs.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = formatSessionTime(
                        project.lastActiveAt ?: project.updatedAt ?: project.createdAt
                    ),
                    style = MaterialTheme.typography.labelSmall.copy(lineHeight = 12.sp),
                    color = cs.onSurfaceVariant.copy(alpha = 0.6f)
                )
                if (project.sessionCount > 0) {
                    DotSeparator()
                    Text(
                        text = "${project.sessionCount} 个会话",
                        style = MaterialTheme.typography.labelSmall.copy(lineHeight = 12.sp),
                        color = cs.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

// =====================================================
//  临时对话卡片
// =====================================================

@Composable
fun AdhocSessionDrawerItem(
    session: AdhocSession,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val cs = MaterialTheme.colorScheme
    val shapes = LingTheme.shapes

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shapes.sm)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(width = 3.dp, height = 28.dp)
                .background(cs.outlineVariant)
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = session.title ?: "未命名对话",
                style = MaterialTheme.typography.titleSmall,
                color = cs.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = formatSessionTime(session.updatedAt),
                style = MaterialTheme.typography.labelSmall.copy(lineHeight = 12.sp),
                color = cs.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .clickable { showDeleteDialog = true },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Delete,
                contentDescription = "删除",
                tint = cs.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(15.dp)
            )
        }
    }

    if (showDeleteDialog) {
        DeleteConfirmDialog(
            itemName = session.title ?: "未命名对话",
            onConfirm = {
                onDelete()
                showDeleteDialog = false
            },
            onDismiss = { showDeleteDialog = false }
        )
    }
}

// =====================================================
//  小元件
// =====================================================

@Composable
private fun ThinDivider(modifier: Modifier = Modifier) {
    val cs = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(cs.outlineVariant.copy(alpha = 0.5f))
    )
}

@Composable
private fun DotSeparator() {
    val cs = MaterialTheme.colorScheme
    Spacer(Modifier.width(6.dp))
    Box(
        modifier = Modifier
            .size(2.dp)
            .clip(CircleShape)
            .background(cs.onSurfaceVariant.copy(alpha = 0.4f))
    )
    Spacer(Modifier.width(6.dp))
}

@Composable
private fun DrawerEmptyState(isSearching: Boolean) {
    val cs = MaterialTheme.colorScheme
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(32.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(cs.surfaceVariant.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isSearching) Icons.Outlined.Search else Icons.Outlined.FolderOpen,
                contentDescription = null,
                tint = cs.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = if (isSearching) "未找到匹配项" else "暂无对话记录",
            style = MaterialTheme.typography.bodyMedium,
            color = cs.onSurfaceVariant
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = if (isSearching) "试试其他关键词" else "开始一段新对话吧",
            style = MaterialTheme.typography.bodySmall,
            color = cs.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}

// =====================================================
//  Helpers
// =====================================================

/** 时间格式化（与原版一致） */
private fun formatSessionTime(dateTimeStr: String): String {
    return try {
        val instant = Instant.parse(dateTimeStr)
        val zoned = instant.atZone(ZoneId.systemDefault())
        val today = LocalDate.now()
        val date = zoned.toLocalDate()

        when {
            date == today -> zoned.format(DateTimeFormatter.ofPattern("HH:mm"))
            date == today.minusDays(1) -> "昨天 ${zoned.format(DateTimeFormatter.ofPattern("HH:mm"))}"
            date.year == today.year -> zoned.format(DateTimeFormatter.ofPattern("MM-dd HH:mm"))
            else -> zoned.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        }
    } catch (e: Exception) {
        dateTimeStr.take(16)
    }
}
