package com.guyi.demo1.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * Warm Calm 通用装饰背景：暖光晕 mesh + 装饰圆环 + 浮动小圆点
 *
 * 三层光晕（顶部 primary、右中 tertiary、左下 primary 弱光）+ 两个细描边圆环
 * + 四个不同周期 / 相位的浮动光点。整体气息柔和、有"呼吸感"。
 */
@Composable
fun AmbientBackdrop(
    modifier: Modifier = Modifier,
    primary: Color,
    tertiary: Color,
    outlineSoft: Color
) {
    val drift1 = rememberDriftAnimation(durationMs = 8000, range = 12f)
    val drift2 = rememberDriftAnimation(durationMs = 10000, range = 16f, phaseShift = 0.3f)
    val drift3 = rememberDriftAnimation(durationMs = 12000, range = 10f, phaseShift = 0.6f)

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(primary.copy(alpha = 0.18f), Color.Transparent),
                center = Offset(w * 0.5f, h * -0.05f),
                radius = w * 0.85f
            ),
            radius = w * 0.85f,
            center = Offset(w * 0.5f, h * -0.05f)
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(tertiary.copy(alpha = 0.13f), Color.Transparent),
                center = Offset(w * 0.95f, h * 0.45f),
                radius = w * 0.6f
            ),
            radius = w * 0.6f,
            center = Offset(w * 0.95f, h * 0.45f)
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(primary.copy(alpha = 0.1f), Color.Transparent),
                center = Offset(w * 0.05f, h * 0.85f),
                radius = w * 0.55f
            ),
            radius = w * 0.55f,
            center = Offset(w * 0.05f, h * 0.85f)
        )

        drawCircle(
            color = outlineSoft,
            radius = w * 0.42f,
            center = Offset(w * 1.05f, h * 0.18f),
            style = Stroke(width = 1.dp.toPx())
        )
        drawCircle(
            color = outlineSoft,
            radius = w * 0.28f,
            center = Offset(w * -0.05f, h * 0.65f),
            style = Stroke(width = 1.dp.toPx())
        )

        drawCircle(
            color = primary.copy(alpha = 0.42f),
            radius = 4f,
            center = Offset(w * 0.10f, h * 0.32f + drift1)
        )
        drawCircle(
            color = tertiary.copy(alpha = 0.42f),
            radius = 3f,
            center = Offset(w * 0.85f, h * 0.22f + drift2)
        )
        drawCircle(
            color = primary.copy(alpha = 0.32f),
            radius = 4f,
            center = Offset(w * 0.78f, h * 0.78f + drift3)
        )
        drawCircle(
            color = tertiary.copy(alpha = 0.36f),
            radius = 3f,
            center = Offset(w * 0.18f, h * 0.72f + drift2 * 0.6f)
        )
    }
}

@Composable
private fun rememberDriftAnimation(
    durationMs: Int,
    range: Float,
    phaseShift: Float = 0f
): Float {
    val anim = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay((phaseShift * durationMs).toLong())
        anim.animateTo(
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMs, easing = EaseInOutSine),
                repeatMode = RepeatMode.Reverse
            )
        )
    }
    return (anim.value - 0.5f) * 2f * range
}
