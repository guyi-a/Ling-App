package com.guyi.demo1.ui.screen.auth

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
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Shield
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontStyle
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

/**
 * 登录页 — Hero + 表单合一
 *
 * 视觉骨架（参考 Web Landing 精神）：
 *   · 顶部 Badge："五个专业 Agent · 全场景覆盖"
 *   · 巨型标题：Logo(L) + ing + Agent (italic 强调色)
 *   · 副标题
 *   · 装饰：暖光晕渐变 + 浮动小圆点 + 装饰圆环
 *   · 表单：自定义输入框
 *   · 主 CTA + 注册链接
 *   · 底部三能力 chip
 */
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit = {},
    onRegisterClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val appContainer = (context.applicationContext as LingAgentApplication).container
    val viewModel: LoginViewModel = viewModel(
        factory = LoginViewModelFactory(appContainer.authRepository)
    )

    // 调试默认凭据（发布前删）
    var username by remember { mutableStateOf("guyi") }
    var password by remember { mutableStateOf("123456") }
    var passwordVisible by remember { mutableStateOf(false) }

    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    val cs = MaterialTheme.colorScheme
    val space = LingTheme.spacing

    LaunchedEffect(uiState) {
        if (uiState is LoginUiState.Success) {
            onLoginSuccess()
            viewModel.resetState()
        }
    }

    val isLoading = uiState is LoginUiState.Loading
    val canSubmit = username.isNotBlank() && password.isNotBlank() && !isLoading

    Surface(color = cs.background, modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
            // ───── 装饰背景层 ─────
            AmbientBackdrop(
                modifier = Modifier.fillMaxSize(),
                primary = cs.primary,
                tertiary = cs.tertiary,
                outlineSoft = cs.outline.copy(alpha = 0.18f)
            )

            // ───── 主体内容 ─────
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .statusBarsPadding()
                    .imePadding()
                    .padding(horizontal = space.pageHorizontal),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(space.huge))

                // Badge
                FadeInRise(delayMs = 0) {
                    HeroBadge()
                }
                Spacer(Modifier.height(space.xl))

                // Hero Title
                FadeInRise(delayMs = 80) {
                    BigHeroTitle()
                }
                Spacer(Modifier.height(space.md))

                // Subtitle
                FadeInRise(delayMs = 160) {
                    Text(
                        text = "AI 驱动的多智能体生产力平台",
                        style = MaterialTheme.typography.bodyLarge,
                        color = cs.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(space.xs))
                FadeInRise(delayMs = 220) {
                    Text(
                        text = "对话 · 开发 · 数据 · 文档 · 身心健康",
                        style = MaterialTheme.typography.bodySmall.copy(letterSpacing = 1.sp),
                        color = cs.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }

                Spacer(Modifier.height(space.xxl))

                // 表单
                FadeInRise(delayMs = 300) {
                    LingTextField(
                        value = username,
                        onValueChange = {
                            username = it
                            if (uiState is LoginUiState.Error) viewModel.resetState()
                        },
                        placeholder = "用户名",
                        leadingIcon = Icons.Outlined.Person,
                        enabled = !isLoading,
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

                FadeInRise(delayMs = 360) {
                    LingTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            if (uiState is LoginUiState.Error) viewModel.resetState()
                        },
                        placeholder = "密码",
                        leadingIcon = Icons.Outlined.Lock,
                        enabled = !isLoading,
                        visualTransformation = if (passwordVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        trailingContent = {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .clickable { passwordVisible = !passwordVisible },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (passwordVisible) {
                                        Icons.Outlined.Visibility
                                    } else {
                                        Icons.Outlined.VisibilityOff
                                    },
                                    contentDescription = if (passwordVisible) "隐藏密码" else "显示密码",
                                    tint = cs.onSurfaceVariant.copy(alpha = 0.7f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                if (canSubmit) viewModel.login(username, password)
                            }
                        )
                    )
                }

                // 错误提示
                if (uiState is LoginUiState.Error) {
                    Spacer(Modifier.height(space.sm))
                    InlineErrorText(text = (uiState as LoginUiState.Error).message)
                }

                Spacer(Modifier.height(space.xl))

                // CTA
                FadeInRise(delayMs = 440) {
                    LingPrimaryButton(
                        text = "登录",
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.login(username, password)
                        },
                        enabled = canSubmit,
                        loading = isLoading
                    )
                }
                Spacer(Modifier.height(space.md))

                // 注册链接
                FadeInRise(delayMs = 500) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "还没有账号？",
                                style = MaterialTheme.typography.bodyMedium,
                                color = cs.onSurfaceVariant
                            )
                            Spacer(Modifier.width(4.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable(enabled = !isLoading) { onRegisterClick() }
                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "立即注册",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = cs.primary
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(space.xxl))

                // 三能力 chip
                FadeInRise(delayMs = 580) {
                    CapabilityRow()
                }

                Spacer(Modifier.height(space.xl))
                Spacer(Modifier.navigationBarsPadding())
            }
        }
    }
}

// =====================================================
//  Hero 子组件
// =====================================================

@Composable
private fun HeroBadge() {
    val cs = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(cs.surface.copy(alpha = 0.6f))
            .border(
                width = 1.dp,
                color = cs.outlineVariant,
                shape = RoundedCornerShape(999.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(cs.primary)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = "五个专业子 Agent · 全场景覆盖",
            style = MaterialTheme.typography.labelSmall.copy(
                letterSpacing = 1.sp,
                lineHeight = 12.sp
            ),
            color = cs.onSurfaceVariant
        )
    }
}

@Composable
private fun BigHeroTitle() {
    val cs = MaterialTheme.colorScheme
    Column {
        // 第一行：Logo(L) + ing
        Row(verticalAlignment = Alignment.Bottom) {
            LingLogo(size = 72.dp)
            Spacer(Modifier.width(6.dp))
            Text(
                text = "ing",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Normal,
                    letterSpacing = 0.sp
                ),
                color = cs.onBackground,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }
        // 第二行：Agent (italic 强调色)
        // 水平偏移：让 A 出现在 ing 的 g 右下方，形成连续书写感
        Text(
            text = "Agent",
            style = MaterialTheme.typography.displayMedium.copy(
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Light,
                letterSpacing = (-0.5).sp
            ),
            color = cs.primary,
            modifier = Modifier.padding(start = 165.dp)
        )
    }
}

@Composable
private fun InlineErrorText(text: String) {
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
private fun CapabilityRow() {
    val cs = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        CapabilityChip(icon = Icons.Outlined.Bolt, label = "流式响应", tint = cs.primary)
        CapabilityChip(icon = Icons.Outlined.Shield, label = "安全可控", tint = cs.tertiary)
        CapabilityChip(icon = Icons.Outlined.FavoriteBorder, label = "温暖共情", tint = cs.secondary)
    }
}

@Composable
private fun CapabilityChip(icon: ImageVector, label: String, tint: Color) {
    val cs = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(cs.surface.copy(alpha = 0.5f))
            .border(width = 1.dp, color = cs.outlineVariant, shape = RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                letterSpacing = 0.5.sp,
                lineHeight = 12.sp
            ),
            color = cs.onSurfaceVariant
        )
    }
}

