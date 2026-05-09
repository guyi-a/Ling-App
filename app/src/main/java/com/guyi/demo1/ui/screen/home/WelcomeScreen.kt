package com.guyi.demo1.ui.screen.home

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.TravelExplore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.guyi.demo1.ui.components.FadeInRise
import com.guyi.demo1.ui.components.LingLogo
import com.guyi.demo1.ui.theme.LingTheme
import java.util.Calendar

/**
 * 欢迎页 — 提示卡片改造为「分类 + 多 case」可展开卡片（参考 Web ChatPage 空状态）
 *
 * 结构：
 *   · 顶栏（menu / Logo / Person）
 *   · 日月装饰
 *   · 问候语 + 副标题
 *   · 4 张可展开分类卡（搜索资讯 / 生成文档 / 开发应用 / 身心健康）
 *     - Header 可点击展开/折叠
 *     - 第一个 case 始终可见
 *     - 展开后显示该分类的全部 cases
 *     - 任意 case 点击触发 onExampleClick(text)
 *   · 底部 CTA「开始新的对话」
 */
@Composable
fun WelcomeScreen(
    onStartChat: () -> Unit = {},
    onMenuClick: () -> Unit = {},
    onExampleClick: (String) -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    val cs = MaterialTheme.colorScheme
    val space = LingTheme.spacing

    val categories = remember {
        listOf(
            SuggestionCategory(
                icon = Icons.Outlined.TravelExplore,
                title = "搜索资讯",
                cases = listOf(
                    "搜索一下最近有什么重大的 AI 新闻，帮我整理成摘要",
                    "帮我搜索最新的科技行业融资动态",
                    "搜一下今年最值得关注的开源项目有哪些"
                )
            ),
            SuggestionCategory(
                icon = Icons.Outlined.EditNote,
                title = "生成文档",
                cases = listOf(
                    "帮我生成一份唐诗宋词精选集 PDF，要排版精美",
                    "帮我写一份产品需求文档（PRD）模板",
                    "生成一份周报模板，包含本周完成、下周计划和风险项"
                )
            ),
            SuggestionCategory(
                icon = Icons.Outlined.Code,
                title = "开发应用",
                cases = listOf(
                    "帮我做一个情绪轮盘，我想更精确地描述现在的感受",
                    "帮我做一个呼吸放松练习，我现在有点焦虑",
                    "帮我做一个认知扭曲训练小游戏，学会识别思维陷阱"
                )
            ),
            SuggestionCategory(
                icon = Icons.Outlined.Spa,
                title = "身心健康",
                cases = listOf(
                    "我今天头有点疼，心情也不太好，帮我记录一下健康日记",
                    "帮我做一个心理健康自评，看看我的焦虑和压力水平",
                    "帮我生成最近的身心健康趋势图表"
                )
            )
        )
    }

    val accents = listOf(cs.primary, cs.tertiary, cs.secondary, cs.primary)
    var expandedTitle by remember { mutableStateOf<String?>(null) }
    var inputText by remember { mutableStateOf("") }

    Surface(color = cs.background, modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 顶栏
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = space.md, vertical = space.xs),
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavIconButton(icon = Icons.Outlined.Menu, contentDesc = "菜单", onClick = onMenuClick)
                Spacer(Modifier.weight(1f))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LingLogo(size = 30.dp)
                    Spacer(Modifier.width(3.dp))
                    Text(
                        text = "ing",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 19.sp,
                            lineHeight = 24.sp,
                            letterSpacing = 0.5.sp,
                            fontWeight = FontWeight.Normal
                        ),
                        color = cs.onSurface
                    )
                }
                Spacer(Modifier.weight(1f))
                NavIconButton(icon = Icons.Outlined.Person, contentDesc = "个人资料", onClick = onProfileClick)
            }

            // 主体（含 hero + cards + CTA，整体可滚）
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = space.pageHorizontal)
            ) {
                Spacer(Modifier.height(space.md))

                FadeInRise(delayMs = 0) {
                    SunMoonOrnament(
                        primary = cs.primary,
                        accent = cs.tertiary,
                        backdrop = cs.primaryContainer
                    )
                }

                Spacer(Modifier.height(space.sm))

                FadeInRise(delayMs = 80) {
                    Text(
                        text = greetingByHour(),
                        style = MaterialTheme.typography.headlineLarge,
                        color = cs.onBackground
                    )
                }
                Spacer(Modifier.height(space.xxs))
                FadeInRise(delayMs = 140) {
                    Text(
                        text = "今天，有什么我可以陪你做的？",
                        style = MaterialTheme.typography.bodyMedium,
                        color = cs.onSurfaceVariant
                    )
                }

                Spacer(Modifier.height(space.lg))

                FadeInRise(delayMs = 200) {
                    SectionLabel("你想做什么")
                }
                Spacer(Modifier.height(space.sm))

                categories.forEachIndexed { index, cat ->
                    FadeInRise(delayMs = 260 + index * 60) {
                        SuggestionCategoryCard(
                            category = cat,
                            accent = accents[index % accents.size],
                            expanded = expandedTitle == cat.title,
                            onToggle = {
                                expandedTitle = if (expandedTitle == cat.title) null else cat.title
                            },
                            // 点击 case 不直接发送，先填到输入框，让用户编辑后再发
                            onCaseClick = { caseText ->
                                inputText = caseText
                                expandedTitle = null
                            }
                        )
                    }
                    Spacer(Modifier.height(space.xs))
                }

                Spacer(Modifier.height(space.xl))

                // 输入栏（取代「开始新的对话」按钮）
                FadeInRise(delayMs = 540) {
                    WelcomeInputBar(
                        text = inputText,
                        onTextChange = { inputText = it },
                        onSend = {
                            if (inputText.isNotBlank()) {
                                val msg = inputText
                                inputText = ""
                                onExampleClick(msg)
                            }
                        }
                    )
                }

                Spacer(Modifier.height(space.md))
                Spacer(Modifier.navigationBarsPadding())
            }
        }
    }
}

