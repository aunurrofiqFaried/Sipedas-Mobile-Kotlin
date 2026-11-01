package com.sipedas.app.ui.pengumuman

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sipedas.app.data.model.PengumumanDto
import com.sipedas.app.ui.theme.Green600
import com.sipedas.app.ui.util.formatWaktu
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.ui.unit.sp

private val warnaPenting = Color(0xFFDC2626)

// Ikon lonceng di pojok kanan atas; lingkaran merah berisi jumlah yang belum dibaca
// Ikon lonceng di pojok kanan atas; lencana merah berisi jumlah yang belum dibaca
// Ikon lonceng mengambang di pojok kanan atas, tanpa latar
@Composable
fun LoncengPengumuman(
    belumDibaca: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.primary,
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape) // membatasi riak sentuhan agar bundar
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            Icons.Filled.Notifications,
            contentDescription = "Pengumuman",
            tint = tint,
            modifier = Modifier.size(28.dp),
        )

        if (belumDibaca > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 3.dp, end = 3.dp)
                    .defaultMinSize(minWidth = 20.dp, minHeight = 20.dp)
                    .clip(CircleShape)
                    .background(warnaPenting)
                    .border(2.dp, Color.White, CircleShape)
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (belumDibaca > 9) "9+" else "$belumDibaca",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        lineHeight = 12.sp,
                    ),
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }
        }
    }
}

// Panel yang meluncur dari sisi kanan, dengan latar gelap di sisi kiri
@Composable
fun PanelPengumuman(viewModel: PengumumanViewModel) {
    BackHandler(enabled = viewModel.panelBuka) { viewModel.tutupPanel() }

    AnimatedVisibility(
        visible = viewModel.panelBuka,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.35f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = viewModel::tutupPanel,
                ),
        )
    }

    AnimatedVisibility(
        visible = viewModel.panelBuka,
        enter = slideInHorizontally(animationSpec = tween(250)) { it },
        exit = slideOutHorizontally(animationSpec = tween(250)) { it },
        modifier = Modifier.fillMaxSize(),
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.CenterEnd) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.88f)
                    .widthIn(max = 380.dp)
                    .clip(RoundedCornerShape(topStart = 28.dp, bottomStart = 28.dp))
                    .background(Color.White.copy(alpha = 0.97f))
                    // menyerap ketukan supaya tidak menutup panel lewat latar gelap
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {},
                    ),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 8.dp, top = 12.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "Pengumuman",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = viewModel::tutupPanel) {
                        Icon(Icons.Filled.Close, contentDescription = "Tutup")
                    }
                }

                viewModel.error?.let {
                    Text(
                        it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 20.dp),
                    )
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (viewModel.daftar.isEmpty()) {
                        item {
                            Text(
                                "Belum ada pengumuman.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(4.dp),
                            )
                        }
                    }

                    items(viewModel.daftar, key = { it.id }) { p ->
                        PengumumanItem(p, baru = p.id > viewModel.batasBaru)
                    }
                }
            }
        }
    }
}

@Composable
private fun PengumumanItem(p: PengumumanDto, baru: Boolean) {
    val penting = p.tingkat == "penting"
    val warna = if (penting) warnaPenting else MaterialTheme.colorScheme.primary
    val shape = RoundedCornerShape(18.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(warna.copy(alpha = if (baru) 0.12f else 0.05f))
            .border(
                width = if (baru) 1.5.dp else 1.dp,
                color = warna.copy(alpha = if (baru) 0.6f else 0.2f),
                shape = shape,
            )
            .padding(14.dp),
    ) {
        if (penting || baru) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (penting) Label("Penting", warnaPenting)
                if (baru) Label("Baru", Green600)
            }
            Spacer(Modifier.height(6.dp))
        }

        Text(
            p.judul,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )

        Spacer(Modifier.height(4.dp))
        Text(p.isi, style = MaterialTheme.typography.bodyMedium)

        Spacer(Modifier.height(8.dp))
        Text(
            text = formatWaktu(p.tanggal) + (p.pembuat?.let { " · $it" } ?: ""),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun Label(teks: String, warna: Color) {
    Text(
        text = teks,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(warna)
            .padding(horizontal = 8.dp, vertical = 2.dp),
    )
}