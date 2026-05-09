package com.guyi.demo1.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

/**
 * Warm Calm — Ling-App 设计系统入口
 *
 * 三层 token：
 *   - ColorScheme（5 套主题色 × Light/Dark）
 *   - Typography（Serif 标题 + Sans 正文，可缩放）
 *   - Shapes / Spacing（通过 CompositionLocal 暴露）
 *
 * 使用：
 *   - 颜色：MaterialTheme.colorScheme.primary
 *   - 字体：MaterialTheme.typography.headlineMedium
 *   - 形状：LingTheme.shapes.md
 *   - 间距：LingTheme.spacing.lg
 */
@Composable
fun Demo1Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    themeColor: String = "ORANGE",
    fontSizeName: String = "MEDIUM",
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeColor) {
        "PURPLE" -> if (darkTheme) PurpleDarkScheme else PurpleLightScheme
        "GREEN" -> if (darkTheme) GreenDarkScheme else GreenLightScheme
        "BLUE" -> if (darkTheme) BlueDarkScheme else BlueLightScheme
        "RED" -> if (darkTheme) RedDarkScheme else RedLightScheme
        else -> if (darkTheme) OrangeDarkScheme else OrangeLightScheme  // ORANGE 为默认
    }

    val typography = warmCalmTypography(fontSizeScale(fontSizeName))

    CompositionLocalProvider(
        LocalLingShapes provides LingShapes(),
        LocalLingSpacing provides LingSpacing()
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            shapes = WarmCalmMaterialShapes,
            content = content
        )
    }
}

/** 访问 Warm Calm 设计 token 的入口 */
object LingTheme {
    val shapes: LingShapes
        @Composable @ReadOnlyComposable
        get() = LocalLingShapes.current

    val spacing: LingSpacing
        @Composable @ReadOnlyComposable
        get() = LocalLingSpacing.current
}
