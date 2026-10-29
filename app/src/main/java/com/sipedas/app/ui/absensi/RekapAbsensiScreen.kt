package com.sipedas.app.ui.absensi

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sipedas.app.data.model.KelasRekapDto
import com.sipedas.app.data.model.RekapAbsensiResponse
import com.sipedas.app.data.model.RekapSiswaDto
import com.sipedas.app.ui.components.ScreenHeader
import java.time.format.DateTimeFormatter
import java.util.Locale

private val warnaHadir = Color(0xFF16A34A)
private val warnaSakit = Color(0xFFD97706)
private val warnaIzin = Color(0xFF0284C7)
private val warnaAlpa = Color(0xFFDC2626)

private val formatBulan = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.forLanguageTag("id-ID"))

@Composable
fun RekapAbsensiScreen(
    onBack: () -> Unit,
    viewModel: RekapAbsensiViewModel = viewModel(factory = RekapAbsensiViewModel.Factory),
) {
    var dialogKelas by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(title = "Rekap kehadiran", onBack = onBack)

        when {
            viewModel.kelasLoading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }

            viewModel.kelasError != null -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(viewModel.kelasError ?: "", color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(16.dp))
                Button(onClick = viewModel::muatKelas) { Text("Coba lagi") }
            }

            else -> Isi(viewModel, onPilihKelas = { dialogKelas = true })
        }
    }

    if (dialogKelas) {
        PilihKelasDialog(
            daftar = viewModel.daftarKelas,
            dipilih = viewModel.kelasId,
            onPilih = { k ->
                viewModel.pilihKelas(k.id)
                dialogKelas = false
            },
            onTutup = { dialogKelas = false },
        )
    }
}

@Composable
private fun Isi(viewModel: RekapAbsensiViewModel, onPilihKelas: () -> Unit) {
    val hasil = viewModel.hasil

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item(key = "kelas") {
            OutlinedButton(
                onClick = onPilihKelas,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
            ) {
                Text(
                    viewModel.kelasNama ?: "Pilih kelas",
                    style = MaterialTheme.typography.titleSmall,
                )
            }
        }

        item(key = "periode") {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { viewModel.geserBulan(-1) },
                        enabled = !viewModel.semuaTahun && viewModel.bisaMundur(),
                    ) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Bulan sebelumnya")
                    }

                    Text(
                        text = if (viewModel.semuaTahun) "Satu tahun ajaran" else viewModel.bulan.format(formatBulan),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f),
                    )

                    IconButton(
                        onClick = { viewModel.geserBulan(1) },
                        enabled = !viewModel.semuaTahun && viewModel.bisaMaju(),
                    ) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Bulan berikutnya")
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ChipPilihan("Per bulan", !viewModel.semuaTahun, Modifier.weight(1f)) {
                        viewModel.ubahSemuaTahun(false)
                    }
                    ChipPilihan("Satu tahun ajaran", viewModel.semuaTahun, Modifier.weight(1f)) {
                        viewModel.ubahSemuaTahun(true)
                    }
                }
            }
        }

        when {
            viewModel.kelasId == null -> item(key = "petunjuk") {
                Text(
                    "Pilih kelas untuk melihat jumlah sakit, izin, dan alpa tiap siswa.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            viewModel.memuat -> item(key = "memuat") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center,
                ) { CircularProgressIndicator() }
            }

            viewModel.error != null -> item(key = "galat") {
                Column {
                    Text(viewModel.error ?: "", color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(10.dp))
                    Button(onClick = viewModel::muatRekap) { Text("Coba lagi") }
                }
            }

            hasil != null -> {
                item(key = "ringkasan") { Ringkasan(hasil) }

                item(key = "kepala") { BarisKepala() }

                if (hasil.siswa.isEmpty()) {
                    item(key = "kosong") {
                        Text(
                            "Kelas ini belum punya siswa aktif.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                items(hasil.siswa, key = { "siswa-${it.id}" }) { BarisSiswa(it) }
            }
        }
    }
}

@Composable
private fun Ringkasan(hasil: RekapAbsensiResponse) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            "${hasil.kelas.nama} · ${hasil.periode.label}",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Ubin("Hadir", hasil.total.hadir, warnaHadir, Modifier.weight(1f))
            Ubin("Sakit", hasil.total.sakit, warnaSakit, Modifier.weight(1f))
            Ubin("Izin", hasil.total.izin, warnaIzin, Modifier.weight(1f))
            Ubin("Alpa", hasil.total.alpa, warnaAlpa, Modifier.weight(1f))
        }

        Text(
            "Absensi tercatat pada ${hasil.hariTercatat} hari",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun Ubin(label: String, nilai: Int, warna: Color, modifier: Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(warna.copy(alpha = 0.12f))
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            "$nilai",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = warna,
        )
        Text(label, style = MaterialTheme.typography.labelMedium, color = warna)
    }
}

@Composable
private fun BarisKepala() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            "Nama siswa",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
        )
        Kolom("H", warnaHadir, tebal = true)
        Kolom("S", warnaSakit, tebal = true)
        Kolom("I", warnaIzin, tebal = true)
        Kolom("A", warnaAlpa, tebal = true)
    }
}

@Composable
private fun BarisSiswa(s: RekapSiswaDto) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                s.nama,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                s.nis,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Angka(s.hadir, warnaHadir)
        Angka(s.sakit, warnaSakit)
        Angka(s.izin, warnaIzin)
        Angka(s.alpa, warnaAlpa)
    }
}

// Angka nol ditampilkan redup supaya yang bermasalah langsung terlihat
@Composable
private fun Angka(nilai: Int, warna: Color) {
    if (nilai == 0) {
        Kolom("–", MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f), tebal = false)
    } else {
        Kolom("$nilai", warna, tebal = true)
    }
}

@Composable
private fun Kolom(teks: String, warna: Color, tebal: Boolean) {
    Text(
        text = teks,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = if (tebal) FontWeight.Bold else FontWeight.Normal,
        color = warna,
        textAlign = TextAlign.Center,
        modifier = Modifier.width(38.dp),
    )
}

@Composable
private fun ChipPilihan(label: String, dipilih: Boolean, modifier: Modifier, onClick: () -> Unit) {
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
private fun PilihKelasDialog(
    daftar: List<KelasRekapDto>,
    dipilih: Int?,
    onPilih: (KelasRekapDto) -> Unit,
    onTutup: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onTutup,
        title = { Text("Pilih kelas") },
        text = {
            LazyColumn(modifier = Modifier.heightIn(max = 420.dp)) {
                items(daftar, key = { it.id }) { k ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (k.id == dipilih) MaterialTheme.colorScheme.primary.copy(alpha = 0.10f) else Color.Transparent
                            )
                            .clickable { onPilih(k) }
                            .padding(horizontal = 10.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            k.nama,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (k.id == dipilih) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            "${k.jumlahSiswa} siswa",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onTutup) { Text("Tutup") } },
    )
}