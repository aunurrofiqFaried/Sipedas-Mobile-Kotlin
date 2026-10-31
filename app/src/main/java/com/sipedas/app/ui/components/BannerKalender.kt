package com.sipedas.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun BannerLibur(judul: String, keterangan: String?, modifier: Modifier = Modifier) {
    Banner("Libur · $judul", keterangan, Color(0xFFDC2626), modifier)
}

@Composable
fun BannerPeringatan(judul: String, keterangan: String?, modifier: Modifier = Modifier) {
    Banner("Hari ini · $judul", keterangan, Color(0xFF0284C7), modifier)
}

@Composable
private fun Banner(judul: String, keterangan: String?, warna: Color, modifier: Modifier) {
    val shape = RoundedCornerShape(16.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(warna.copy(alpha = 0.10f))
            .border(1.dp, warna.copy(alpha = 0.35f), shape)
            .padding(14.dp),
    ) {
        Text(
            judul,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = warna,
        )

        if (!keterangan.isNullOrBlank()) {
            Spacer(Modifier.height(4.dp))
            Text(keterangan, style = MaterialTheme.typography.bodyMedium)
        }
    }
}