package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = SaffronPrimary,
    onPrimary = DeepNavy,
    primaryContainer = SurfaceCardNavy,
    onPrimaryContainer = RadiantGold,
    secondary = TealAccent,
    onSecondary = DeepNavy,
    secondaryContainer = SurfaceNavy,
    onSecondaryContainer = EmeraldAccent,
    tertiary = SaffronLight,
    onTertiary = DeepNavy,
    background = MidnightNavy,
    onBackground = TextPrimaryDark,
    surface = DeepNavy,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceCardNavy,
    onSurfaceVariant = TextSecondaryDark,
    outline = SurfaceCardBorder,
    error = CoralRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimarySaffron,
    onPrimary = Color.White,
    primaryContainer = LightSurfaceVariant,
    onPrimaryContainer = LightPrimarySaffron,
    secondary = LightSecondaryTeal,
    onSecondary = Color.White,
    secondaryContainer = LightSurfaceVariant,
    onSecondaryContainer = LightSecondaryTeal,
    tertiary = SaffronDark,
    onTertiary = Color.White,
    background = LightCreamBg,
    onBackground = TextPrimaryLight,
    surface = LightSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = TextSecondaryLight,
    outline = Color(0xFFD6CEBF),
    error = CoralRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to deep midnight/gold aesthetic
    dynamicColor: Boolean = false, // Keep distinctive brand identity
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
