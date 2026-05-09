package com.guyi.demo1.ui.screen.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.guyi.demo1.LingAgentApplication
import com.guyi.demo1.data.api.UserUpdateRequest
import com.guyi.demo1.ui.components.LingTextField
import com.guyi.demo1.ui.theme.LingTheme
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * 个人资料页 — Warm Calm 重做
 *   · 自定义顶栏 + 右侧编辑/取消/保存
 *   · 大头像 + 相机徽章 + 用户名
 *   · 基本信息卡（描边、无 elevation）
 *   · 账号设置入口
 *
 * 全部原交互保留（加载用户、更新用户名、上传头像、编辑态切换）
 */
@Composable
fun ProfileScreen(
    onBackClick: () -> Unit = {},
    onSaveSuccess: () -> Unit = {},
    onAccountSecurityClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val appContainer = (context.applicationContext as LingAgentApplication).container
    val tokenManager = appContainer.tokenManager
    val userApi = appContainer.userApi
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val cs = MaterialTheme.colorScheme
    val space = LingTheme.spacing

    val storedUsername by tokenManager.getUsernameFlow().collectAsState(initial = null)
    val storedUserId by tokenManager.getUserIdFlow().collectAsState(initial = null)

    var isEditing by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    var username by remember { mutableStateOf("") }
    var deviceModel by remember { mutableStateOf("") }
    var createdAt by remember { mutableStateOf("") }
    var avatarUrl by remember { mutableStateOf<String?>(null) }
    var avatarKey by remember { mutableStateOf(0) }

    var editUsername by remember { mutableStateOf("") }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            scope.launch {
                try {
                    val userId = storedUserId ?: return@launch
                    val inputStream = context.contentResolver.openInputStream(selectedUri)
                        ?: return@launch
                    val bytes = inputStream.readBytes()
                    inputStream.close()
                    val contentType = context.contentResolver.getType(selectedUri) ?: "image/jpeg"
                    val requestBody = bytes.toRequestBody(contentType.toMediaTypeOrNull())
                    val part = MultipartBody.Part.createFormData(
                        "file", "avatar.${contentType.substringAfter("/")}", requestBody
                    )
                    userApi.uploadAvatar(userId, part)
                    avatarKey++
                    snackbarHostState.showSnackbar("头像更新成功")
                } catch (e: Exception) {
                    snackbarHostState.showSnackbar("头像上传失败：${e.message}")
                }
            }
        }
    }

    LaunchedEffect(storedUserId) {
        storedUserId?.let { userId ->
            try {
                val user = userApi.getUser(userId)
                username = user.username ?: ""
                deviceModel = user.deviceModel ?: ""
                createdAt = user.createdAt.take(10)
                if (user.avatar != null) {
                    avatarUrl = "http://10.0.2.2:9000/api/users/$userId/avatar"
                }
            } catch (e: Exception) {
                username = storedUsername ?: ""
            }
            isLoading = false
        }
    }

    Surface(color = cs.background, modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                // 顶栏
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = space.md, vertical = space.xs),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconCircleButton(
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDesc = "返回",
                        onClick = onBackClick
                    )
                    Spacer(Modifier.width(space.sm))
                    Text(
                        text = "个人资料",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = cs.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    if (isEditing) {
                        TextPillButton(
                            text = "取消",
                            onClick = {
                                isEditing = false
                                editUsername = username
                            },
                            primary = false
                        )
                        Spacer(Modifier.width(8.dp))
                        TextPillButton(
                            text = if (isSaving) "保存中..." else "保存",
                            onClick = {
                                if (!isSaving && editUsername.isNotBlank() && editUsername != username) {
                                    isSaving = true
                                    scope.launch {
                                        try {
                                            val userId = storedUserId ?: return@launch
                                            val response = userApi.updateUser(
                                                userId, UserUpdateRequest(username = editUsername)
                                            )
                                            username = response.username ?: editUsername
                                            tokenManager.saveUserInfo(userId, username)
                                            isEditing = false
                                            snackbarHostState.showSnackbar("保存成功")
                                            onSaveSuccess()
                                        } catch (e: Exception) {
                                            snackbarHostState.showSnackbar("保存失败：${e.message}")
                                        } finally {
                                            isSaving = false
                                        }
                                    }
                                }
                            },
                            primary = true,
                            enabled = !isSaving && editUsername.isNotBlank() && editUsername != username
                        )
                    } else {
                        IconCircleButton(
                            icon = Icons.Outlined.Edit,
                            contentDesc = "编辑",
                            onClick = {
                                editUsername = username
                                isEditing = true
                            }
                        )
                    }
                }

                if (isLoading) {
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
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .imePadding()
                            .padding(horizontal = space.pageHorizontal)
                    ) {
                        Spacer(Modifier.height(space.xl))

                        // 头像 + 用户名区
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            AvatarWithCameraBadge(
                                avatarUrl = avatarUrl?.let { "$it?v=$avatarKey" },
                                fallbackInitial = username.take(1).uppercase(),
                                onClick = { imagePickerLauncher.launch("image/*") }
                            )
                        }
                        Spacer(Modifier.height(space.md))
                        Text(
                            text = username.ifBlank { "—" },
                            style = MaterialTheme.typography.headlineSmall,
                            color = cs.onBackground,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                        Spacer(Modifier.height(space.xxs))
                        Text(
                            text = "点击头像更换",
                            style = MaterialTheme.typography.labelSmall.copy(
                                letterSpacing = 1.sp,
                                lineHeight = 12.sp
                            ),
                            color = cs.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )

                        Spacer(Modifier.height(space.xxl))

                        // 基本信息
                        SectionLabel("基本信息")
                        Spacer(Modifier.height(space.sm))
                        InfoCard {
                            if (isEditing) {
                                Box(modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)) {
                                    LingTextField(
                                        value = editUsername,
                                        onValueChange = { editUsername = it },
                                        placeholder = "用户名",
                                        leadingIcon = Icons.Outlined.Person,
                                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                            imeAction = ImeAction.Done
                                        )
                                    )
                                }
                            } else {
                                ProfileInfoRow(
                                    icon = Icons.Outlined.Person,
                                    label = "用户名",
                                    value = username
                                )
                                if (deviceModel.isNotBlank()) {
                                    InfoDivider()
                                    ProfileInfoRow(
                                        icon = Icons.Outlined.PhoneAndroid,
                                        label = "设备",
                                        value = deviceModel
                                    )
                                }
                                if (createdAt.isNotBlank()) {
                                    InfoDivider()
                                    ProfileInfoRow(
                                        icon = Icons.Outlined.CalendarToday,
                                        label = "注册时间",
                                        value = createdAt
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(space.xl))

                        // 账号设置
                        SectionLabel("账号设置")
                        Spacer(Modifier.height(space.sm))
                        InfoCard {
                            ProfileActionRow(
                                icon = Icons.Outlined.Security,
                                title = "账号安全",
                                description = "登录设备、活跃时间",
                                onClick = onAccountSecurityClick
                            )
                        }

                        Spacer(Modifier.height(space.xxl))
                        Spacer(Modifier.navigationBarsPadding())
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
}

// =====================================================
//  组件
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

/** 胶囊文字按钮（顶栏取消/保存用） */
@Composable
private fun TextPillButton(
    text: String,
    onClick: () -> Unit,
    primary: Boolean,
    enabled: Boolean = true
) {
    val cs = MaterialTheme.colorScheme
    val shapes = LingTheme.shapes
    val bg = when {
        !enabled -> cs.surfaceVariant.copy(alpha = 0.5f)
        primary -> cs.primary
        else -> Color.Transparent
    }
    val border = when {
        !enabled -> cs.outlineVariant
        primary -> Color.Transparent
        else -> cs.outlineVariant
    }
    val textColor = when {
        !enabled -> cs.onSurfaceVariant.copy(alpha = 0.5f)
        primary -> cs.onPrimary
        else -> cs.onSurface
    }

    Box(
        modifier = Modifier
            .clip(shapes.pill)
            .background(bg)
            .then(
                if (border != Color.Transparent) {
                    Modifier.border(width = 1.dp, color = border, shape = shapes.pill)
                } else Modifier
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Medium,
                lineHeight = 16.sp
            ),
            color = textColor
        )
    }
}

