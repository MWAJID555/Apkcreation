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
    primary = VoltLime,
    onPrimary = OnVoltLime,
    primaryContainer = VoltLimeContainer,
    onPrimaryContainer = VoltLime,
    secondary = ElectricCyan,
    onSecondary = Color(0xFF001F24),
    secondaryContainer = CyanContainer,
    onSecondaryContainer = ElectricCyan,
    tertiary = RacingRed,
    onTertiary = Color.White,
    tertiaryContainer = RacingRedContainer,
    onTertiaryContainer = RacingRed,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF537E00),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD7FA6A),
    onPrimaryContainer = Color(0xFF142000),
    secondary = Color(0xFF006876),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFA1EFFF),
    onSecondaryContainer = Color(0xFF001F24),
    tertiary = Color(0xFFB32635),
    onTertiary = Color.White,
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to sleek athletic dark theme
    dynamicColor: Boolean = false, // Keep distinctive sports chronograph identity
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
