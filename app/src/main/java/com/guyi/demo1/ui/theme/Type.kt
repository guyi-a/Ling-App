package com.guyi.demo1.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
)

/**
 * 根据字体缩放系数生成 Typography
 * @param scale 缩放系数：SMALL=0.85, MEDIUM=1.0, LARGE=1.15, EXTRA_LARGE=1.3
 */
fun scaledTypography(scale: Float): Typography {
    return Typography(
        displayLarge = Typography.displayLarge.copy(fontSize = (57 * scale).sp, lineHeight = (64 * scale).sp),
        displayMedium = Typography.displayMedium.copy(fontSize = (45 * scale).sp, lineHeight = (52 * scale).sp),
        displaySmall = Typography.displaySmall.copy(fontSize = (36 * scale).sp, lineHeight = (44 * scale).sp),
        headlineLarge = Typography.headlineLarge.copy(fontSize = (32 * scale).sp, lineHeight = (40 * scale).sp),
        headlineMedium = Typography.headlineMedium.copy(fontSize = (28 * scale).sp, lineHeight = (36 * scale).sp),
        headlineSmall = Typography.headlineSmall.copy(fontSize = (24 * scale).sp, lineHeight = (32 * scale).sp),
        titleLarge = Typography.titleLarge.copy(fontSize = (22 * scale).sp, lineHeight = (28 * scale).sp),
        titleMedium = Typography.titleMedium.copy(fontSize = (16 * scale).sp, lineHeight = (24 * scale).sp),
        titleSmall = Typography.titleSmall.copy(fontSize = (14 * scale).sp, lineHeight = (20 * scale).sp),
        bodyLarge = Typography.bodyLarge.copy(fontSize = (16 * scale).sp, lineHeight = (24 * scale).sp),
        bodyMedium = Typography.bodyMedium.copy(fontSize = (14 * scale).sp, lineHeight = (20 * scale).sp),
        bodySmall = Typography.bodySmall.copy(fontSize = (12 * scale).sp, lineHeight = (16 * scale).sp),
        labelLarge = Typography.labelLarge.copy(fontSize = (14 * scale).sp, lineHeight = (20 * scale).sp),
        labelMedium = Typography.labelMedium.copy(fontSize = (12 * scale).sp, lineHeight = (16 * scale).sp),
        labelSmall = Typography.labelSmall.copy(fontSize = (11 * scale).sp, lineHeight = (16 * scale).sp),
    )
}

/** 字体大小名称到缩放系数的映射 */
fun fontSizeScale(name: String): Float = when (name) {
    "SMALL" -> 0.85f
    "LARGE" -> 1.15f
    "EXTRA_LARGE" -> 1.3f
    else -> 1.0f // MEDIUM
}
