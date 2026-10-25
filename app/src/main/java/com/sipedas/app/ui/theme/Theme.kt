package com.sipedas.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val SipedasColors = lightColorScheme(
    primary = Orange600,
    onPrimary = Color.White,
    primaryContainer = Orange100,
    onPrimaryContainer = Orange900,
    secondary = Green600,
    onSecondary = Color.White,
    secondaryContainer = Green100,
    onSecondaryContainer = Green900,
    tertiary = Green700,
    background = Color.White,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
    surfaceVariant = Color(0xFFF3F4F6),
    onSurfaceVariant = InkSoft,
    outline = Line,
    error = Danger,
    onError = Color.White,
)

private val SipedasShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
)

// Sengaja hanya mode terang: aplikasi ini dominan putih
@Composable
fun SipedasTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SipedasColors,
        typography = Typography,
        shapes = SipedasShapes,
        content = content,
    )
}