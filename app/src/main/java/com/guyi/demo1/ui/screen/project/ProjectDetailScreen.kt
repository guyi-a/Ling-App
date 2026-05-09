package com.guyi.demo1.ui.screen.project

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.guyi.demo1.LingAgentApplication
import com.guyi.demo1.data.model.DevProcess
import com.guyi.demo1.data.model.ProjectDetail
import com.guyi.demo1.data.model.SessionBrief
import com.guyi.demo1.data.network.ApiConfig
import com.guyi.demo1.ui.theme.LingTheme
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * 项目详情页 — Warm Calm 重做
 *   · 自定义顶栏
 *   · 项目信息卡（描边 + 大图标容器）
 *   · 进程管理卡（状态点 + 进程列表 + 启停 / 立即前往）
 *   · 静态应用预览入口
 *   · 会话列表章节
 *
 * 保留全部交互：加载详情、启停进程、应用预览、会话点击
 */
@Composable
fun ProjectDetailScreen(
    projectId: Int,
    onBackClick: () -> Unit = {},
    onSessionClick: (String) -> Unit = {},
    onOpenApp: (url: String, title: String) -> Unit = { _, _ -> },
    onProjectDeleted: () -> Unit = {}
) {
    val context = LocalContext.current
    val appContainer = (context.applicationContext as LingAgentApplication).container
    val projectRepository = appContainer.projectRepository
    val devRepository = appContainer.devRepository
    val scope = rememberCoroutineScope()
    val cs = MaterialTheme.colorScheme
    val space = LingTheme.spacing

    var project by remember { mutableStateOf<ProjectDetail?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var runningProcesses by remember { mutableStateOf<List<DevProcess>>(emptyList()) }
    var stoppedProcesses by remember { mutableStateOf<List<DevProcess>>(emptyList()) }
    var processesLoadError by remember { mutableStateOf<String?>(null) }
    var isProcessLoading by remember { mutableStateOf(false) }
    var showSettingsMenu by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var renameText by remember { mutableStateOf("") }
    var isActionLoading by remember { mutableStateOf(false) }

    // 刷新进程列表（拉取全局、用项目 sessionIds 过滤、分 running / stopped 两组）
    suspend fun refreshProcesses(sessionIds: Set<String>) {
        if (sessionIds.isEmpty()) {
            runningProcesses = emptyList()
            stoppedProcesses = emptyList()
            return
        }
        devRepository.listAllProcesses()
            .onSuccess { procs ->
                android.util.Log.d(
                    "ProjectDetail",
                    "listAllProcesses OK, total=${procs.size}, sessionIds=$sessionIds, " +
                        "procs=${procs.map { "${it.name}@${it.sessionId}=${it.status}" }}"
                )
                val matching = procs.filter { it.sessionId != null && sessionIds.contains(it.sessionId) }
                val seenR = mutableSetOf<String>()
                val running = matching
                    .filter { it.status == "running" }
                    .filter { seenR.add("${it.name}:${it.port ?: ""}") }
                val seenS = mutableSetOf<String>()
                val stopped = matching
                    .filter { it.status == "exited" }
                    .filter { seenS.add("${it.name}:${it.port ?: ""}") }
                runningProcesses = running
                stoppedProcesses = stopped
                processesLoadError = null
            }
            .onFailure {
                android.util.Log.e("ProjectDetail", "listAllProcesses failed", it)
                processesLoadError = it.message ?: "加载失败"
            }
    }

    LaunchedEffect(projectId) {
        scope.launch {
            isLoading = true
            val result = projectRepository.getProjectDetail(projectId)
            result.onSuccess { detail ->
                project = detail
                error = null
                val sessionIds = detail.sessions.map { it.sessionId }.toSet()
                refreshProcesses(sessionIds)
            }.onFailure {
                error = it.message
            }
            isLoading = false
        }
    }

    Surface(color = cs.background, modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 顶栏
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
                Text(
                    text = project?.title ?: "项目详情",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = cs.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(cs.outlineVariant.copy(alpha = 0.5f))
            )

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = cs.primary,
                            strokeWidth = 2.5.dp,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                error != null -> {
                    ErrorFallback(message = error ?: "加载失败")
                }
                project != null -> {
                    val p = project!!
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = space.pageHorizontal,
                            end = space.pageHorizontal,
                            top = space.lg,
                            bottom = space.xxl
                        ),
                        verticalArrangement = Arrangement.spacedBy(space.sm)
                    ) {
                        // 项目信息卡（右下角 meta 行带齿轮按钮）
                        item {
                            ProjectInfoCard(
                                project = p,
                                trailing = {
                                    Box {
                                        IconCircleButton(
                                            icon = Icons.Outlined.Settings,
                                            contentDesc = "项目设置",
                                            onClick = { showSettingsMenu = true }
                                        )
                                        ProjectSettingsMenu(
                                            expanded = showSettingsMenu,
                                            onDismiss = { showSettingsMenu = false },
                                            onRename = {
                                                showSettingsMenu = false
                                                renameText = project?.title ?: ""
                                                showRenameDialog = true
                                            },
                                            onDelete = {
                                                showSettingsMenu = false
                                                showDeleteConfirm = true
                                            }
                                        )
                                    }
                                }
                            )
                        }

                        // 应用运行区：优先展示 running 卡片，没有 running 才展示 stopped 卡片
                        val sessionIds = p.sessions.map { it.sessionId }.toSet()
                        if (runningProcesses.isNotEmpty()) {
                            item {
                                ProcessCard(
                                    processes = runningProcesses,
                                    mode = ProcessCardMode.RUNNING,
                                    isProcessLoading = isProcessLoading,
                                    onAction = {
                                        scope.launch {
                                            isProcessLoading = true
                                            runningProcesses.forEach { proc ->
                                                proc.sessionId?.let {
                                                    devRepository.stopProcess(it, proc.name)
                                                }
                                            }
                                            refreshProcesses(sessionIds)
                                            isProcessLoading = false
                                        }
                                    },
                                    onOpenPreview = { port ->
                                        onOpenApp(
                                            "${ApiConfig.BASE_URL}/api/preview/$port/",
                                            p.title ?: "应用预览"
                                        )
                                    }
                                )
                            }
                        } else if (stoppedProcesses.isNotEmpty()) {
                            item {
                                ProcessCard(
                                    processes = stoppedProcesses,
                                    mode = ProcessCardMode.STOPPED,
                                    isProcessLoading = isProcessLoading,
                                    onAction = {
                                        scope.launch {
                                            isProcessLoading = true
                                            stoppedProcesses.forEach { proc ->
                                                proc.sessionId?.let {
                                                    devRepository.restartProcess(it, proc.name)
                                                }
                                            }
                                            refreshProcesses(sessionIds)
                                            isProcessLoading = false
                                        }
                                    },
                                    onOpenPreview = {}
                                )
                            }
                        } else {
                            // 两组都空 — 展示占位卡，便于排查
                            item {
                                NoAppsPlaceholder(
                                    errorMessage = processesLoadError,
                                    onRetry = {
                                        scope.launch { refreshProcesses(sessionIds) }
                                    }
                                )
                            }
                        }

                        // 会话章节
                        item {
                            Spacer(Modifier.height(space.sm))
                            SectionLabel("会话列表")
                            Spacer(Modifier.height(space.xs))
                        }

                        if (p.sessions.isEmpty()) {
                            item {
                                EmptySessionsCard()
                            }
                        } else {
                            items(p.sessions, key = { it.sessionId }) { session ->
                                SessionRow(
                                    session = session,
                                    onClick = { onSessionClick(session.sessionId) }
                                )
                            }
                        }

                        item { Spacer(Modifier.navigationBarsPadding()) }
                    }
                }
            }
        }
    }

    // ── 重命名对话框 ──
    if (showRenameDialog) {
        RenameProjectDialog(
            currentName = renameText,
            onNameChange = { renameText = it },
            isLoading = isActionLoading,
            onConfirm = {
                if (renameText.isNotBlank() && renameText != project?.title) {
                    scope.launch {
                        isActionLoading = true
                        projectRepository.updateProject(projectId, title = renameText)
                            .onSuccess { updated ->
                                project = project?.copy(title = updated.title)
                            }
                        isActionLoading = false
                        showRenameDialog = false
                    }
                } else {
                    showRenameDialog = false
                }
            },
            onDismiss = { showRenameDialog = false }
        )
    }

    // ── 删除确认对话框 ──
    if (showDeleteConfirm) {
        DeleteProjectDialog(
            projectTitle = project?.title ?: "这个项目",
            isLoading = isActionLoading,
            onConfirm = {
                scope.launch {
                    isActionLoading = true
                    projectRepository.deleteProject(projectId)
                        .onSuccess {
                            showDeleteConfirm = false
                            isActionLoading = false
                            onProjectDeleted()
                        }
                        .onFailure {
                            isActionLoading = false
                            error = it.message
                            showDeleteConfirm = false
                        }
                }
            },
            onDismiss = { showDeleteConfirm = false }
        )
    }
}

