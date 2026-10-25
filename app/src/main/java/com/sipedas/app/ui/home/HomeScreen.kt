package com.sipedas.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sipedas.app.data.local.Session
import com.sipedas.app.data.model.JadwalDto
import com.sipedas.app.ui.components.GlassCard
import com.sipedas.app.ui.theme.Green100
import com.sipedas.app.ui.theme.Green600
import com.sipedas.app.ui.theme.Orange100
import com.sipedas.app.ui.theme.Orange500
import com.sipedas.app.ui.theme.Orange600
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    session: Session,
    onKeAbsensi: () -> Unit,
    onKeJurnal: () -> Unit,
    onKePoin: () -> Unit,
    // key = token: tiap login baru mendapat ViewModel baru, jadi data guru sebelumnya tidak terbawa
    viewModel: HomeViewModel = viewModel(key = session.token, factory = HomeViewModel.Factory),
) {
    PullToRefreshBox(
        isRefreshing = viewModel.refreshing,
        onRefresh = viewModel::muatUlang,
        modifier = Modifier.fillMaxSize(),
    ) {
        when (val state = viewModel.state) {
            HomeState.Loading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }

            is HomeState.Error -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(state.message, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(16.dp))
                Button(onClick = viewModel::cobaLagi) { Text("Coba lagi") }
            }

            // Sesi berakhir ditangani terpusat oleh ApiClient.onUnauthorized
            HomeState.Unauthorized -> Unit

            is HomeState.Success -> HomeContent(
                session = session,
                tanggal = state.data.tanggal,
                jadwal = state.data.data,
                onKeAbsensi = onKeAbsensi,
                onKeJurnal = onKeJurnal,
                onKePoin = onKePoin,
            )
        }
    }
}

@Composable
private fun HomeContent(
    session: Session,
    tanggal: String,
    jadwal: List<JadwalDto>,
    onKeAbsensi: () -> Unit,
    onKeJurnal: () -> Unit,
    onKePoin: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            HeroCard(
                nama = session.name.substringBefore(' ').substringBefore(','),
                tanggal = formatTanggal(tanggal),
                sesi = jadwal.size,
                jurnalTerisi = jadwal.count { it.sudahJurnal },
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickTile("Absensi", Icons.Filled.CheckCircle, Green600, Green100, onKeAbsensi, Modifier.weight(1f))
                QuickTile("Jurnal", Icons.Filled.Edit, Orange600, Orange100, onKeJurnal, Modifier.weight(1f))
                QuickTile("Poin", Icons.Filled.Warning, Orange600, Orange100, onKePoin, Modifier.weight(1f))
            }
        }

        item {
            Text("Jadwal mengajar hari ini", style = MaterialTheme.typography.titleMedium)
        }

        if (jadwal.isEmpty()) {
            item {
                Text(
                    "Tidak ada jadwal mengajar hari ini.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        items(jadwal, key = { it.id }) { JadwalCard(it, onIsiJurnal = onKeJurnal) }
    }
}

@Composable
private fun HeroCard(nama: String, tanggal: String, sesi: Int, jurnalTerisi: Int) {
    val shape = RoundedCornerShape(28.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Brush.linearGradient(listOf(Orange500, Orange600)))
            .drawBehind {
                // dua lingkaran putih transparan sebagai hiasan
                drawCircle(
                    color = Color.White.copy(alpha = 0.14f),
                    radius = size.height * 0.9f,
                    center = Offset(size.width * 0.95f, -size.height * 0.1f),
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.10f),
                    radius = size.height * 0.5f,
                    center = Offset(size.width * 0.72f, size.height * 1.05f),
                )
            }
            .padding(22.dp),
    ) {
        Column {
            Text(
                "Halo, $nama",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
            )
            Text(
                tanggal,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.92f),
            )

            Spacer(Modifier.height(18.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatPill("$sesi", "Sesi mengajar", Modifier.weight(1f))
                StatPill("$jurnalTerisi/$sesi", "Jurnal terisi", Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun StatPill(nilai: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = 0.22f))
            .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Text(
            nilai,
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            fontWeight = FontWeight.Bold,
        )
        Text(label, style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.92f))
    }
}

@Composable
private fun QuickTile(
    label: String,
    icon: ImageVector,
    warnaIkon: Color,
    warnaLatar: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    GlassCard(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick),
        contentPadding = 14.dp,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(warnaLatar),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = warnaIkon)
        }
        Spacer(Modifier.height(10.dp))
        Text(label, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun JadwalCard(j: JadwalDto, onIsiJurnal: () -> Unit) {
    val rentang = if (j.jamKeMulai == j.jamKeSelesai) "${j.jamKeMulai}" else "${j.jamKeMulai}-${j.jamKeSelesai}"

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Jam ke-$rentang",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.weight(1f))
            Text(
                "${j.jamMulai.take(5)} - ${j.jamSelesai.take(5)}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(Modifier.height(8.dp))

        Text(j.kelas?.nama ?: "-", style = MaterialTheme.typography.titleLarge)
        Text(
            j.mapel?.nama ?: "-",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        j.ruang?.let { ruang ->
            Spacer(Modifier.height(4.dp))
            Text(
                "${if (ruang.tipe == "lab") "Lab" else "Kelas"}: ${ruang.nama}",
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        Spacer(Modifier.height(12.dp))

        when {
            j.sudahJurnal -> Text(
                "✓ Jurnal sudah diisi",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary,
            )

            j.bisaIsiJurnal -> Button(
                onClick = onIsiJurnal,
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Isi jurnal") }

            else -> Text(
                "Jurnal bisa diisi pada jam mengajar",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// "2026-10-26" -> "Senin, 26 Oktober 2026"
private fun formatTanggal(iso: String): String = try {
    val masuk = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(iso)
    SimpleDateFormat("EEEE, d MMMM yyyy", Locale.forLanguageTag("id-ID")).format(masuk!!)
} catch (e: Exception) {
    iso
}