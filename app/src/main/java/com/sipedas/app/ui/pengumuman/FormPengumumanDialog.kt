package com.sipedas.app.ui.pengumuman

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sipedas.app.data.model.PengumumanDto
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

// awal = null untuk pengumuman baru
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormPengumumanDialog(
    awal: PengumumanDto?,
    menyimpan: Boolean,
    pesan: String?,
    onSimpan: (judul: String, isi: String, tingkat: String, berlakuSampai: String?) -> Unit,
    onTutup: () -> Unit,
) {
    var judul by rememberSaveable { mutableStateOf(awal?.judul ?: "") }
    var isi by rememberSaveable { mutableStateOf(awal?.isi ?: "") }
    var tingkat by rememberSaveable { mutableStateOf(awal?.tingkat ?: "biasa") }
    var berlaku by rememberSaveable { mutableStateOf<String?>(awal?.berlakuSampai) }
    var pilihTanggal by remember { mutableStateOf(false) }

    val siap = judul.isNotBlank() && isi.isNotBlank() && !menyimpan

    AlertDialog(
        onDismissRequest = { if (!menyimpan) onTutup() },
        title = { Text(if (awal == null) "Buat pengumuman" else "Ubah pengumuman") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = judul,
                    onValueChange = { if (it.length <= 150) judul = it },
                    label = { Text("Judul") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                OutlinedTextField(
                    value = isi,
                    onValueChange = { if (it.length <= 5000) isi = it },
                    label = { Text("Isi pengumuman") },
                    minLines = 4,
                    maxLines = 8,
                    modifier = Modifier.fillMaxWidth(),
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = tingkat == "biasa",
                        onClick = { tingkat = "biasa" },
                        label = { Text("Biasa") },
                    )
                    FilterChip(
                        selected = tingkat == "penting",
                        onClick = { tingkat = "penting" },
                        label = { Text("Penting") },
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = { pilihTanggal = true }) {
                        Text(berlaku?.let { "Berlaku sampai $it" } ?: "Berlaku sampai (opsional)")
                    }
                    if (berlaku != null) {
                        TextButton(onClick = { berlaku = null }) { Text("Hapus") }
                    }
                }

                pesan?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(enabled = siap, onClick = { onSimpan(judul, isi, tingkat, berlaku) }) {
                Text(if (menyimpan) "Menyimpan..." else if (awal == null) "Terbitkan" else "Simpan")
            }
        },
        dismissButton = {
            TextButton(enabled = !menyimpan, onClick = onTutup) { Text("Batal") }
        },
    )

    if (pilihTanggal) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = berlaku?.let {
                LocalDate.parse(it).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
            },
        )

        DatePickerDialog(
            onDismissRequest = { pilihTanggal = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let {
                        berlaku = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate().toString()
                    }
                    pilihTanggal = false
                }) { Text("Pilih") }
            },
            dismissButton = {
                TextButton(onClick = { pilihTanggal = false }) { Text("Batal") }
            },
        ) {
            DatePicker(state = state)
        }
    }
}