// =====================================================
//  子组件
// =====================================================

@Composable
private fun IconCircleButton(
    icon: ImageVector,
    contentDesc: String,
    onClick: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDesc,
            tint = cs.onSurface,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    val cs = MaterialTheme.colorScheme
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(start = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .width(12.dp)
                .height(1.dp)
                .background(cs.onSurfaceVariant.copy(alpha = 0.5f))
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium.copy(
                letterSpacing = 2.sp,
                lineHeight = 14.sp,
                fontWeight = FontWeight.Medium
            ),
            color = cs.onSurfaceVariant
        )
    }
}

@Composable
private fun ProjectInfoCard(
    project: ProjectDetail,
    trailing: @Composable () -> Unit = {}
) {
    val cs = MaterialTheme.colorScheme
    val shapes = LingTheme.shapes
    Surface(
        shape = shapes.md,
        color = cs.surface,
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = cs.outlineVariant, shape = shapes.md)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val iconText = project.icon?.takeIf { !it.startsWith("__img__") }
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(cs.primary.copy(alpha = 0.10f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (iconText != null) {
                        Text(
                            text = iconText,
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.wrapContentSize(Alignment.Center)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.FolderOpen,
                            contentDescription = null,
                            tint = cs.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = project.title ?: "未命名项目",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = cs.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    project.description?.takeIf { it.isNotBlank() }?.let { desc ->
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = desc,
                            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 16.sp),
                            color = cs.onSurfaceVariant.copy(alpha = 0.8f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Meta 行：左侧 chips + 右侧 trailing（齿轮按钮）
            Row(verticalAlignment = Alignment.CenterVertically) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    MetaChip(
                        icon = Icons.Outlined.Chat,
                        text = "${project.sessionCount} 个会话"
                    )
                    project.lastActiveAt?.let {
                        MetaChip(
                            icon = Icons.Outlined.Schedule,
                            text = "最近 ${formatTime(it)}"
                        )
                    }
                }
                trailing()
            }
        }
    }
}

@Composable
private fun MetaChip(icon: ImageVector, text: String) {
    val cs = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(cs.surfaceVariant.copy(alpha = 0.6f))
            .border(
                width = 1.dp,
                color = cs.outlineVariant.copy(alpha = 0.6f),
                shape = RoundedCornerShape(999.dp)
            )
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = cs.onSurfaceVariant.copy(alpha = 0.75f),
            modifier = Modifier.size(12.dp)
        )
        Spacer(Modifier.width(5.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                letterSpacing = 0.3.sp,
                lineHeight = 12.sp
            ),
            color = cs.onSurfaceVariant
        )
    }
}

