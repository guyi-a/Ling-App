package com.guyi.demo1.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Warm Calm 间距系统（8 点栅格 + 关键中间值）
 *
 *   xxs  4    紧贴元素之间（图标和文字）
 *   xs   8    单元间小距离
 *   sm   12   inline 间距（卡片内 row 之间）
 *   md   16   组件标准内边距
 *   lg   20   段落间距 / 卡片内边距
 *   xl   24   章节间距 / 页面水平边距
 *   xxl  32   大块分隔
 *   xxxl 48   首屏巨型留白
 *   huge 64   英雄区留白
 */
@Immutable
data class LingSpacing(
    val xxs: Dp = 4.dp,
    val xs: Dp = 8.dp,
    val sm: Dp = 12.dp,
    val md: Dp = 16.dp,
    val lg: Dp = 20.dp,
    val xl: Dp = 24.dp,
    val xxl: Dp = 32.dp,
    val xxxl: Dp = 48.dp,
    val huge: Dp = 64.dp,

    /** 页面默认水平 padding */
    val pageHorizontal: Dp = 20.dp,
    /** 页面默认垂直 padding */
    val pageVertical: Dp = 16.dp
)

val LocalLingSpacing = compositionLocalOf { LingSpacing() }
