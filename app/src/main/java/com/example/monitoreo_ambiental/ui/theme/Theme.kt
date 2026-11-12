package com.example.monitoreo_ambiental.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val EcoMonitorColorScheme = darkColorScheme(
    primary = Color(0xFF55C98A),
    onPrimary = Color(0xFF10251D),
    secondary = Color(0xFF82D9A5),
    onSecondary = Color(0xFF10251D),
    background = Color(0xFF10251D),
    onBackground = Color(0xFFF1F5F2),
    surface = Color(0xFF19382B),
    onSurface = Color(0xFFF1F5F2),
    surfaceVariant = Color(0xFF254536),
    onSurfaceVariant = Color(0xFFB8C9BE),
    outline = Color(0xFF527461),
    error = Color(0xFFFF7777)
)

@Composable
fun Monitoreo_ambientalTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = EcoMonitorColorScheme,
        typography = Typography,
        content = content
    )
}

