package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val PWSaraColorScheme = darkColorScheme(
    primary = GateRedPrimary,
    onPrimary = GateWhite,
    primaryContainer = GateRedDark,
    onPrimaryContainer = GateWhite,
    secondary = GateRedLight,
    onSecondary = GateWhite,
    secondaryContainer = GateCardElevated,
    onSecondaryContainer = GateWhite,
    tertiary = GateAmber,
    onTertiary = Color.Black,
    background = GateBlack,
    onBackground = GateWhite,
    surface = GateDarkGray,
    onSurface = GateWhite,
    surfaceVariant = GateCard,
    onSurfaceVariant = GateTextSecondary,
    outline = GateBorder,
    outlineVariant = GateBorderRed,
    error = GateRedLight,
    onError = GateWhite
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Always enforce the signature PW SARA Black & Red aesthetic
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = PWSaraColorScheme,
        typography = Typography,
        content = content
    )
}
