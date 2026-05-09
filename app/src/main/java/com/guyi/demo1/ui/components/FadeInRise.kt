package com.guyi.demo1.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.delay

/**
 * Warm Calm 标准入场动画：fade + 微 slide-up
 *
 * @param delayMs 延迟启动毫秒数；多元素 stagger 时按 60~80ms 递增更自然
 * @param riseFromPx 起始向下偏移像素（默认 20px，越大越"落入"感）
 * @param durationMs 动画时长
 */
@Composable
fun FadeInRise(
    delayMs: Int = 0,
    riseFromPx: Float = 20f,
    durationMs: Int = 480,
    content: @Composable () -> Unit
) {
    val alpha = remember { Animatable(0f) }
    val translateY = remember { Animatable(riseFromPx) }
    LaunchedEffect(Unit) {
        delay(delayMs.toLong())
        alpha.animateTo(1f, tween(durationMs, easing = EaseOutCubic))
    }
    LaunchedEffect(Unit) {
        delay(delayMs.toLong())
        translateY.animateTo(0f, tween(durationMs, easing = EaseOutCubic))
    }
    Box(
        modifier = Modifier
            .alpha(alpha.value)
            .graphicsLayer { translationY = translateY.value }
    ) { content() }
}
