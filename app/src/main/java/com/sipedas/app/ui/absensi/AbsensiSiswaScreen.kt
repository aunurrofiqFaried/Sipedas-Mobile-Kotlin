package com.sipedas.app.ui.absensi

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sipedas.app.ui.components.GlassCard
import com.sipedas.app.ui.components.ScreenHeader
import com.sipedas.app.ui.theme.Green600
import com.sipedas.app.ui.theme.InkSoft

private data class OpsiStatus(val kode: String, val label: String, val warna: Color)

private val opsiStatus = listOf(
    OpsiStatus("hadir", "Hadir", Green600),
    OpsiStatus("sakit", "Sakit", Color(0xFFD97706)),
    OpsiStatus("izin", "Izin", Color(0xFF0284C7)),
    OpsiStatus("alpa", "Alpa", Color(0xFFDC2626)),
)

private fun warnaStatus(kode: String?): Color =
    opsiStatus.firstOrNull { it.kode == kode }?.warna ?: InkSoft

@Composable
fun AbsensiSiswaScreen(
    kelasId: Int,
    onBack: () -> Unit,
    viewModel: AbsensiSiswaViewModel = viewModel(
        key = "absensi-siswa-$kelasId",
        factory = AbsensiSiswaViewModel.factory(kelasId),
    ),
) {
    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(title = viewModel.kelasNama.ifEmpty { "Absensi" }, onBack = onBack)

        when {
            viewModel.loading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }

            viewModel.error != null && viewModel.siswa.isEmpty() -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(viewModel.error ?: "", color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(16.dp))
                Button(onClick = viewModel::muat) { Text("Coba lagi") }
            }

            else -> Konten(viewModel)
        }
    }
}

@Composable
private fun Konten(viewModel: AbsensiSiswaViewModel) {
    val siswa = viewModel.siswa
    val belumDitandai = siswa.count { it.status == null }
    val adaTidakHadir = siswa.any { it.status != null && it.status != "hadir" }

    Column(modifier = Modifier.fillMaxSize()) {

        // Ringkasan berwarna
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                opsiStatus.forEach { opsi ->
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(opsi.warna.copy(alpha = 0.12f))
                            .padding(vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            "${siswa.count { it.status == opsi.kode }}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = opsi.warna,
                        )
                        Text(
                            opsi.label,
                            style = MaterialTheme.typography.labelMedium,
                            color = opsi.warna,
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (belumDitandai == 0) "Semua siswa sudah ditandai" else "$belumDitandai siswa belum ditandai",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = viewModel::semuaHadir) { Text("Tandai semua hadir") }
            }
        }

        viewModel.pesan?.let { p ->
            Text(
                text = p.teks + if (p.sukses && adaTidakHadir) {
                    "\nHubungi wali siswa yang tidak hadir lewat tombol WhatsApp pada nama mereka."
                } else {
                    ""
                },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = if (p.sukses) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
            )
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(siswa, key = { it.id }) { s ->
                SiswaCard(s, onStatus = { kode -> viewModel.setStatus(s.id, kode) })
            }
        }

        // Tombol simpan di bawah
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)) {
            Button(
                onClick = viewModel::simpan,
                enabled = belumDitandai == 0 && !viewModel.saving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
            ) {
                if (viewModel.saving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Text(
                        text = if (belumDitandai == 0) "Simpan absensi" else "Tandai $belumDitandai siswa lagi",
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}

@Composable
private fun SiswaCard(s: SiswaItem, onStatus: (String) -> Unit) {
    val context = LocalContext.current
    val warna = warnaStatus(s.status)

    GlassCard(modifier = Modifier.fillMaxWidth(), contentPadding = 14.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(warna.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = s.nama.trim().firstOrNull()?.uppercase() ?: "?",
                    style = MaterialTheme.typography.titleMedium,
                    color = warna,
                )
            }

            Column(modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)) {
                Text(
                    s.nama,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    s.nis,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            opsiStatus.forEach { opsi ->
                StatusChip(
                    opsi = opsi,
                    dipilih = s.status == opsi.kode,
                    onClick = { onStatus(opsi.kode) },
                    modifier = Modifier.weight(1f),
                )
            }
        }

        // WhatsApp hanya untuk yang tidak hadir DAN statusnya sudah tersimpan
        val tampilWa = s.status != null && s.status != "hadir" && s.status == s.statusServer
        if (tampilWa) {
            Spacer(Modifier.height(8.dp))
            if (s.waLink != null) {
                OutlinedButton(
                    onClick = { bukaWa(context, s.waLink) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Hubungi wali via WhatsApp") }
            } else {
                Text(
                    "Nomor wali belum diisi",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun StatusChip(
    opsi: OpsiStatus,
    dipilih: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(12.dp)

    Box(
        modifier = modifier
            .clip(shape)
            .background(if (dipilih) opsi.warna else opsi.warna.copy(alpha = 0.08f))
            .border(1.dp, opsi.warna.copy(alpha = if (dipilih) 1f else 0.35f), shape)
            .clickable(onClick = onClick)
            .padding(vertical = 9.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            opsi.label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (dipilih) Color.White else opsi.warna,
        )
    }
}

private fun bukaWa(context: Context, link: String) {
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link)))
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "Tidak ada aplikasi untuk membuka WhatsApp", Toast.LENGTH_SHORT).show()
    }
}