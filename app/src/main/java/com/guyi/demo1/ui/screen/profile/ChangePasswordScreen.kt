package com.guyi.demo1.ui.screen.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockReset
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.guyi.demo1.LingAgentApplication
import com.guyi.demo1.ui.components.LingPrimaryButton
import com.guyi.demo1.ui.components.LingTextField
import com.guyi.demo1.ui.theme.LingTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 修改密码 — Warm Calm 重做
 * 保留：authRepository.changePassword / 三个密码字段 / 可见性切换 / 成功后登出
 */
@Composable
fun ChangePasswordScreen(
    onBackClick: () -> Unit = {},
    onPasswordChanged: () -> Unit = {}
) {
    val context = LocalContext.current
    val authRepository = (context.applicationContext as LingAgentApplication).container.authRepository
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current
    val cs = MaterialTheme.colorScheme
    val space = LingTheme.spacing

    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    var showOld by remember { mutableStateOf(false) }
    var showNew by remember { mutableStateOf(false) }
    var showConfirm by remember { mutableStateOf(false) }

    val passwordsMatch = confirmPassword.isNotEmpty() && confirmPassword == newPassword
    val passwordsMismatch = confirmPassword.isNotEmpty() && confirmPassword != newPassword
    val canSubmit = !isSaving &&
            oldPassword.isNotBlank() &&
            newPassword.length >= 6 &&
            passwordsMatch

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
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .clickable(onClick = onBackClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回",
                            tint = cs.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(Modifier.width(space.sm))
                    Text(
                        text = "修改密码",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = cs.onSurface
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .imePadding()
                        .padding(horizontal = space.pageHorizontal)
                ) {
                    Spacer(Modifier.height(space.xl))

                    // Hero 区：图标 + 说明
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(cs.primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.LockReset,
                            contentDescription = null,
                            tint = cs.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(Modifier.height(space.md))
                    Text(
                        text = "更换新密码",
                        style = MaterialTheme.typography.headlineSmall,
                        color = cs.onBackground
                    )
                    Spacer(Modifier.height(space.xs))
                    Text(
                        text = "修改成功后需要使用新密码重新登录",
                        style = MaterialTheme.typography.bodyMedium,
                        color = cs.onSurfaceVariant
                    )

                    Spacer(Modifier.height(space.xxl))

                    // 当前密码
                    LingTextField(
                        value = oldPassword,
                        onValueChange = { oldPassword = it },
                        placeholder = "当前密码",
                        leadingIcon = Icons.Outlined.Lock,
                        enabled = !isSaving,
                        visualTransformation = if (showOld) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingContent = {
                            EyeToggle(visible = showOld) { showOld = !showOld }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        )
                    )
                    Spacer(Modifier.height(space.sm))

                    // 新密码
                    LingTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        placeholder = "新密码（至少 6 位）",
                        leadingIcon = Icons.Outlined.Lock,
                        enabled = !isSaving,
                        visualTransformation = if (showNew) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingContent = {
                            EyeToggle(visible = showNew) { showNew = !showNew }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        )
                    )
                    if (newPassword.isNotEmpty() && newPassword.length < 6) {
                        Spacer(Modifier.height(space.xs))
                        HintRow(
                            icon = Icons.Outlined.Close,
                            text = "密码至少需要 6 位",
                            color = cs.error
                        )
                    }
                    Spacer(Modifier.height(space.sm))

                    // 确认密码
                    LingTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        placeholder = "再次输入新密码",
                        leadingIcon = Icons.Outlined.Lock,
                        enabled = !isSaving,
                        isError = passwordsMismatch,
                        visualTransformation = if (showConfirm) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingContent = {
                            EyeToggle(visible = showConfirm) { showConfirm = !showConfirm }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { focusManager.clearFocus() }
                        )
                    )
                    if (passwordsMatch) {
                        Spacer(Modifier.height(space.xs))
                        HintRow(
                            icon = Icons.Outlined.Check,
                            text = "密码匹配",
                            color = cs.primary
                        )
                    } else if (passwordsMismatch) {
                        Spacer(Modifier.height(space.xs))
                        HintRow(
                            icon = Icons.Outlined.Close,
                            text = "两次输入的密码不一致",
                            color = cs.error
                        )
                    }

                    Spacer(Modifier.height(space.xl))

                    LingPrimaryButton(
                        text = if (isSaving) "正在修改..." else "确认修改",
                        onClick = {
                            focusManager.clearFocus()
                            isSaving = true
                            scope.launch {
                                val result = authRepository.changePassword(oldPassword, newPassword)
                                result.onSuccess {
                                    snackbarHostState.showSnackbar("密码修改成功，请重新登录")
                                    delay(1000)
                                    authRepository.logout()
                                    onPasswordChanged()
                                }.onFailure { e ->
                                    snackbarHostState.showSnackbar(e.message ?: "修改失败")
                                }
                                isSaving = false
                            }
                        },
                        enabled = canSubmit,
                        loading = isSaving,
                        showArrow = false
                    )

                    Spacer(Modifier.height(space.xxl))
                    Spacer(Modifier.navigationBarsPadding())
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

@Composable
private fun EyeToggle(visible: Boolean, onToggle: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .clickable(onClick = onToggle),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (visible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
            contentDescription = if (visible) "隐藏密码" else "显示密码",
            tint = cs.onSurfaceVariant.copy(alpha = 0.7f),
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun HintRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    color: androidx.compose.ui.graphics.Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(12.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 14.sp),
            color = color
        )
    }
}
