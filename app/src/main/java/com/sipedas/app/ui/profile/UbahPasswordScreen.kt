package com.sipedas.app.ui.profile

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sipedas.app.ui.components.GlassCard
import com.sipedas.app.ui.components.ScreenHeader

@Composable
fun UbahPasswordScreen(
    onBack: () -> Unit,
    onSelesai: () -> Unit,
    viewModel: UbahPasswordViewModel = viewModel(factory = UbahPasswordViewModel.Factory),
) {
    val context = LocalContext.current
    var tampil by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel.selesai) {
        if (viewModel.selesai) {
            Toast.makeText(context, "Password berhasil diubah", Toast.LENGTH_SHORT).show()
            onSelesai()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
    ) {
        ScreenHeader(title = "Ubah password", onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                KolomPassword(
                    label = "Password lama",
                    value = viewModel.lama,
                    onChange = viewModel::onLama,
                    tampil = tampil,
                    enabled = !viewModel.saving,
                    ime = ImeAction.Next,
                )

                Spacer(Modifier.height(12.dp))

                KolomPassword(
                    label = "Password baru",
                    value = viewModel.baru,
                    onChange = viewModel::onBaru,
                    tampil = tampil,
                    enabled = !viewModel.saving,
                    ime = ImeAction.Next,
                )

                Spacer(Modifier.height(12.dp))

                KolomPassword(
                    label = "Ulangi password baru",
                    value = viewModel.konfirmasi,
                    onChange = viewModel::onKonfirmasi,
                    tampil = tampil,
                    enabled = !viewModel.saving,
                    ime = ImeAction.Done,
                    onDone = viewModel::simpan,
                )

                TextButton(onClick = { tampil = !tampil }) {
                    Text(if (tampil) "Sembunyikan password" else "Tampilkan password")
                }
            }

            Text(
                "Minimal 8 karakter dan mengandung huruf serta angka. " +
                        "Setelah diganti, perangkat lain yang memakai akun ini akan keluar otomatis.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            viewModel.error?.let {
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
                    Text("Simpan password baru", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
private fun KolomPassword(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    tampil: Boolean,
    enabled: Boolean,
    ime: ImeAction,
    onDone: () -> Unit = {},
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = true,
        enabled = enabled,
        shape = MaterialTheme.shapes.medium,
        visualTransformation = if (tampil) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ime),
        keyboardActions = KeyboardActions(onDone = { onDone() }),
        modifier = Modifier.fillMaxWidth(),
    )
}