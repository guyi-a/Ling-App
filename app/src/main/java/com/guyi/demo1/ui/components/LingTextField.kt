package com.guyi.demo1.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.guyi.demo1.ui.theme.LingTheme

/**
 * Warm Calm 自定义输入框
 *  · 56dp 高、md 圆角
 *  · surfaceVariant 底色 + 1dp outlineVariant 描边
 *  · 聚焦时左侧 3dp 主题色竖条 + 描边变 primary
 *  · 错误态：error 描边
 *  · 左 leadingIcon 可选，右 trailingIcon 可选（适合密码可见性）
 */
@Composable
fun LingTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true
) {
    val cs = MaterialTheme.colorScheme
    val shapes = LingTheme.shapes
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()

    val borderColor by animateColorAsState(
        targetValue = when {
            isError -> cs.error
            focused -> cs.primary
            else -> cs.outlineVariant
        },
        animationSpec = tween(180),
        label = "border"
    )
    val accentBarColor by animateColorAsState(
        targetValue = when {
            isError -> cs.error
            focused -> cs.primary
            else -> Color.Transparent
        },
        animationSpec = tween(180),
        label = "accent"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(color = cs.surfaceVariant.copy(alpha = 0.5f), shape = shapes.md)
            .border(width = 1.dp, color = borderColor, shape = shapes.md)
    ) {
        // 左侧 accent bar（聚焦/错误时显示）
        Box(
            Modifier
                .padding(vertical = 12.dp)
                .padding(start = 1.dp)
                .width(3.dp)
                .height(32.dp)
                .background(accentBarColor)
                .align(Alignment.CenterStart)
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (leadingIcon != null) {
                androidx.compose.material3.Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = if (focused) cs.primary else cs.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(12.dp))
            }

            Box(modifier = Modifier.weight(1f)) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    enabled = enabled,
                    singleLine = singleLine,
                    interactionSource = interaction,
                    visualTransformation = visualTransformation,
                    keyboardOptions = keyboardOptions,
                    keyboardActions = keyboardActions,
                    cursorBrush = SolidColor(cs.primary),
                    textStyle = LocalTextStyle.current.copy(
                        color = cs.onSurface,
                        fontSize = MaterialTheme.typography.bodyLarge.fontSize
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyLarge,
                        color = cs.onSurfaceVariant.copy(alpha = 0.55f)
                    )
                }
            }

            if (trailingContent != null) {
                Spacer(Modifier.width(8.dp))
                trailingContent()
            }
        }
    }
}
