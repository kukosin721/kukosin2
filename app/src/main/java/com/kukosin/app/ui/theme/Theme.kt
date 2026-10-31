package com.kukosin.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/**
 * Esquema Material derivado del color de acento elegido.
 *
 * Antes era un `val` de nivel superior construido con el dorado fijo: al cambiar el acento,
 * el resto de la app se repintaba pero MaterialTheme seguía en dorado (chips, sliders,
 * ripples). Ahora se arma con el acento actual y se recalcula solo cuando cambia.
 */
private fun hifiScheme(accent: androidx.compose.ui.graphics.Color, accentDim: androidx.compose.ui.graphics.Color) =
    darkColorScheme(
        primary = accent,
        onPrimary = Ink,
        primaryContainer = accentDim,
        onPrimaryContainer = Cream,
        secondary = CreamMuted,
        onSecondary = Ink,
        background = Ink,
        onBackground = Cream,
        surface = Ink,
        onSurface = Cream,
        surfaceVariant = InkElevated,
        onSurfaceVariant = CreamMuted,
        outline = Line,
        outlineVariant = Line,
    )

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    val accent = AccentTheme.color
    val accentDim = AccentTheme.colorDim
    val scheme = remember(accent, accentDim) { hifiScheme(accent, accentDim) }

    MaterialTheme(
        colorScheme = scheme,
        typography = Typography,
        content = content,
    )
}