private enum class ProcessCardMode { RUNNING, STOPPED }

@Composable
private fun ProcessCard(
    processes: List<DevProcess>,
    mode: ProcessCardMode,
    isProcessLoading: Boolean,
    onAction: () -> Unit,
    onOpenPreview: (port: Int) -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val shapes = LingTheme.shapes
    val isRunning = mode == ProcessCardMode.RUNNING
    // 运行卡用柔和的生机感（苔藓绿）；已停止卡用中性灰
    val runningAccent = Color(0xFF5F8F5F)
    val accent = if (isRunning) runningAccent else cs.onSurfaceVariant
    val cardBg = if (isRunning) runningAccent.copy(alpha = 0.08f)
                 else cs.surfaceVariant.copy(alpha = 0.5f)
    val cardBorder = if (isRunning) runningAccent.copy(alpha = 0.28f)
                     else cs.outlineVariant

    Surface(
        shape = shapes.md,
        color = cardBg,
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = cardBorder, shape = shapes.md)
    ) {
        Column {
            // ── 状态行 ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(
                            if (isRunning) accent
                            else cs.outline.copy(alpha = 0.5f)
                        )
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = if (isRunning) "运行中" else "已停止",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = accent,
                    modifier = Modifier.weight(1f)
                )
                if (!isRunning && processes.isNotEmpty()) {
                    Text(
                        text = "${processes.size} 个服务",
                        style = MaterialTheme.typography.labelSmall.copy(lineHeight = 12.sp),
                        color = cs.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Spacer(Modifier.width(10.dp))
                }
                if (isProcessLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = accent
                    )
                } else {
                    InlineActionText(
                        icon = if (isRunning) Icons.Outlined.Stop else Icons.Outlined.PlayArrow,
                        label = if (isRunning) "停止" else "启动",
                        color = accent,
                        onClick = onAction
                    )
                }
            }

            // 分隔线
            CardInlineDivider(cardBorder)

            // ── 进程列表 ──
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                processes.forEach { proc ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isRunning) accent
                                    else cs.outline.copy(alpha = 0.4f)
                                )
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = proc.name,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                lineHeight = 16.sp
                            ),
                            color = cs.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        if (proc.port != null) {
                            PortChip(port = proc.port)
                        }
                    }
                }
            }

            // ── 立即前往 ──
            if (isRunning) {
                val firstWithPort = processes.firstOrNull { it.port != null }
                if (firstWithPort != null) {
                    CardInlineDivider(cardBorder)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenPreview(firstWithPort.port!!) }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "立即前往",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = accent,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = accent,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CardInlineDivider(color: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(color.copy(alpha = 0.6f))
    )
}

