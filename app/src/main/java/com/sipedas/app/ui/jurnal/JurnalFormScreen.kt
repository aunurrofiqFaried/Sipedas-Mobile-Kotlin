package com.sipedas.app.ui.jurnal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sipedas.app.ui.components.GlassCard
import com.sipedas.app.ui.components.ScreenHeader

@Composable
fun JurnalFormScreen(
    jadwalId: Int,
    onBack: () -> Unit,
    onSelesai: () -> Unit,
    viewModel: JurnalFormViewModel = viewModel(
        key = "jurnal-isi-$jadwalId",
        factory = JurnalFormViewModel.factory(jadwalId),
    ),
) {
    LaunchedEffect(viewModel.selesai) {
        if (viewModel.selesai) onSelesai()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(title = "Isi jurnal", onBack = onBack)

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
private fun Formulir(viewModel: JurnalFormViewModel) {
    val j = viewModel.jadwal ?: return
    val rentang = if (j.jamKeMulai == j.jamKeSelesai) "${j.jamKeMulai}" else "${j.jamKeMulai}-${j.jamKeSelesai}"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Text(j.kelas?.nama ?: "-", style = MaterialTheme.typography.titleLarge)
            Text(
                j.mapel?.nama ?: "-",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
            Row {
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
        }

        OutlinedTextField(
            value = viewModel.materi,
            onValueChange = viewModel::onMateriChange,
            label = { Text("Materi pembelajaran *") },
            supportingText = { Text("${viewModel.materi.length}/${JurnalFormViewModel.BATAS}") },
            minLines = 4,
            maxLines = 8,
            enabled = !viewModel.saving,
            shape = MaterialTheme.shapes.medium,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
            value = viewModel.catatan,
            onValueChange = viewModel::onCatatanChange,
            label = { Text("Catatan (opsional)") },
            minLines = 2,
            maxLines = 5,
            enabled = !viewModel.saving,
            shape = MaterialTheme.shapes.medium,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            modifier = Modifier.fillMaxWidth(),
        )

        Text(
            "Jurnal hanya dapat disimpan selama jam mengajar sesuai jadwal.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        viewModel.saveError?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        }

        Button(
            onClick = viewModel::simpan,
            enabled = !viewModel.saving,
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
                Text("Simpan jurnal", style = MaterialTheme.typography.labelLarge)
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}