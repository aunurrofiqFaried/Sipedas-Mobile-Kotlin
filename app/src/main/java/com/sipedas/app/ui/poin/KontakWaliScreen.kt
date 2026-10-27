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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sipedas.app.data.model.KontakWaliResponse
import com.sipedas.app.data.model.RiwayatKontakDto
import com.sipedas.app.ui.components.GlassCard
import com.sipedas.app.ui.components.ScreenHeader
import com.sipedas.app.ui.util.bukaWhatsApp
import com.sipedas.app.ui.util.formatWaktu

private val daftarTujuan = listOf(
    "konseling" to "Konseling",
    "pelanggaran" to "Pelanggaran",
    "absensi" to "Absensi",
    "lainnya" to "Lainnya",
)

private fun labelTujuan(kode: String): String =
    daftarTujuan.firstOrNull { it.first == kode }?.second ?: kode

@Composable
fun KontakWaliScreen(
    siswaId: Int,
    onBack: () -> Unit,
    viewModel: KontakWaliViewModel = viewModel(
        key = "kontak-wali-$siswaId",
        factory = KontakWaliViewModel.factory(siswaId),
    ),
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
    ) {
        ScreenHeader(title = "Hubungi wali", onBack = onBack)

        val data = viewModel.data

        when {
            viewModel.loading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }

            data == null -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(viewModel.error ?: "Data tidak tersedia.", color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(16.dp))
                Button(onClick = viewModel::muat) { Text("Coba lagi") }
            }

            else -> Isi(
                data = data,
                viewModel = viewModel,
                onBukaWa = { link -> bukaWhatsApp(context, link) },
            )
        }
    }
}

@Composable
private fun Isi(
    data: KontakWaliResponse,
    viewModel: KontakWaliViewModel,
    onBukaWa: (String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text(data.siswa.nama, style = MaterialTheme.typography.titleLarge)
                    Text(
                        "NIS ${data.siswa.nis} · ${data.siswa.kelas ?: "tanpa kelas"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Spacer(Modifier.height(12.dp))

                    Text(
                        "Orang tua / wali",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        buildString {
                            append(data.wali.nama ?: "Nama wali belum diisi")
                            data.wali.hubungan?.let { append(" (").append(it).append(")") }
                        },
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        data.wali.noHp ?: "Nomor belum diisi",
                        style = MaterialTheme.typography.bodyMedium,
                    )

                    if (!data.adaNomorWali) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Nomor wali belum valid atau belum diisi. Minta admin melengkapinya di data siswa.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }

            item {
                Text("Tujuan menghubungi", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    daftarTujuan.forEach { (kode, label) ->
                        TujuanChip(
                            label = label,
                            dipilih = viewModel.tujuan == kode,
                            onClick = { viewModel.pilihTujuan(kode) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = viewModel.catatan,
                    onValueChange = viewModel::onCatatan,
                    label = { Text("Catatan (opsional)") },
                    placeholder = { Text("Hal yang akan dibicarakan dengan wali") },
                    minLines = 2,
                    maxLines = 5,
                    enabled = !viewModel.sending,
                    shape = MaterialTheme.shapes.medium,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            viewModel.pesan?.let { p ->
                item {
                    Text(p, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                }
            }

            item {
                Spacer(Modifier.height(4.dp))
                Text("Riwayat kontak", style = MaterialTheme.typography.titleMedium)
            }

            if (data.riwayat.isEmpty()) {
                item {
                    Text(
                        "Belum pernah dihubungi lewat aplikasi.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            items(data.riwayat, key = { it.id }) { RiwayatKontakItem(it) }
        }

        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)) {
            Button(
                onClick = { viewModel.hubungi(onBukaWa) },
                enabled = data.waLink != null && !viewModel.sending,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
            ) {
                if (viewModel.sending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Text("Hubungi via WhatsApp", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
private fun TujuanChip(
    label: String,
    dipilih: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(12.dp)
    val warna = MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier
            .clip(shape)
            .background(if (dipilih) warna else warna.copy(alpha = 0.08f))
            .border(1.dp, warna.copy(alpha = if (dipilih) 1f else 0.35f), shape)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (dipilih) MaterialTheme.colorScheme.onPrimary else warna,
        )
    }
}

@Composable
private fun RiwayatKontakItem(k: RiwayatKontakDto) {
    GlassCard(modifier = Modifier.fillMaxWidth(), contentPadding = 16.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                labelTujuan(k.tujuan),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f),
            )
            Text(
                formatWaktu(k.waktu),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            "oleh ${k.oleh ?: "-"}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (!k.catatan.isNullOrBlank()) {
            Spacer(Modifier.height(4.dp))
            Text(k.catatan, style = MaterialTheme.typography.bodyMedium)
        }
    }
}