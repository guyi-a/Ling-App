package com.guyi.demo1.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.guyi.demo1.ui.theme.LingTheme

/**
 * Warm Calm 主 CTA 按钮
 *  · 22dp 圆角、56dp 高、primary 实色
 *  · 按下 0.97× 微缩
 *  · 支持 loading 状态（显示 spinner）
 *  · 默认带右箭头，可关掉
 */
@Composable
fun LingPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    showArrow: Boolean = true
) {
    val cs = MaterialTheme.colorScheme
    val shapes = LingTheme.shapes
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val pressScale = if (pressed && enabled && !loading) 0.97f else 1f
    val resolvedColor = if (enabled && !loading) cs.primary else cs.primary.copy(alpha = 0.4f)

    Surface(
        onClick = { if (enabled && !loading) onClick() },
        interactionSource = interaction,
        shape = shapes.lg,
        color = resolvedColor,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .scale(pressScale)
            .alpha(if (enabled || loading) 1f else 0.7f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = cs.onPrimary,
                    strokeWidth = 2.5.dp
                )
            } else {
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    ),
                    color = cs.onPrimary
                )
                if (showArrow) {
                    Spacer(Modifier.width(10.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = cs.onPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
