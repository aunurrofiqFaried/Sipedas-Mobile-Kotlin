package com.sipedas.app.ui.notifikasi

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sipedas.app.ui.theme.Green600
import kotlinx.coroutines.delay

private const val LAMA_TAMPIL_MS = 3500L

// Kartu notifikasi yang turun dari atas, seperti notifikasi di panel Filament
@Composable
fun NotifikasiHost(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    val antrean = remember { ArrayDeque<NotifEvent>() }
    var aktif by remember { mutableStateOf<NotifEvent?>(null) }
    var terakhir by remember { mutableStateOf<NotifEvent?>(null) } // untuk isi kartu saat animasi keluar
    var tampil by remember { mutableStateOf(false) }

    // Terima kejadian: tampilkan langsung kalau kosong, kalau tidak masuk antrean
    LaunchedEffect(Unit) {
        Notifikasi.events.collect { e ->
            if (aktif == null) {
                aktif = e
            } else {
                antrean.addLast(e)
            }
        }
    }

    // Tampilkan, bunyikan (khusus sukses), tunggu, lalu sembunyikan dan lanjut ke antrean
    LaunchedEffect(aktif) {
        val e = aktif ?: return@LaunchedEffect

        terakhir = e
        tampil = true

        if (e.jenis == JenisNotif.SUKSES) PemutarNotifikasi.putar(context)

        delay(LAMA_TAMPIL_MS)
        tampil = false
        delay(300) // beri waktu animasi keluar

        aktif = antrean.removeFirstOrNull()
    }

    AnimatedVisibility(
        visible = tampil,
        enter = slideInVertically { -it } + fadeIn(),
        exit = slideOutVertically { -it } + fadeOut(),
        modifier = modifier,
    ) {
        terakhir?.let { e ->
            KartuNotifikasi(
                e = e,
                onTutup = {
                    tampil = false
                    aktif = antrean.removeFirstOrNull()
                },
            )
        }
    }
}

@Composable
private fun KartuNotifikasi(e: NotifEvent, onTutup: () -> Unit) {
    val (ikon, warna) = when (e.jenis) {
        JenisNotif.SUKSES -> Icons.Filled.CheckCircle to Green600
        JenisNotif.PERINGATAN -> Icons.Filled.Warning to Color(0xFFD97706)
        JenisNotif.GALAT -> Icons.Filled.Warning to Color(0xFFDC2626)
        JenisNotif.INFO -> Icons.Filled.Info to Color(0xFF0284C7)
    }

    val shape = RoundedCornerShape(16.dp)

    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth()
            .shadow(12.dp, shape, ambientColor = warna.copy(alpha = 0.3f), spotColor = warna.copy(alpha = 0.4f))
            .clip(shape)
            .background(Color.White)
            .border(1.dp, warna.copy(alpha = 0.25f), shape)
            .clickable(onClick = onTutup)
            .padding(14.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(warna.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(ikon, contentDescription = null, tint = warna, modifier = Modifier.size(22.dp))
        }

        Column(modifier = Modifier.padding(start = 12.dp)) {
            Text(
                e.judul,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            if (!e.pesan.isNullOrBlank()) {
                Text(
                    e.pesan,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}