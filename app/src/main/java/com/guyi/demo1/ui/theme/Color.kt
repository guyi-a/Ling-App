package com.guyi.demo1.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// ============================================================
//  Warm Calm — 共享中性底色
//  灵感：米白晨光、雾灰薄暮、温润纸面
// ============================================================

// Light 中性
private val NeutralBg = Color(0xFFF8F2E8)            // 温润米白（page background）
private val NeutralSurface = Color(0xFFFDFAF4)       // 卡片底（比 bg 略亮）
private val NeutralSurfaceVariant = Color(0xFFEDE5D6)// 次级卡片 / 输入框
private val NeutralSurfaceHigh = Color(0xFFE5DBC7)   // 强调容器
private val NeutralInk = Color(0xFF2A2520)           // 深墨棕（主文字，非纯黑）
private val NeutralInkSoft = Color(0xFF6B5F50)       // 次要文字（暖灰棕）
private val NeutralOutline = Color(0xFFC9BCA6)       // 描边
private val NeutralOutlineSoft = Color(0xFFDDD2BF)   // 弱描边
private val ScrimColor = Color(0xCC1B1612)

// Dark 中性（中性暗灰 — 几乎纯灰、极轻暖意）
private val NeutralBgD = Color(0xFF181817)            // 深暗灰（接近纯黑，无棕）
private val NeutralSurfaceD = Color(0xFF222221)       // 卡片底
private val NeutralSurfaceVariantD = Color(0xFF2C2C2B)// 次级卡片
private val NeutralSurfaceHighD = Color(0xFF3A3A39)
private val NeutralInkD = Color(0xFFECEAE6)           // 暖白（文字）
private val NeutralInkSoftD = Color(0xFFA09D98)       // 中性灰
private val NeutralOutlineD = Color(0xFF494846)
private val NeutralOutlineSoftD = Color(0xFF333231)

// Error（共享）— 砖窑红，避免太刺眼
private val ErrorLight = Color(0xFFA0463A)
private val ErrorContainerLight = Color(0xFFF8D7CF)
private val OnErrorContainerLight = Color(0xFF3F1810)
private val ErrorDark = Color(0xFFE8A399)
private val ErrorContainerDark = Color(0xFF5D2B22)
private val OnErrorContainerDark = Color(0xFFFFDAD2)

// ============================================================
//  Warm Calm — 5 套主题强调色
//  每套都是温润、低饱和、有泥土感的调子
// ============================================================

// 焦糖橙（Warm Calm 标志色）
private val OrangePrimaryL = Color(0xFFB5663A)
private val OrangeOnPrimaryL = Color(0xFFFFFFFF)
private val OrangePrimaryContainerL = Color(0xFFF8DBC4)
private val OrangeOnPrimaryContainerL = Color(0xFF3F1F0A)
private val OrangeSecondaryL = Color(0xFF8A6B4A)
private val OrangeOnSecondaryL = Color(0xFFFFFFFF)
private val OrangeSecondaryContainerL = Color(0xFFEEDBC2)
private val OrangeOnSecondaryContainerL = Color(0xFF2D1F10)
private val OrangeTertiaryL = Color(0xFF6B7A4A)
private val OrangeOnTertiaryL = Color(0xFFFFFFFF)
private val OrangeTertiaryContainerL = Color(0xFFE2EAC9)
private val OrangeOnTertiaryContainerL = Color(0xFF1F2810)

private val OrangePrimaryD = Color(0xFFE8A57E)
private val OrangeOnPrimaryD = Color(0xFF3F1F0A)
private val OrangePrimaryContainerD = Color(0xFF6B3A1E)
private val OrangeOnPrimaryContainerD = Color(0xFFFFE0CC)
private val OrangeSecondaryD = Color(0xFFD4B596)
private val OrangeOnSecondaryD = Color(0xFF2D1F10)
private val OrangeSecondaryContainerD = Color(0xFF4A3A24)
private val OrangeOnSecondaryContainerD = Color(0xFFF1E0CB)
private val OrangeTertiaryD = Color(0xFFB5C99E)
private val OrangeOnTertiaryD = Color(0xFF1F2810)
private val OrangeTertiaryContainerD = Color(0xFF374226)
private val OrangeOnTertiaryContainerD = Color(0xFFE0EBC9)

// 苔绿
private val GreenPrimaryL = Color(0xFF5A7A48)
private val GreenOnPrimaryL = Color(0xFFFFFFFF)
private val GreenPrimaryContainerL = Color(0xFFDCE8CB)
private val GreenOnPrimaryContainerL = Color(0xFF1A2810)
private val GreenSecondaryL = Color(0xFF7A6B4A)
private val GreenOnSecondaryL = Color(0xFFFFFFFF)
private val GreenSecondaryContainerL = Color(0xFFE8DEC4)
private val GreenOnSecondaryContainerL = Color(0xFF26200F)
private val GreenTertiaryL = Color(0xFFA8704A)
private val GreenOnTertiaryL = Color(0xFFFFFFFF)
private val GreenTertiaryContainerL = Color(0xFFF4D9C5)
private val GreenOnTertiaryContainerL = Color(0xFF35190C)

