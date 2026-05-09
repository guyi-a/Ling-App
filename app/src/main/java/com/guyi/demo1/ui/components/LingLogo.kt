package com.guyi.demo1.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.guyi.demo1.R

/**
 * Ling 应用 Logo（形状沿用 Web 端 favicon）
 *
 * 默认用主题 primary 色染色，会随主题切换而变化；
 * 可传入 [tint] 覆盖（例如在白底容器上反白时）。
 */
@Composable
fun LingLogo(
    modifier: Modifier = Modifier,
    size: Dp = 28.dp,
    tint: Color = MaterialTheme.colorScheme.primary,
    contentDescription: String? = "Ling"
) {
    Icon(
        painter = painterResource(R.drawable.ic_ling_logo),
        contentDescription = contentDescription,
        tint = tint,
        modifier = modifier.size(size)
    )
}