@Composable
private fun AvatarWithCameraBadge(
    avatarUrl: String?,
    fallbackInitial: String,
    onClick: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val context = LocalContext.current
    Box(modifier = Modifier.size(132.dp)) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .align(Alignment.TopStart)
                .clip(CircleShape)
                .background(cs.primary.copy(alpha = 0.12f))
                .border(width = 1.dp, color = cs.outlineVariant, shape = CircleShape)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            if (avatarUrl != null) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(avatarUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "头像",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = fallbackInitial.ifBlank { "?" },
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Light
                    ),
                    color = cs.primary
                )
            }
        }
        // 相机徽章
        Box(
            modifier = Modifier
                .size(36.dp)
                .align(Alignment.BottomEnd)
                .clip(CircleShape)
                .background(cs.primary)
                .border(width = 2.dp, color = cs.background, shape = CircleShape)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.CameraAlt,
                contentDescription = "更换头像",
                tint = cs.onPrimary,
                modifier = Modifier.size(16.dp)
            )
        }
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
private fun InfoCard(content: @Composable () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val shapes = LingTheme.shapes
    Surface(
        shape = shapes.md,
        color = cs.surface,
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = cs.outlineVariant, shape = shapes.md)
    ) {
        Column { content() }
    }
}

@Composable
private fun InfoDivider() {
    val cs = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 60.dp)
            .height(0.5.dp)
            .background(cs.outlineVariant.copy(alpha = 0.5f))
    )
}

@Composable
private fun ProfileInfoRow(icon: ImageVector, label: String, value: String) {
    val cs = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(cs.onSurfaceVariant.copy(alpha = 0.10f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = cs.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 1.sp,
                    lineHeight = 12.sp
                ),
                color = cs.onSurfaceVariant.copy(alpha = 0.7f)
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = cs.onSurface
            )
        }
    }
}

@Composable
private fun ProfileActionRow(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(cs.onSurfaceVariant.copy(alpha = 0.10f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = cs.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = cs.onSurface
            )
            Spacer(Modifier.height(1.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 16.sp),
                color = cs.onSurfaceVariant.copy(alpha = 0.75f)
            )
        }
        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = cs.onSurfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier.size(16.dp)
        )
    }
}