private val GreenPrimaryD = Color(0xFFB5C99E)
private val GreenOnPrimaryD = Color(0xFF1A2810)
private val GreenPrimaryContainerD = Color(0xFF344726)
private val GreenOnPrimaryContainerD = Color(0xFFD8E8C8)
private val GreenSecondaryD = Color(0xFFD4C497)
private val GreenOnSecondaryD = Color(0xFF26200F)
private val GreenSecondaryContainerD = Color(0xFF433925)
private val GreenOnSecondaryContainerD = Color(0xFFEFE0BB)
private val GreenTertiaryD = Color(0xFFE8B695)
private val GreenOnTertiaryD = Color(0xFF35190C)
private val GreenTertiaryContainerD = Color(0xFF5A3622)
private val GreenOnTertiaryContainerD = Color(0xFFFAD9C2)

// 雾蓝（保留蓝意，但去除冰冷）
private val BluePrimaryL = Color(0xFF4A6E7E)
private val BlueOnPrimaryL = Color(0xFFFFFFFF)
private val BluePrimaryContainerL = Color(0xFFD2DEE2)
private val BlueOnPrimaryContainerL = Color(0xFF0F2128)
private val BlueSecondaryL = Color(0xFF7A6F58)
private val BlueOnSecondaryL = Color(0xFFFFFFFF)
private val BlueSecondaryContainerL = Color(0xFFE5DCC4)
private val BlueOnSecondaryContainerL = Color(0xFF26200F)
private val BlueTertiaryL = Color(0xFFA0664D)
private val BlueOnTertiaryL = Color(0xFFFFFFFF)
private val BlueTertiaryContainerL = Color(0xFFF2D6C8)
private val BlueOnTertiaryContainerL = Color(0xFF34160B)

private val BluePrimaryD = Color(0xFFA3BFC9)
private val BlueOnPrimaryD = Color(0xFF0F2128)
private val BluePrimaryContainerD = Color(0xFF2C434C)
private val BlueOnPrimaryContainerD = Color(0xFFCFE0E6)
private val BlueSecondaryD = Color(0xFFD4C9A7)
private val BlueOnSecondaryD = Color(0xFF26200F)
private val BlueSecondaryContainerD = Color(0xFF433A28)
private val BlueOnSecondaryContainerD = Color(0xFFEFE3BE)
private val BlueTertiaryD = Color(0xFFE8B095)
private val BlueOnTertiaryD = Color(0xFF34160B)
private val BlueTertiaryContainerD = Color(0xFF583122)
private val BlueOnTertiaryContainerD = Color(0xFFFAD0BD)

// 陶土紫
private val PurplePrimaryL = Color(0xFF84576E)
private val PurpleOnPrimaryL = Color(0xFFFFFFFF)
private val PurplePrimaryContainerL = Color(0xFFE8D2DC)
private val PurpleOnPrimaryContainerL = Color(0xFF2D1320)
private val PurpleSecondaryL = Color(0xFF7A6B4A)
private val PurpleOnSecondaryL = Color(0xFFFFFFFF)
private val PurpleSecondaryContainerL = Color(0xFFE8DEC4)
private val PurpleOnSecondaryContainerL = Color(0xFF26200F)
private val PurpleTertiaryL = Color(0xFF6E7A48)
private val PurpleOnTertiaryL = Color(0xFFFFFFFF)
private val PurpleTertiaryContainerL = Color(0xFFE3EAC9)
private val PurpleOnTertiaryContainerL = Color(0xFF222810)

private val PurplePrimaryD = Color(0xFFD2A6BC)
private val PurpleOnPrimaryD = Color(0xFF2D1320)
private val PurplePrimaryContainerD = Color(0xFF523246)
private val PurpleOnPrimaryContainerD = Color(0xFFF2D9E5)
private val PurpleSecondaryD = Color(0xFFD4C497)
private val PurpleOnSecondaryD = Color(0xFF26200F)
private val PurpleSecondaryContainerD = Color(0xFF433925)
private val PurpleOnSecondaryContainerD = Color(0xFFEFE0BB)
private val PurpleTertiaryD = Color(0xFFB6C99E)
private val PurpleOnTertiaryD = Color(0xFF222810)
private val PurpleTertiaryContainerD = Color(0xFF374226)
private val PurpleOnTertiaryContainerD = Color(0xFFDFEBC9)

