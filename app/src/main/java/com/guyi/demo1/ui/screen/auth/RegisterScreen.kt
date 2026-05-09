package com.guyi.demo1.ui.screen.auth

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.guyi.demo1.LingAgentApplication
import com.guyi.demo1.ui.components.AmbientBackdrop
import com.guyi.demo1.ui.components.FadeInRise
import com.guyi.demo1.ui.components.LingLogo
import com.guyi.demo1.ui.components.LingPrimaryButton
import com.guyi.demo1.ui.components.LingTextField
import com.guyi.demo1.ui.theme.LingTheme
import kotlinx.coroutines.delay

/** 密码强度等级（保留原计算逻辑，UI 重做） */
enum class PasswordStrength(val label: String, val filledSegments: Int) {
    WEAK("弱", 1),
    MEDIUM("中", 2),
    STRONG("强", 3)
}

/**
 * 注册页 — 风格延续登录页的 Hero + 装饰背景
 *
 * 表单：用户名 / 密码（带强度条）/ 确认密码（带匹配提示）/ 协议勾选
 */
@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit = {},
    onBackToLogin: () -> Unit = {}
) {
    val context = LocalContext.current
    val appContainer = (context.applicationContext as LingAgentApplication).container
    val viewModel: RegisterViewModel = viewModel(
        factory = RegisterViewModelFactory(appContainer.authRepository)
    )

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var agreeToTerms by remember { mutableStateOf(false) }
    var countdown by remember { mutableStateOf(0) }

    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    val cs = MaterialTheme.colorScheme
    val space = LingTheme.spacing

    LaunchedEffect(uiState) {
        if (uiState is RegisterUiState.Success) {
            countdown = 2
            while (countdown > 0) {
                delay(1000)
                countdown--
            }
            onRegisterSuccess()
            viewModel.resetState()
        }
    }

    val passwordStrength = remember(password) {
        when {
            password.isEmpty() -> null
            password.length < 6 -> PasswordStrength.WEAK
            password.length < 10 -> PasswordStrength.MEDIUM
            else -> PasswordStrength.STRONG
        }
    }

    val passwordsMatch = remember(password, confirmPassword) {
        confirmPassword.isNotEmpty() && password == confirmPassword
    }

    val isLoading = uiState is RegisterUiState.Loading
    val canSubmit = !isLoading &&
            username.length >= 3 &&
            password.length >= 6 &&
            passwordsMatch &&
            agreeToTerms

    Surface(color = cs.background, modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
            // ───── 装饰背景 ─────
            AmbientBackdrop(
                modifier = Modifier.fillMaxSize(),
                primary = cs.primary,
                tertiary = cs.tertiary,
                outlineSoft = cs.outline.copy(alpha = 0.18f)
            )

            // ───── 顶栏：返回箭头 ─────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = space.md, vertical = space.xs),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .clickable(enabled = !isLoading) { onBackToLogin() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "返回",
                        tint = cs.onSurface,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(Modifier.weight(1f))
                LingLogo(size = 24.dp)
                Spacer(Modifier.width(space.md))
            }

            // ───── 主体可滚动内容 ─────
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .statusBarsPadding()
                    .imePadding()
                    .padding(horizontal = space.pageHorizontal, vertical = space.huge),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(space.xxl))

                // Hero
                FadeInRise(delayMs = 0) {
                    Text(
                        text = "创建账号",
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.Normal,
                            letterSpacing = (-0.5).sp
                        ),
                        color = cs.onBackground
                    )
                }
                Spacer(Modifier.height(space.sm))
                FadeInRise(delayMs = 80) {
                    Text(
                        text = "加入 Ling，开启 AI 协作",
                        style = MaterialTheme.typography.bodyLarge,
                        color = cs.onSurfaceVariant
                    )
                }

                Spacer(Modifier.height(space.xxl))

                // 用户名
                FadeInRise(delayMs = 160) {
                    LingTextField(
                        value = username,
                        onValueChange = {
                            username = it
                            if (uiState is RegisterUiState.Error) viewModel.resetState()
                        },
                        placeholder = "用户名（至少 3 个字符）",
                        leadingIcon = Icons.Outlined.Person,
                        enabled = !isLoading,
                        trailingContent = if (username.length >= 3) {
                            { CheckMark(color = cs.primary) }
                        } else null,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        )
                    )
                }
                Spacer(Modifier.height(space.sm))

                // 密码
                FadeInRise(delayMs = 220) {
                    LingTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            if (uiState is RegisterUiState.Error) viewModel.resetState()
                        },
                        placeholder = "密码（至少 6 个字符）",
                        leadingIcon = Icons.Outlined.Lock,
                        enabled = !isLoading,
                        visualTransformation = if (passwordVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        trailingContent = {
                            EyeToggle(visible = passwordVisible) {
                                passwordVisible = !passwordVisible
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        )
                    )
                }

                // 密码强度条
                if (passwordStrength != null) {
                    Spacer(Modifier.height(space.xs))
                    PasswordStrengthBar(strength = passwordStrength)
                }

                Spacer(Modifier.height(space.sm))

                // 确认密码
                FadeInRise(delayMs = 280) {
                    LingTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            if (uiState is RegisterUiState.Error) viewModel.resetState()
                        },
                        placeholder = "再次输入密码",
                        leadingIcon = Icons.Outlined.Lock,
                        enabled = !isLoading,
                        isError = confirmPassword.isNotEmpty() && !passwordsMatch,
                        visualTransformation = if (confirmPasswordVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        trailingContent = {
                            EyeToggle(visible = confirmPasswordVisible) {
                                confirmPasswordVisible = !confirmPasswordVisible
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { focusManager.clearFocus() }
                        )
                    )
                }
                if (confirmPassword.isNotEmpty()) {
                    Spacer(Modifier.height(space.xs))
                    MatchHint(matched = passwordsMatch)
                }

                Spacer(Modifier.height(space.lg))

                // 协议勾选
                FadeInRise(delayMs = 340) {
                    TermsCheckbox(
                        checked = agreeToTerms,
                        onCheckedChange = { agreeToTerms = it },
                        enabled = !isLoading
                    )
                }

                // 错误提示
                if (uiState is RegisterUiState.Error) {
                    Spacer(Modifier.height(space.sm))
                    InlineErrorTextR(text = (uiState as RegisterUiState.Error).message)
                }

                Spacer(Modifier.height(space.xl))

                // 注册按钮
                FadeInRise(delayMs = 400) {
                    LingPrimaryButton(
                        text = "创建账号",
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.register(username, password)
                        },
                        enabled = canSubmit,
                        loading = isLoading
                    )
                }

                Spacer(Modifier.height(space.md))

                // 成功提示卡
                if (uiState is RegisterUiState.Success && countdown > 0) {
                    SuccessCard(countdown = countdown)
                    Spacer(Modifier.height(space.md))
                }

                // 返回登录
                FadeInRise(delayMs = 460) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "已有账号？",
                                style = MaterialTheme.typography.bodyMedium,
                                color = cs.onSurfaceVariant
                            )
                            Spacer(Modifier.width(4.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable(
                                        enabled = !isLoading && countdown == 0
                                    ) { onBackToLogin() }
                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "返回登录",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = cs.primary
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(space.xl))
                Spacer(Modifier.navigationBarsPadding())
            }
        }
    }
}

