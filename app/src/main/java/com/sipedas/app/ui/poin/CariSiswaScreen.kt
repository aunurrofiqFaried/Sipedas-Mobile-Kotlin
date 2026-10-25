package com.sipedas.app.ui.poin

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sipedas.app.data.model.SiswaCariDto
import com.sipedas.app.ui.components.GlassCard
import com.sipedas.app.ui.components.ScreenHeader

@Composable
fun CariSiswaScreen(
    onPilih: (siswaId: Int) -> Unit,
    viewModel: CariSiswaViewModel = viewModel(factory = CariSiswaViewModel.Factory),
) {
    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(title = "Poin pelanggaran")

        OutlinedTextField(
            value = viewModel.query,
            onValueChange = viewModel::onQueryChange,
            label = { Text("Cari siswa (nama atau NIS)") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            singleLine = true,
            shape = MaterialTheme.shapes.medium,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
        )

        Spacer(Modifier.height(12.dp))

        val q = viewModel.query.trim()

        when {
            q.length < 2 -> Pesan("Ketik minimal 2 huruf nama atau angka NIS siswa.")

            viewModel.loading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopCenter,
            ) { CircularProgressIndicator(modifier = Modifier.padding(top = 32.dp)) }

            viewModel.error != null -> Pesan(viewModel.error ?: "", galat = true)

            viewModel.hasil.isEmpty() -> Pesan("Siswa tidak ditemukan.")

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(viewModel.hasil, key = { it.id }) { s ->
                    SiswaCard(s, onClick = { onPilih(s.id) })
                }
            }
        }
    }
}

@Composable
private fun Pesan(teks: String, galat: Boolean = false) {
    Text(
        text = teks,
        color = if (galat) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
    )
}

@Composable
private fun SiswaCard(s: SiswaCariDto, onClick: () -> Unit) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick),
        contentPadding = 16.dp,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(s.nama, style = MaterialTheme.typography.titleMedium)
                Text(
                    "NIS ${s.nis}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                s.kelas ?: "-",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}