// 砖窑红
private val RedPrimaryL = Color(0xFF9C4E40)
private val RedOnPrimaryL = Color(0xFFFFFFFF)
private val RedPrimaryContainerL = Color(0xFFF0CDC4)
private val RedOnPrimaryContainerL = Color(0xFF3D1108)
private val RedSecondaryL = Color(0xFF7A6B4A)
private val RedOnSecondaryL = Color(0xFFFFFFFF)
private val RedSecondaryContainerL = Color(0xFFE8DEC4)
private val RedOnSecondaryContainerL = Color(0xFF26200F)
private val RedTertiaryL = Color(0xFF5A7A6E)
private val RedOnTertiaryL = Color(0xFFFFFFFF)
private val RedTertiaryContainerL = Color(0xFFD2E5DC)
private val RedOnTertiaryContainerL = Color(0xFF14281F)

private val RedPrimaryD = Color(0xFFE8A294)
private val RedOnPrimaryD = Color(0xFF3D1108)
private val RedPrimaryContainerD = Color(0xFF5D2B22)
private val RedOnPrimaryContainerD = Color(0xFFFAD8CD)
private val RedSecondaryD = Color(0xFFD4C497)
private val RedOnSecondaryD = Color(0xFF26200F)
private val RedSecondaryContainerD = Color(0xFF433925)
private val RedOnSecondaryContainerD = Color(0xFFEFE0BB)
private val RedTertiaryD = Color(0xFF98C0B0)
private val RedOnTertiaryD = Color(0xFF14281F)
private val RedTertiaryContainerD = Color(0xFF2E4439)
private val RedOnTertiaryContainerD = Color(0xFFD2EBDD)

// ============================================================
//  ColorScheme 构建器
// ============================================================

private fun buildLight(
    primary: Color, onPrimary: Color, primaryContainer: Color, onPrimaryContainer: Color,
    secondary: Color, onSecondary: Color, secondaryContainer: Color, onSecondaryContainer: Color,
    tertiary: Color, onTertiary: Color, tertiaryContainer: Color, onTertiaryContainer: Color
): ColorScheme = lightColorScheme(
    primary = primary,
    onPrimary = onPrimary,
    primaryContainer = primaryContainer,
    onPrimaryContainer = onPrimaryContainer,
    secondary = secondary,
    onSecondary = onSecondary,
    secondaryContainer = secondaryContainer,
    onSecondaryContainer = onSecondaryContainer,
    tertiary = tertiary,
    onTertiary = onTertiary,
    tertiaryContainer = tertiaryContainer,
    onTertiaryContainer = onTertiaryContainer,
    background = NeutralBg,
    onBackground = NeutralInk,
    surface = NeutralSurface,
    onSurface = NeutralInk,
    surfaceVariant = NeutralSurfaceVariant,
    onSurfaceVariant = NeutralInkSoft,
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFFBF6EC),
    surfaceContainer = NeutralSurface,
    surfaceContainerHigh = NeutralSurfaceVariant,
    surfaceContainerHighest = NeutralSurfaceHigh,
    surfaceTint = primary,
    inverseSurface = NeutralInk,
    inverseOnSurface = NeutralBg,
    inversePrimary = primary.copy(alpha = 0.85f),
    error = ErrorLight,
    onError = Color.White,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight,
    outline = NeutralOutline,
    outlineVariant = NeutralOutlineSoft,
    scrim = ScrimColor
)

private fun buildDark(
    primary: Color, onPrimary: Color, primaryContainer: Color, onPrimaryContainer: Color,
    secondary: Color, onSecondary: Color, secondaryContainer: Color, onSecondaryContainer: Color,
    tertiary: Color, onTertiary: Color, tertiaryContainer: Color, onTertiaryContainer: Color
): ColorScheme = darkColorScheme(
    primary = primary,
    onPrimary = onPrimary,
    primaryContainer = primaryContainer,
    onPrimaryContainer = onPrimaryContainer,
    secondary = secondary,
    onSecondary = onSecondary,
    secondaryContainer = secondaryContainer,
    onSecondaryContainer = onSecondaryContainer,
    tertiary = tertiary,
    onTertiary = onTertiary,
    tertiaryContainer = tertiaryContainer,
    onTertiaryContainer = onTertiaryContainer,
    background = NeutralBgD,
    onBackground = NeutralInkD,
    surface = NeutralSurfaceD,
    onSurface = NeutralInkD,
    surfaceVariant = NeutralSurfaceVariantD,
    onSurfaceVariant = NeutralInkSoftD,
    surfaceContainerLowest = Color(0xFF111110),
    surfaceContainerLow = Color(0xFF1D1D1C),
    surfaceContainer = NeutralSurfaceD,
    surfaceContainerHigh = NeutralSurfaceVariantD,
    surfaceContainerHighest = NeutralSurfaceHighD,
    surfaceTint = primary,
    inverseSurface = NeutralInkD,
    inverseOnSurface = NeutralBgD,
    inversePrimary = primary.copy(alpha = 0.85f),
    error = ErrorDark,
    onError = Color(0xFF3F1810),
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark,
    outline = NeutralOutlineD,
    outlineVariant = NeutralOutlineSoftD,
    scrim = ScrimColor
)