// =====================================================
//  子组件
// =====================================================

@Composable
private fun CheckMark(color: Color) {
    Box(
        modifier = Modifier
            .size(20.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Check,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(12.dp)
        )
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

/**
 * 密码强度条 — 三段细圆条 + 等级文字
 *  · 弱：第 1 段填 error 暖色
 *  · 中：前 2 段填 secondary
 *  · 强：3 段都填 primary
 */
@Composable
private fun PasswordStrengthBar(strength: PasswordStrength) {
    val cs = MaterialTheme.colorScheme
    val activeColor = when (strength) {
        PasswordStrength.WEAK -> cs.error
        PasswordStrength.MEDIUM -> cs.secondary
        PasswordStrength.STRONG -> cs.primary
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) { index ->
            val active = index < strength.filledSegments
            val color by animateColorAsState(
                targetValue = if (active) activeColor else cs.outlineVariant,
                animationSpec = tween(220),
                label = "seg$index"
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color)
            )
            if (index < 2) Spacer(Modifier.width(6.dp))
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text = strength.label,
            style = MaterialTheme.typography.labelSmall.copy(
                letterSpacing = 1.sp,
                lineHeight = 12.sp,
                fontWeight = FontWeight.Medium
            ),
            color = activeColor
        )
    }
}

@Composable
private fun MatchHint(matched: Boolean) {
    val cs = MaterialTheme.colorScheme
    val color = if (matched) cs.primary else cs.error
    val icon = if (matched) Icons.Outlined.Check else Icons.Outlined.Close
    val label = if (matched) "密码匹配" else "两次输入的密码不一致"

    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(12.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = color
        )
    }
}

/**
 * 协议勾选 — 自定义方框（替换 Material Checkbox），更精致
 */
@Composable
private fun TermsCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean
) {
    val cs = MaterialTheme.colorScheme
    val borderColor by animateColorAsState(
        targetValue = if (checked) cs.primary else cs.outline,
        animationSpec = tween(160),
        label = "tcborder"
    )
    val fillColor by animateColorAsState(
        targetValue = if (checked) cs.primary else Color.Transparent,
        animationSpec = tween(160),
        label = "tcfill"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(enabled = enabled) { onCheckedChange(!checked) }
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(fillColor)
                .border(width = 1.5.dp, color = borderColor, shape = RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (checked) {
                Icon(
                    imageVector = Icons.Outlined.Check,
                    contentDescription = null,
                    tint = cs.onPrimary,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
        Spacer(Modifier.width(10.dp))
        Text(
            text = "我已阅读并同意 用户协议 与 隐私政策",
            style = MaterialTheme.typography.bodySmall,
            color = cs.onSurfaceVariant
        )
    }
}

@Composable
private fun InlineErrorTextR(text: String) {
    val cs = MaterialTheme.colorScheme
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(width = 3.dp, height = 14.dp)
                .background(cs.error)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = cs.error
        )
    }
}

@Composable
private fun SuccessCard(countdown: Int) {
    val cs = MaterialTheme.colorScheme
    val shapes = LingTheme.shapes
    Surface(
        shape = shapes.md,
        color = cs.primaryContainer.copy(alpha = 0.5f),
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = cs.primary.copy(alpha = 0.3f), shape = shapes.md)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(cs.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Check,
                    contentDescription = null,
                    tint = cs.onPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = "注册成功",
                    style = MaterialTheme.typography.titleMedium,
                    color = cs.onPrimaryContainer
                )
                Text(
                    text = "${countdown} 秒后跳回登录…",
                    style = MaterialTheme.typography.bodySmall,
                    color = cs.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}
