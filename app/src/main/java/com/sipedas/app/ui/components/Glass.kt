package com.sipedas.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.sipedas.app.ui.theme.Green600
import com.sipedas.app.ui.theme.Orange500
import androidx.compose.ui.unit.Dp

// Latar: putih dengan dua "cahaya" lembut, oranye di kanan atas dan hijau di kiri bawah
@Composable
fun AppBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color.White, Color(0xFFFFF7ED), Color.White)))
            .drawBehind {
                val radius = size.width * 0.8f

                val oranye = Offset(size.width * 0.95f, size.height * 0.06f)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Orange500.copy(alpha = 0.30f), Color.Transparent),
                        center = oranye,
                        radius = radius,
                    ),
                    radius = radius,
                    center = oranye,
                )

                val hijau = Offset(size.width * 0.05f, size.height * 0.94f)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Green600.copy(alpha = 0.26f), Color.Transparent),
                        center = hijau,
                        radius = radius,
                    ),
                    radius = radius,
                    center = hijau,
                )
            },
        content = content,
    )
}

// Kartu kaca: putih semi-transparan dengan tepi tipis yang bercahaya
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    contentPadding: Dp = 20.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(24.dp)

    Column(
        modifier = modifier
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(Color.White.copy(alpha = 0.82f), Color.White.copy(alpha = 0.52f))
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(Color.White, Orange500.copy(alpha = 0.25f))
                ),
                shape = shape,
            )
            .padding(contentPadding),
        content = content,
    )
}