// ============================================================
//  导出的 5 套 ColorScheme
// ============================================================

internal val OrangeLightScheme = buildLight(
    OrangePrimaryL, OrangeOnPrimaryL, OrangePrimaryContainerL, OrangeOnPrimaryContainerL,
    OrangeSecondaryL, OrangeOnSecondaryL, OrangeSecondaryContainerL, OrangeOnSecondaryContainerL,
    OrangeTertiaryL, OrangeOnTertiaryL, OrangeTertiaryContainerL, OrangeOnTertiaryContainerL
)
internal val OrangeDarkScheme = buildDark(
    OrangePrimaryD, OrangeOnPrimaryD, OrangePrimaryContainerD, OrangeOnPrimaryContainerD,
    OrangeSecondaryD, OrangeOnSecondaryD, OrangeSecondaryContainerD, OrangeOnSecondaryContainerD,
    OrangeTertiaryD, OrangeOnTertiaryD, OrangeTertiaryContainerD, OrangeOnTertiaryContainerD
)

internal val GreenLightScheme = buildLight(
    GreenPrimaryL, GreenOnPrimaryL, GreenPrimaryContainerL, GreenOnPrimaryContainerL,
    GreenSecondaryL, GreenOnSecondaryL, GreenSecondaryContainerL, GreenOnSecondaryContainerL,
    GreenTertiaryL, GreenOnTertiaryL, GreenTertiaryContainerL, GreenOnTertiaryContainerL
)
internal val GreenDarkScheme = buildDark(
    GreenPrimaryD, GreenOnPrimaryD, GreenPrimaryContainerD, GreenOnPrimaryContainerD,
    GreenSecondaryD, GreenOnSecondaryD, GreenSecondaryContainerD, GreenOnSecondaryContainerD,
    GreenTertiaryD, GreenOnTertiaryD, GreenTertiaryContainerD, GreenOnTertiaryContainerD
)

internal val BlueLightScheme = buildLight(
    BluePrimaryL, BlueOnPrimaryL, BluePrimaryContainerL, BlueOnPrimaryContainerL,
    BlueSecondaryL, BlueOnSecondaryL, BlueSecondaryContainerL, BlueOnSecondaryContainerL,
    BlueTertiaryL, BlueOnTertiaryL, BlueTertiaryContainerL, BlueOnTertiaryContainerL
)
internal val BlueDarkScheme = buildDark(
    BluePrimaryD, BlueOnPrimaryD, BluePrimaryContainerD, BlueOnPrimaryContainerD,
    BlueSecondaryD, BlueOnSecondaryD, BlueSecondaryContainerD, BlueOnSecondaryContainerD,
    BlueTertiaryD, BlueOnTertiaryD, BlueTertiaryContainerD, BlueOnTertiaryContainerD
)

internal val PurpleLightScheme = buildLight(
    PurplePrimaryL, PurpleOnPrimaryL, PurplePrimaryContainerL, PurpleOnPrimaryContainerL,
    PurpleSecondaryL, PurpleOnSecondaryL, PurpleSecondaryContainerL, PurpleOnSecondaryContainerL,
    PurpleTertiaryL, PurpleOnTertiaryL, PurpleTertiaryContainerL, PurpleOnTertiaryContainerL
)
internal val PurpleDarkScheme = buildDark(
    PurplePrimaryD, PurpleOnPrimaryD, PurplePrimaryContainerD, PurpleOnPrimaryContainerD,
    PurpleSecondaryD, PurpleOnSecondaryD, PurpleSecondaryContainerD, PurpleOnSecondaryContainerD,
    PurpleTertiaryD, PurpleOnTertiaryD, PurpleTertiaryContainerD, PurpleOnTertiaryContainerD
)

internal val RedLightScheme = buildLight(
    RedPrimaryL, RedOnPrimaryL, RedPrimaryContainerL, RedOnPrimaryContainerL,
    RedSecondaryL, RedOnSecondaryL, RedSecondaryContainerL, RedOnSecondaryContainerL,
    RedTertiaryL, RedOnTertiaryL, RedTertiaryContainerL, RedOnTertiaryContainerL
)
internal val RedDarkScheme = buildDark(
    RedPrimaryD, RedOnPrimaryD, RedPrimaryContainerD, RedOnPrimaryContainerD,
    RedSecondaryD, RedOnSecondaryD, RedSecondaryContainerD, RedOnSecondaryContainerD,
    RedTertiaryD, RedOnTertiaryD, RedTertiaryContainerD, RedOnTertiaryContainerD
)
