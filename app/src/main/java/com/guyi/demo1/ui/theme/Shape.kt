package com.guyi.demo1.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.dp

/**
 * Warm Calm 形状系统
 *
 * 圆角语义：
 *   - xs (8)   → 小标签 / chip / 内嵌图标
 *   - sm (12)  → 输入框、小卡片、按钮（次级）
 *   - md (18)  → 卡片、列表项
 *   - lg (22)  → 主要 CTA 按钮、大卡片
 *   - xl (28)  → 容器、Sheet、对话框
 *   - pill     → 完全圆形（chip、头像、圆形按钮）
 */
@Immutable
data class LingShapes(
    val xs: RoundedCornerShape = RoundedCornerShape(8.dp),
    val sm: RoundedCornerShape = RoundedCornerShape(12.dp),
    val md: RoundedCornerShape = RoundedCornerShape(18.dp),
    val lg: RoundedCornerShape = RoundedCornerShape(22.dp),
    val xl: RoundedCornerShape = RoundedCornerShape(28.dp),
    val pill: RoundedCornerShape = RoundedCornerShape(999.dp)
)

val LocalLingShapes = compositionLocalOf { LingShapes() }

/** 注入到 Material 3 的 Shapes 系统，让默认组件也跟随 Warm Calm */
val WarmCalmMaterialShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)
