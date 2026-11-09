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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sipedas.app.data.model.JenisRemisiDto
import com.sipedas.app.ui.components.ScreenHeader
import com.sipedas.app.ui.theme.Green600

@Composable
fun BeriRemisiScreen(
    siswaId: Int,
    onBack: () -> Unit,
    onSelesai: () -> Unit,
    viewModel: BeriRemisiViewModel = viewModel(
        key = "remisi-$siswaId",
        factory = BeriRemisiViewModel.factory(siswaId),
    ),
) {
    LaunchedEffect(viewModel.selesai) {
        if (viewModel.selesai) onSelesai()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
    ) {
        ScreenHeader(title = "Beri remisi poin", onBack = onBack)

        when {
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
private fun Formulir(viewModel: BeriRemisiViewModel) {
    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item(key = "pengantar") {
                Text(
                    "Pilih kegiatan atau ketertiban yang dilakukan siswa. " +
                            "Poin pelanggarannya akan dikurangi sesuai nilainya.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (viewModel.jenis.isEmpty()) {
                item(key = "kosong") {
                    Text(
                        "Belum ada jenis remisi. Minta admin menambahkannya di panel.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            items(viewModel.jenis, key = { "jenis-${it.id}" }) { j ->
                JenisRemisiItem(
                    j = j,
                    dipilih = viewModel.terpilih == j.id,
                    onClick = { viewModel.pilih(j.id) },
                )
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
                        text = if (j == null) "Pilih kegiatan" else "Berikan: ${j.nama} (−${j.poin} poin)",
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}

@Composable
private fun JenisRemisiItem(j: JenisRemisiDto, dipilih: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(16.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (dipilih) Green600.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
            .border(
                width = if (dipilih) 2.dp else 1.dp,
                color = if (dipilih) Green600 else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                shape = shape,
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                j.nama,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (dipilih) FontWeight.SemiBold else FontWeight.Normal,
            )
            if (!j.keterangan.isNullOrBlank()) {
                Text(
                    j.keterangan,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Text(
            "−${j.poin}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Green600,
        )
    }
}