@Composable
private fun InlineActionText(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(14.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Medium,
                lineHeight = 14.sp
            ),
            color = color
        )
    }
}

@Composable
private fun PortChip(port: Int) {
    val cs = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(cs.surfaceVariant.copy(alpha = 0.6f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = ":$port",
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                lineHeight = 12.sp
            ),
            color = cs.onSurfaceVariant
        )
    }
}

@Composable
private fun SessionRow(session: SessionBrief, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val shapes = LingTheme.shapes
    Surface(
        shape = shapes.sm,
        color = cs.surface,
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = cs.outlineVariant, shape = shapes.sm)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(width = 3.dp, height = 28.dp)
                    .background(cs.outlineVariant)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = session.title ?: "未命名会话",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = cs.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (session.isPinned) {
                        Spacer(Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Outlined.PushPin,
                            contentDescription = "置顶",
                            tint = cs.primary,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    text = formatTime(session.updatedAt),
                    style = MaterialTheme.typography.labelSmall.copy(lineHeight = 12.sp),
                    color = cs.onSurfaceVariant.copy(alpha = 0.65f)
                )
            }
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = cs.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun EmptySessionsCard() {
    val cs = MaterialTheme.colorScheme
    val shapes = LingTheme.shapes
    Surface(
        shape = shapes.md,
        color = cs.surface,
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = cs.outlineVariant, shape = shapes.md)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(cs.surfaceVariant.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Chat,
                    contentDescription = null,
                    tint = cs.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = "暂无会话",
                style = MaterialTheme.typography.bodySmall,
                color = cs.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun ErrorFallback(message: String) {
    val cs = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "加载失败",
            style = MaterialTheme.typography.titleMedium,
            color = cs.onBackground
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = cs.error
        )
    }
}

// =====================================================
//  Helpers
// =====================================================

private fun formatTime(dateTimeStr: String): String {
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

// =====================================================
//  无后台应用占位卡
// =====================================================

@Composable
private fun NoAppsPlaceholder(
    errorMessage: String?,
    onRetry: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val shapes = LingTheme.shapes
    Surface(
        shape = shapes.md,
        color = cs.surfaceVariant.copy(alpha = 0.4f),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = cs.outlineVariant.copy(alpha = 0.6f),
                shape = shapes.md
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(cs.outline.copy(alpha = 0.5f))
            )
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (errorMessage != null) "加载失败" else "暂无后台应用",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = cs.onSurfaceVariant
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = errorMessage ?: "此项目还没有启动过应用",
                    style = MaterialTheme.typography.labelSmall.copy(lineHeight = 14.sp),
                    color = cs.onSurfaceVariant.copy(alpha = 0.65f)
                )
            }
            if (errorMessage != null) {
                InlineActionText(
                    icon = Icons.Outlined.PlayArrow,
                    label = "重试",
                    color = cs.primary,
                    onClick = onRetry
                )
            }
        }
    }
}

