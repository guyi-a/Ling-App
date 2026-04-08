package com.guyi.demo1.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

// 紫色（默认）
private val PurpleDarkScheme = darkColorScheme(
    primary = Purple80, secondary = PurpleGrey80, tertiary = Pink80
)
private val PurpleLightScheme = lightColorScheme(
    primary = Purple40, secondary = PurpleGrey40, tertiary = Pink40
)

// 蓝色
private val BlueDarkScheme = darkColorScheme(
    primary = Blue80, secondary = BlueGrey80, tertiary = BluePink80
)
private val BlueLightScheme = lightColorScheme(
    primary = Blue40, secondary = BlueGrey40, tertiary = BluePink40
)

// 绿色
private val GreenDarkScheme = darkColorScheme(
    primary = Green80, secondary = GreenGrey80, tertiary = GreenPink80
)
private val GreenLightScheme = lightColorScheme(
    primary = Green40, secondary = GreenGrey40, tertiary = GreenPink40
)

// 橙色
private val OrangeDarkScheme = darkColorScheme(
    primary = Orange80, secondary = OrangeGrey80, tertiary = OrangePink80
)
private val OrangeLightScheme = lightColorScheme(
    primary = Orange40, secondary = OrangeGrey40, tertiary = OrangePink40
)

// 红色
private val RedDarkScheme = darkColorScheme(
    primary = Red80, secondary = RedGrey80, tertiary = RedPink80
)
private val RedLightScheme = lightColorScheme(
    primary = Red40, secondary = RedGrey40, tertiary = RedPink40
)

@Composable
fun Demo1Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    themeColor: String = "BLUE",
    fontSizeName: String = "MEDIUM",
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeColor) {
        "PURPLE" -> if (darkTheme) PurpleDarkScheme else PurpleLightScheme
        "GREEN" -> if (darkTheme) GreenDarkScheme else GreenLightScheme
        "ORANGE" -> if (darkTheme) OrangeDarkScheme else OrangeLightScheme
        "RED" -> if (darkTheme) RedDarkScheme else RedLightScheme
        else -> if (darkTheme) BlueDarkScheme else BlueLightScheme // BLUE 为默认
    }

    val typography = if (fontSizeName == "MEDIUM") Typography else scaledTypography(fontSizeScale(fontSizeName))

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = content
    )
}