// =====================================================
//  数据
// =====================================================

private data class SuggestionCategory(
    val icon: ImageVector,
    val title: String,
    val cases: List<String>
)

// =====================================================
//  子组件
// =====================================================

@Composable
private fun NavIconButton(icon: ImageVector, contentDesc: String, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDesc,
            tint = cs.onSurface,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    val cs = MaterialTheme.colorScheme
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .width(14.dp)
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

/**
 * 可展开分类卡 — Header 永远可见，第一个 case 永远可见，其余 cases 展开后显示
 */
@Composable
private fun SuggestionCategoryCard(
    category: SuggestionCategory,
    accent: Color,
    expanded: Boolean,
    onToggle: () -> Unit,
    onCaseClick: (String) -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val shapes = LingTheme.shapes
    val space = LingTheme.spacing
    val arrowRotation by animateFloatAsState(
        targetValue = if (expanded) 90f else 0f,
        animationSpec = tween(220),
        label = "arrow"
    )

    Surface(
        shape = shapes.md,
        color = cs.surface,
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = cs.outlineVariant, shape = shapes.md)
    ) {
        Column(
            modifier = Modifier.animateContentSize(animationSpec = tween(240))
        ) {
            // Header（点击展开/折叠）
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle)
                    .padding(horizontal = space.md, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(accent.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = category.icon,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(15.dp)
                    )
                }
                Spacer(Modifier.width(space.sm))
                Text(
                    text = category.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = cs.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = if (expanded) "折叠" else "展开",
                    tint = cs.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier
                        .size(18.dp)
                        .rotate(arrowRotation)
                )
            }

            // 分隔线（细，header 与 cases 间）
            Box(
                modifier = Modifier
                    .padding(horizontal = space.md)
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .background(cs.outlineVariant.copy(alpha = 0.5f))
            )

            // 第一个 case 永远可见
            CaseRow(
                text = category.cases.first(),
                accent = accent,
                onClick = { onCaseClick(category.cases.first()) }
            )

            // 展开后剩余 cases
            if (expanded) {
                category.cases.drop(1).forEach { caseText ->
                    CaseRow(
                        text = caseText,
                        accent = accent,
                        onClick = { onCaseClick(caseText) }
                    )
                }
            }

            Spacer(Modifier.height(space.xxs))
        }
    }
}

/**
 * 单条 case — 左侧细色条 + 文字（紧凑）
 */
@Composable
private fun CaseRow(
    text: String,
    accent: Color,
    onClick: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val shapes = LingTheme.shapes
    val space = LingTheme.spacing
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = space.sm)
            .clip(shapes.sm)
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick
            )
            .background(
                color = if (pressed) accent.copy(alpha = 0.08f) else Color.Transparent,
                shape = shapes.sm
            )
            .padding(horizontal = space.sm, vertical = 6.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 5.dp)
                .width(2.dp)
                .height(12.dp)
                .background(accent.copy(alpha = 0.5f))
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 14.sp,
                lineHeight = 20.sp
            ),
            color = if (pressed) accent else cs.onSurfaceVariant.copy(alpha = 0.85f)
        )
    }
}

/**
 * 输入栏（取代之前的"开始新的对话"按钮）
 *  · 输入框（多行自适应 44~140dp）+ 圆形发送按钮
 *  · 点击 case 后内容会被填进来，用户可编辑再发
 */
@Composable
private fun WelcomeInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val shapes = LingTheme.shapes
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val canSend = text.isNotBlank()

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Surface(
            shape = shapes.md,
            color = cs.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 44.dp, max = 140.dp)
                .border(
                    width = 1.dp,
                    color = if (focused) cs.primary else cs.outlineVariant.copy(alpha = 0.7f),
                    shape = shapes.md
                )
        ) {
            Box(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp)
            ) {
                BasicTextField(
                    value = text,
                    onValueChange = onTextChange,
                    interactionSource = interaction,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = cs.onSurface,
                        lineHeight = 20.sp
                    ),
                    cursorBrush = SolidColor(cs.primary)
                )
                if (text.isEmpty()) {
                    Text(
                        text = "和 Ling 聊聊…",
                        style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
                        color = cs.onSurfaceVariant.copy(alpha = 0.55f)
                    )
                }
            }
        }

        // 圆形发送按钮
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(if (canSend) cs.primary else cs.surfaceVariant)
                .then(if (canSend) Modifier.clickable(onClick = onSend) else Modifier),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "发送",
                tint = if (canSend) cs.onPrimary else cs.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/** 抽象「日月」装饰（紧凑版） */
@Composable
private fun SunMoonOrnament(primary: Color, accent: Color, backdrop: Color) {
    Canvas(modifier = Modifier.size(width = 78.dp, height = 52.dp)) {
        val w = size.width
        val h = size.height
        drawCircle(
            color = backdrop,
            radius = h * 0.55f,
            center = Offset(w * 0.32f, h * 0.55f)
        )
        drawCircle(
            color = accent.copy(alpha = 0.55f),
            radius = h * 0.32f,
            center = Offset(w * 0.62f, h * 0.40f)
        )
        drawCircle(
            color = primary,
            radius = h * 0.10f,
            center = Offset(w * 0.85f, h * 0.22f)
        )
    }
}

private fun greetingByHour(): String {
    val h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (h) {
        in 5..10 -> "早上好"
        in 11..12 -> "中午好"
        in 13..17 -> "下午好"
        in 18..22 -> "晚上好"
        else -> "夜深了"
    }
}
