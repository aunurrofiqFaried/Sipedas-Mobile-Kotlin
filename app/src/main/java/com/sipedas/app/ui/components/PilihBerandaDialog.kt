package com.sipedas.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sipedas.app.ui.util.Beranda

@Composable
fun PilihBerandaDialog(
    pilihan: List<Beranda>,
    aktif: Beranda,
    onPilih: (Beranda) -> Unit,
    onTutup: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onTutup,
        title = { Text("Pilih beranda") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                pilihan.forEach { b ->
                    val dipilih = b == aktif

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (dipilih) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                            )
                            .clickable { onPilih(b) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(b.ikon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 12.dp),
                        ) {
                            Text(b.judul, fontWeight = FontWeight.SemiBold)
                            Text(
                                b.keterangan,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        if (dipilih) {
                            Icon(Icons.Filled.Check, contentDescription = "Dipilih", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onTutup) { Text("Tutup") }
        },
    )
}