// =====================================================
//  设置菜单 + 对话框
// =====================================================

@Composable
private fun ProjectSettingsMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        shape = LingTheme.shapes.md,
        containerColor = cs.surface
    ) {
        DropdownMenuItem(
            text = {
                Text(
                    "重命名",
                    style = MaterialTheme.typography.bodyMedium,
                    color = cs.onSurface
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = null,
                    tint = cs.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            },
            onClick = onRename
        )
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 2.dp),
            color = cs.outlineVariant.copy(alpha = 0.6f)
        )
        DropdownMenuItem(
            text = {
                Text(
                    "删除项目",
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
            onClick = onDelete
        )
    }
}

@Composable
private fun RenameProjectDialog(
    currentName: String,
    onNameChange: (String) -> Unit,
    isLoading: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        containerColor = cs.surface,
        shape = LingTheme.shapes.lg,
        title = {
            Text(
                "重命名项目",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium),
                color = cs.onSurface
            )
        },
        text = {
            OutlinedTextField(
                value = currentName,
                onValueChange = onNameChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isLoading,
                shape = LingTheme.shapes.sm,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = cs.primary,
                    unfocusedBorderColor = cs.outlineVariant,
                    focusedContainerColor = cs.surfaceVariant.copy(alpha = 0.3f),
                    unfocusedContainerColor = cs.surfaceVariant.copy(alpha = 0.3f)
                )
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = !isLoading && currentName.isNotBlank()) {
                Text(
                    if (isLoading) "保存中…" else "保存",
                    color = cs.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading) {
                Text("取消", color = cs.onSurfaceVariant)
            }
        }
    )
}

@Composable
private fun DeleteProjectDialog(
    projectTitle: String,
    isLoading: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        containerColor = cs.surface,
        shape = LingTheme.shapes.lg,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(cs.error.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = null,
                        tint = cs.error,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    "删除项目",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = cs.onSurface
                )
            }
        },
        text = {
            Text(
                text = "确定要删除「$projectTitle」吗？\n该操作不可撤销，项目下的所有会话也将一并删除。",
                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
                color = cs.onSurfaceVariant
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = !isLoading) {
                Text(
                    if (isLoading) "删除中…" else "删除",
                    color = cs.error,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading) {
                Text("取消", color = cs.onSurfaceVariant)
            }
        }
    )
}
