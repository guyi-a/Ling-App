package com.guyi.demo1.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Warm Calm 字体系统
 *
 * 设计语言：
 *   - Display / Headline / TitleLarge → 系统 Serif（中文回退到思源宋体/苹方衬线）
 *     用于问候、页面大标题、章节封面等"有仪式感"的场景
 *   - Title (Medium/Small) / Body / Label → 系统 SansSerif
 *     用于正文阅读、按钮、标签等"功能性"场景
 *
 * 字距和字重经过手工调校：
 *   - 大标题字距收紧、字重偏轻 → 显得克制有质感
 *   - 正文标准字距、Normal 字重 → 阅读舒适
 *   - 按钮/标签略加字距、Medium 字重 → 有"印章感"
 */

private val Serif = FontFamily.Serif
private val Sans = FontFamily.SansSerif

private fun displayStyle(size: Int, line: Int) = TextStyle(
    fontFamily = Serif,
    fontWeight = FontWeight.Light,
    fontSize = size.sp,
    lineHeight = line.sp,
    letterSpacing = (-0.4).sp
)

private fun headlineStyle(size: Int, line: Int) = TextStyle(
    fontFamily = Serif,
    fontWeight = FontWeight.Normal,
    fontSize = size.sp,
    lineHeight = line.sp,
    letterSpacing = (-0.2).sp
)

private fun titleSerifStyle(size: Int, line: Int) = TextStyle(
    fontFamily = Serif,
    fontWeight = FontWeight.Medium,
    fontSize = size.sp,
    lineHeight = line.sp,
    letterSpacing = 0.sp
)

private fun titleSansStyle(size: Int, line: Int, weight: FontWeight = FontWeight.SemiBold) = TextStyle(
    fontFamily = Sans,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = line.sp,
    letterSpacing = 0.1.sp
)

private fun bodyStyle(size: Int, line: Int) = TextStyle(
    fontFamily = Sans,
    fontWeight = FontWeight.Normal,
    fontSize = size.sp,
    lineHeight = line.sp,
    letterSpacing = 0.2.sp
)

private fun labelStyle(size: Int, line: Int, weight: FontWeight = FontWeight.Medium) = TextStyle(
    fontFamily = Sans,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = line.sp,
    letterSpacing = 0.5.sp
)

/** 默认字号下的 Typography（MEDIUM = 1.0×） */
val WarmCalmTypography = Typography(
    displayLarge = displayStyle(56, 64),
    displayMedium = displayStyle(44, 52),
    displaySmall = displayStyle(36, 44),
    headlineLarge = headlineStyle(32, 40),
    headlineMedium = headlineStyle(26, 34),
    headlineSmall = headlineStyle(22, 30),
    titleLarge = titleSerifStyle(20, 28),
    titleMedium = titleSansStyle(16, 24),
    titleSmall = titleSansStyle(14, 20),
    bodyLarge = bodyStyle(16, 24),
    bodyMedium = bodyStyle(14, 22),
    bodySmall = bodyStyle(12, 18),
    labelLarge = labelStyle(14, 20),
    labelMedium = labelStyle(12, 16),
    labelSmall = labelStyle(11, 14)
)

/**
 * 按缩放系数生成完整 Typography（保留所有 15 个 slot 的字体/字重/字距）
 * @param scale 0.85 / 1.0 / 1.15 / 1.3
 */
fun warmCalmTypography(scale: Float): Typography {
    if (scale == 1f) return WarmCalmTypography

    fun TextStyle.scaled(s: Int, l: Int) = copy(
        fontSize = (s * scale).sp,
        lineHeight = (l * scale).sp
    )

    return Typography(
        displayLarge = WarmCalmTypography.displayLarge.scaled(56, 64),
        displayMedium = WarmCalmTypography.displayMedium.scaled(44, 52),
        displaySmall = WarmCalmTypography.displaySmall.scaled(36, 44),
        headlineLarge = WarmCalmTypography.headlineLarge.scaled(32, 40),
        headlineMedium = WarmCalmTypography.headlineMedium.scaled(26, 34),
        headlineSmall = WarmCalmTypography.headlineSmall.scaled(22, 30),
        titleLarge = WarmCalmTypography.titleLarge.scaled(20, 28),
        titleMedium = WarmCalmTypography.titleMedium.scaled(16, 24),
        titleSmall = WarmCalmTypography.titleSmall.scaled(14, 20),
        bodyLarge = WarmCalmTypography.bodyLarge.scaled(16, 24),
        bodyMedium = WarmCalmTypography.bodyMedium.scaled(14, 22),
        bodySmall = WarmCalmTypography.bodySmall.scaled(12, 18),
        labelLarge = WarmCalmTypography.labelLarge.scaled(14, 20),
        labelMedium = WarmCalmTypography.labelMedium.scaled(12, 16),
        labelSmall = WarmCalmTypography.labelSmall.scaled(11, 14)
    )
}

fun fontSizeScale(name: String): Float = when (name) {
    "SMALL" -> 0.85f
    "LARGE" -> 1.15f
    "EXTRA_LARGE" -> 1.3f
    else -> 1.0f
}
