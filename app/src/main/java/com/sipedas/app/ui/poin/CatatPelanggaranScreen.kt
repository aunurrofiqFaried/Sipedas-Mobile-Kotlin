package com.sipedas.app.ui.poin

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sipedas.app.data.model.CatatPelanggaranResponse
import com.sipedas.app.data.model.JenisPelanggaranDto
import com.sipedas.app.ui.components.GlassCard
import com.sipedas.app.ui.components.ScreenHeader

@Composable
fun CatatPelanggaranScreen(
    siswaId: Int,
    onBack: () -> Unit,
    onSelesai: () -> Unit,
    viewModel: CatatPelanggaranViewModel = viewModel(
        key = "catat-pelanggaran-$siswaId",
        factory = CatatPelanggaranViewModel.factory(siswaId),
    ),
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
    ) {
        ScreenHeader(title = "Catat pelanggaran", onBack = onBack)

        val hasil = viewModel.hasil

        when {
            hasil != null -> HasilView(hasil, onSelesai)

            viewModel.loading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }

            viewModel.loadError != null -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(viewModel.loadError ?: "", color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(16.dp))
                Button(onClick = viewModel::muat) { Text("Coba lagi") }
            }

            else -> Formulir(viewModel)
        }
    }
}

@Composable
private fun Formulir(viewModel: CatatPelanggaranViewModel) {
    val kelompok = viewModel.jenis.groupBy { it.kategori ?: "lainnya" }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            kelompok.forEach { (kategori, daftar) ->
                item(key = "judul-$kategori") {
                    Text(
                        kategori.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.titleSmall,
                        color = warnaKategori(kategori),
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }

                items(daftar, key = { "jenis-${it.id}" }) { j ->
                    JenisItem(
                        j = j,
                        dipilih = viewModel.terpilih == j.id,
                        onClick = { viewModel.pilih(j.id) },
                    )
                }
            }

            item(key = "keterangan") {
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = viewModel.keterangan,
                    onValueChange = viewModel::onKeterangan,
                    label = { Text("Keterangan (opsional)") },
                    minLines = 2,
                    maxLines = 5,
                    enabled = !viewModel.saving,
                    shape = MaterialTheme.shapes.medium,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)) {
            viewModel.saveError?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(8.dp))
            }

            Button(
                onClick = viewModel::simpan,
                enabled = viewModel.terpilih != null && !viewModel.saving,
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
                    val j = viewModel.jenisTerpilih
                    Text(
                        text = if (j == null) "Pilih jenis pelanggaran" else "Catat: ${j.nama} (+${j.poin} poin)",
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}

@Composable
private fun JenisItem(j: JenisPelanggaranDto, dipilih: Boolean, onClick: () -> Unit) {
    val warna = warnaKategori(j.kategori)
    val shape = RoundedCornerShape(16.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (dipilih) warna.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
            .border(
                width = if (dipilih) 2.dp else 1.dp,
                color = if (dipilih) warna else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                shape = shape,
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            j.nama,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (dipilih) FontWeight.SemiBold else FontWeight.Normal,
            modifier = Modifier.weight(1f),
        )
        Text(
            "+${j.poin}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = warna,
        )
    }
}

@Composable
private fun HasilView(hasil: CatatPelanggaranResponse, onSelesai: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                "✓ Pelanggaran tercatat",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.secondary,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                "+${hasil.poinDitambahkan} poin",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                "Total poin siswa sekarang: ${hasil.totalPoin}",
                style = MaterialTheme.typography.bodyLarge,
            )

            hasil.spBaru?.let { sp ->
                Spacer(Modifier.height(14.dp))
                Text(
                    "Siswa mencapai batas ${sp.aturan?.nama ?: "SP"} (${sp.aturan?.batasPoin ?: "-"} poin). " +
                            "Usulan SP sudah dikirim ke kesiswaan untuk disetujui.",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = onSelesai,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
        ) { Text("Selesai", style = MaterialTheme.typography.labelLarge) }
    }
}