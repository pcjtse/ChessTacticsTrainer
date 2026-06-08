package com.example.chesstacticstrainer.presentation.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape

// ── Colour schemes ────────────────────────────────────────────────────────────

private val LightColorScheme = lightColorScheme(
    primary               = Green40,
    onPrimary             = Neutral99,
    primaryContainer      = Green90,
    onPrimaryContainer    = Green10,

    secondary             = Amber40,
    onSecondary           = Neutral99,
    secondaryContainer    = Amber90,
    onSecondaryContainer  = Amber10,

    tertiary              = Orange40,
    onTertiary            = Neutral99,
    tertiaryContainer     = Orange90,
    onTertiaryContainer   = Orange10,

    error                 = ErrorBase,
    onError               = Neutral99,
    errorContainer        = Error90,
    onErrorContainer      = Error10,

    background            = Neutral99,
    onBackground          = Neutral10,
    surface               = Neutral99,
    onSurface             = Neutral10,
    surfaceVariant        = NeutralVar90,
    onSurfaceVariant      = NeutralVar30,
    outline               = NeutralVar50,
    outlineVariant        = NeutralVar80,
    scrim                 = Neutral10,
)

private val DarkColorScheme = darkColorScheme(
    primary               = Green80,
    onPrimary             = Green20,
    primaryContainer      = Green30,
    onPrimaryContainer    = Green90,

    secondary             = Amber80,
    onSecondary           = Amber20,
    secondaryContainer    = Amber30,
    onSecondaryContainer  = Amber90,

    tertiary              = Orange80,
    onTertiary            = Orange20,
    tertiaryContainer     = Orange30,
    onTertiaryContainer   = Orange90,

    error                 = Error80,
    onError               = Error20,
    errorContainer        = ErrorBase,
    onErrorContainer      = Error90,

    background            = Neutral10,
    onBackground          = Neutral90,
    surface               = Neutral10,
    onSurface             = Neutral90,
    surfaceVariant        = NeutralVar30,
    onSurfaceVariant      = NeutralVar80,
    outline               = NeutralVar50,
    outlineVariant        = NeutralVar30,
    scrim                 = Neutral10,
)

// ── Typography ────────────────────────────────────────────────────────────────
// Uses MD3 type scale; adjusting a few weights for chess UI clarity.

private val AppTypography = Typography(
    headlineLarge  = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold,   lineHeight = 40.sp),
    headlineMedium = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.SemiBold, lineHeight = 36.sp),
    headlineSmall  = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.SemiBold, lineHeight = 32.sp),
    titleLarge     = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold,   lineHeight = 28.sp),
    titleMedium    = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold, lineHeight = 24.sp, letterSpacing = 0.15.sp),
    titleSmall     = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, lineHeight = 20.sp, letterSpacing = 0.1.sp),
    bodyLarge      = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal, lineHeight = 24.sp, letterSpacing = 0.5.sp),
    bodyMedium     = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal, lineHeight = 20.sp, letterSpacing = 0.25.sp),
    bodySmall      = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Normal, lineHeight = 16.sp, letterSpacing = 0.4.sp),
    labelLarge     = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium, lineHeight = 20.sp, letterSpacing = 0.1.sp),
    labelMedium    = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium, lineHeight = 16.sp, letterSpacing = 0.5.sp),
    labelSmall     = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium, lineHeight = 16.sp, letterSpacing = 0.5.sp),
)

// ── Shapes ────────────────────────────────────────────────────────────────────
// MD3 canonical sizes: xSmall 4, small 8, medium 12, large 16, xLarge 28

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small      = RoundedCornerShape(8.dp),
    medium     = RoundedCornerShape(12.dp),
    large      = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

// ── Theme composable ──────────────────────────────────────────────────────────

@Composable
fun ChessTacticsTrainerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color uses the system wallpaper palette on Android 12+.
    // minSdk = 37 so Build check is not strictly required, but kept for clarity.
    dynamicColor: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme    -> DarkColorScheme
        else         -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = AppTypography,
        shapes      = AppShapes,
        content     = content
    )
}
